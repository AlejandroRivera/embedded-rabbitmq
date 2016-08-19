package io.arivera.oss.embedded.rabbitmq.extract;

public class ExtractException extends RuntimeException {

  public ExtractException(String message) {
    super(message);
  }

  public ExtractException(String message, Throwable cause) {
    super(message, cause);
  }
}
