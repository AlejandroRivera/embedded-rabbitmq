package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.StringUtils;
import io.arivera.oss.embedded.rabbitmq.util.SystemUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class Stopper implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(Stopper.class);

  private final EmbeddedRabbitMqConfig config;
  private final StartedProcess rabbitMqProcess;

  Stopper(EmbeddedRabbitMqConfig config, StartedProcess rabbitMqProcess) {
    this.config = config;
    this.rabbitMqProcess = rabbitMqProcess;
  }

  @Override
  public void run() throws ShutDownException {
    stopUsingRabbitMqCtl();
    waitForRabbitMqServerToFinish();
  }

  private void stopUsingRabbitMqCtl() {
    String executable = config.getAppFolder() + "/sbin/rabbitmqctl";
    if (SystemUtils.IS_OS_WINDOWS) {
      executable += ".bat";
    }

    List<String> command = Arrays.asList(executable, "stop");
    try {
      Slf4jStream loggingStream = Slf4jStream.of(EmbeddedRabbitMq.class, "Process.rabbitmqctl");

      ProcessResult rabbitMqCtlProcessResult = new ProcessExecutor()
          .environment(config.getEnvVars())
          .directory(config.getAppFolder())
          .command(command)
          .redirectError(loggingStream.asError())
          .redirectOutput(loggingStream.asInfo())
          .addListener(new LoggingProcessListener(loggingStream.asDebug().getLogger()))
          .destroyOnExit()
          .start()
          .getFuture()
          .get(config.getDefaultRabbitMqCtlTimeoutInMillis(), TimeUnit.MILLISECONDS);

      int exitValue = rabbitMqCtlProcessResult.getExitValue();
      if (exitValue == 0) {
        LOGGER.info("Submitted command to stop RabbitMQ Server successfully.");
      } else {
        LOGGER.warn("Command '" + StringUtils.join(command, " ") + "' exited with value: " + exitValue);
      }
    } catch (IOException e) {
      throw new ShutDownException("Could not successfully execute: " + StringUtils.join(command, " "), e);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new ShutDownException("Command '" + StringUtils.join(command, " ") + "' did not finish as expected", e);
    }
  }

  private void waitForRabbitMqServerToFinish() {
    try {
      Future<ProcessResult> processfuture = rabbitMqProcess.getFuture();
      ProcessResult rabbitMqProcessResult = processfuture.get(
          config.getDefaultRabbitMqCtlTimeoutInMillis(), TimeUnit.MILLISECONDS);
      int exitValue = rabbitMqProcessResult.getExitValue();
      if (exitValue == 0) {
        LOGGER.info("RabbitMQ Server stopped successfully.");
      } else {
        LOGGER.warn("RabbitMQ Server stopped with exit value: " + exitValue);
      }
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new ShutDownException("Error while waiting for RabbitMQ Server to shut down", e);
    }
  }
}
