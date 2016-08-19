package io.arivera.oss.embedded.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class Stopper implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(Stopper.class);

  private final EmbeddedRabbitMqConfig config;
  private final Future<ProcessResult> rabbitMqProcess;
  private final long timeoutDuration;
  private final TimeUnit timeoutUnit;

  Stopper(EmbeddedRabbitMqConfig config, Future<ProcessResult> rabbitMqProcess) {
    this.config = config;
    this.rabbitMqProcess = rabbitMqProcess;
    timeoutDuration = config.getDefaultRabbitMqCtlTimeoutInMillis();
    timeoutUnit = TimeUnit.MILLISECONDS;
  }

  @Override
  public void run() throws ShutDownException {
    stopUsingRabbitMqCtl();
    waitForRabbitMqServerToFinish();
  }

  private void stopUsingRabbitMqCtl() {
    try {
      Future<ProcessResult> resultFuture = new RabbitMqCtl(config).stop();
      ProcessResult rabbitMqCtlProcessResult = resultFuture.get(timeoutDuration, timeoutUnit);

      int exitValue = rabbitMqCtlProcessResult.getExitValue();
      if (exitValue == 0) {
        LOGGER.info("Submitted command to stop RabbitMQ Server successfully.");
      } else {
        LOGGER.warn("Command to stop RabbitMQ Sever failed with exit value: " + exitValue);
      }
    } catch (IOException e) {
      throw new ShutDownException("Could not successfully execute command to stop RabbitMQ Server", e);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new ShutDownException("Error while waiting for command to shut down RabbitMQ to finish", e);
    }
  }

  private void waitForRabbitMqServerToFinish() {
    try {
      ProcessResult rabbitMqProcessResult = rabbitMqProcess.get(timeoutDuration, TimeUnit.MILLISECONDS);
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
