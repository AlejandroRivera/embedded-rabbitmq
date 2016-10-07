package io.arivera.oss.embedded.rabbitmq.helpers;

public class ErlangVersionException extends RuntimeException {

  public ErlangVersionException(String message) {
    super(message);
  }

  public ErlangVersionException(String message, Throwable cause) {
    super(message, cause);
  }
}
