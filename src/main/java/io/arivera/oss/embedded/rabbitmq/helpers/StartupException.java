package io.arivera.oss.embedded.rabbitmq.helpers;

import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqCommandException;

public class StartupException extends RabbitMqCommandException {

  public StartupException(String msg) {
    super(msg);
  }

  public StartupException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
