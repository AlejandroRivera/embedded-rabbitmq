package io.arivera.oss.embedded.rabbitmq.bin;

public class RabbitMqCommandException extends RuntimeException {

  public RabbitMqCommandException(String message) {
    super(message);
  }

  public RabbitMqCommandException(String message, Throwable cause) {
    super(message, cause);
  }

}
