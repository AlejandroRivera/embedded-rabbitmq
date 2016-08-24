package io.arivera.oss.embedded.rabbitmq.helpers;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqCommandException;
import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqCtl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessResult;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A helper class used to shut down a specific RabbitMQ Process and wait until it's the process is stopped.
 */
public class ShutdownHelper implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHelper.class);

  private final EmbeddedRabbitMqConfig config;
  private final Future<ProcessResult> rabbitMqProcess;
  private final long timeoutDuration;
  private final TimeUnit timeoutUnit;

  /**
   * Constructs a new instance that will be used to shut down the given RabbitMQ server process.
   */
  public ShutdownHelper(EmbeddedRabbitMqConfig config, Future<ProcessResult> rabbitMqProcess) {
    this.config = config;
    this.rabbitMqProcess = rabbitMqProcess;
    this.timeoutDuration = config.getDefaultRabbitMqCtlTimeoutInMillis();
    this.timeoutUnit = TimeUnit.MILLISECONDS;
  }

  @Override
  public void run() throws ShutDownException {
    submitShutdownRequest();
    confirmShutdown();
  }

  private void submitShutdownRequest() throws ShutDownException {
    Future<ProcessResult> resultFuture;
    try {
      resultFuture = new RabbitMqCtl(config).stop();
    } catch (RabbitMqCommandException e) {
      throw new ShutDownException("Could not successfully execute command to stop RabbitMQ Server", e);
    }

    int exitValue;
    try {
      ProcessResult rabbitMqCtlProcessResult = resultFuture.get(timeoutDuration, timeoutUnit);
      exitValue = rabbitMqCtlProcessResult.getExitValue();
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new ShutDownException("Error while waiting " + timeoutDuration + " " + timeoutUnit + " for command "
          + "to shut down RabbitMQ Server to finish", e);
    }

    if (exitValue == 0) {
      LOGGER.debug("Successfully commanded RabbitMQ Server to stop.");
    } else {
      LOGGER.warn("Command to stop RabbitMQ Sever failed with exit value: " + exitValue);
    }
  }

  private void confirmShutdown() throws ShutDownException {
    int exitValue;
    try {
      ProcessResult rabbitMqProcessResult = rabbitMqProcess.get(timeoutDuration, TimeUnit.MILLISECONDS);
      exitValue = rabbitMqProcessResult.getExitValue();
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new ShutDownException("Error while waiting " + timeoutDuration + " " + timeoutUnit + "for "
          + "RabbitMQ Server to shut down", e);
    }

    if (exitValue == 0) {
      LOGGER.debug("RabbitMQ Server stopped successfully.");
    } else {
      LOGGER.warn("RabbitMQ Server stopped with exit value: " + exitValue);
    }
  }
}
