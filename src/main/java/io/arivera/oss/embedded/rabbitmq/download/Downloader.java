package io.arivera.oss.embedded.rabbitmq.download;

public interface Downloader extends Runnable {

  @Override
  void run() throws DownloadException;

  abstract class Decorator implements Downloader {

    final Downloader innerDownloader;

    public Decorator(Downloader innerDownloader) {
      this.innerDownloader = innerDownloader;
    }
  }
}
