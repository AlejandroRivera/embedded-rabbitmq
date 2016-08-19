package io.arivera.oss.embedded.rabbitmq;

import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class Starter implements Callable<Future<ProcessResult>> {

  private final EmbeddedRabbitMqConfig config;

  Starter(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  @Override
  public Future<ProcessResult> call()throws ProcessException  {
    try {
      PatternFinderOutputStream initializationWatcher = new PatternFinderOutputStream(".*completed with \\d+ plugins.*");
      PublishingProcessListener rabbitMqProcessListener = new PublishingProcessListener();
      rabbitMqProcessListener.addSubscriber(initializationWatcher);

      Future<ProcessResult> resultFuture = new RabbitMqServer(config, initializationWatcher).start();

      boolean match = initializationWatcher.waitForMatch(
          config.getRabbitMqServerInitializationTimeoutInMillis(), TimeUnit.MILLISECONDS);

      if (!match) {
        throw new ProcessException("Could not start RabbitMQ Server. See logs for more details.");
      }
      return resultFuture;
    } catch (IOException e) {
      throw new ProcessException("Could not execute RabbitMQ rabbitMqProcess", e);
    }
  }

}
