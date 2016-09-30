package io.arivera.oss.embedded.rabbitmq.bin;

public class ErlangShellException extends RuntimeException {
  public ErlangShellException(String message) {
    super(message);
  }

  public ErlangShellException(String message, Throwable cause) {
    super(message, cause);
  }
}
