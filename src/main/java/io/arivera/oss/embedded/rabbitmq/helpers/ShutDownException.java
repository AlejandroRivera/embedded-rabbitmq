package io.arivera.oss.embedded.rabbitmq.helpers;

import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqCommandException;

public class ShutDownException extends RabbitMqCommandException {

  public ShutDownException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
