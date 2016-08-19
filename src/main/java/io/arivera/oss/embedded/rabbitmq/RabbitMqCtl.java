package io.arivera.oss.embedded.rabbitmq;

import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.util.concurrent.Future;

public class RabbitMqCtl {

  public static final String COMMAND = "rabbitmqctl";

  private EmbeddedRabbitMqConfig config;

  public RabbitMqCtl(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  public Future<ProcessResult> execute(String... arguments) throws IOException {
    return new RabbitMqCommandExecutor(config, COMMAND, arguments)
        .call()
        .getFuture();
  }

  public Future<ProcessResult> stop() throws IOException {
    return execute("stop");
  }

}
