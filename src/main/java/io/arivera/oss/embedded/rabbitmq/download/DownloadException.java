package io.arivera.oss.embedded.rabbitmq.download;

public class DownloadException extends RuntimeException {

  public DownloadException(String msg) {
    super(msg);
  }

  public DownloadException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
