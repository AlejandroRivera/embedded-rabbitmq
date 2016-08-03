package io.arivera.oss.embedded.rabbitmq;

public class ShutDownException extends RuntimeException {

  public ShutDownException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
