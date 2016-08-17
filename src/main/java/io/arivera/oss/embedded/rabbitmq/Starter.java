package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.SystemUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jOutputStream;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

class Starter implements Callable<StartedProcess> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedRabbitMq.class.getName() + ".Process.rabbitmq-server");

  private final EmbeddedRabbitMqConfig config;

  Starter(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  @Override
  public StartedProcess call()throws ProcessException  {
    String command = config.getAppFolder().toString() + "/sbin/rabbitmq-server";
    if (SystemUtils.IS_OS_WINDOWS) {
      command += ".bat";
    }

    try {
      PatternFinderOutputStream initializationWatcher = new PatternFinderOutputStream(".*completed with \\d+ plugins.*");
      PublishingProcessListener rabbitMqProcessListener = new PublishingProcessListener();
      rabbitMqProcessListener.addSubscriber(initializationWatcher);

      Slf4jOutputStream processOutputLogger = new RabbitMqServerProcessLogger(LOGGER);
      LoggingProcessListenerDecorator processEventsLogger = new LoggingProcessListenerDecorator(LOGGER, rabbitMqProcessListener);

      StartedProcess rabbitMqProcess = new ProcessExecutor()
          .environment(config.getEnvVars())
          .directory(config.getAppFolder())
          .command(command)
          .redirectError(processOutputLogger)
          .redirectOutput(processOutputLogger)
          .redirectOutputAlsoTo(initializationWatcher)
          .addListener(processEventsLogger)
          .destroyOnExit()
          .start();

      boolean match = initializationWatcher.waitForMatch(
          config.getRabbitMqServerInitializationTimeoutInMillis(), TimeUnit.MILLISECONDS);

      if (!match) {
        throw new ProcessException("Could not start RabbitMQ Server. See logs for more details.");
      }
      return rabbitMqProcess;
    } catch (IOException e) {
      throw new ProcessException("Could not execute RabbitMQ rabbitMqProcess", e);
    }
  }

  private static class RabbitMqServerProcessLogger extends Slf4jOutputStream {

    public RabbitMqServerProcessLogger(Logger logger) {
      super(logger);
    }

    @Override
    protected void processLine(String line) {
      if (line.startsWith("ERROR:")) {
        log.error(line);
      } else {
        log.info(line);
      }
    }
  }
}
