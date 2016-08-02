package com.rivera.oss.embedded.rabbitmq;

public class ProcessException extends RuntimeException {

  public ProcessException(String msg) {
    super(msg);
  }

  public ProcessException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
