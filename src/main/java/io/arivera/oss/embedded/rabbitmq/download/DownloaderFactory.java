package io.arivera.oss.embedded.rabbitmq.download;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

public class DownloaderFactory {

  public static Runnable getNewInstance(EmbeddedRabbitMqConfig config) {
    Runnable downloader = new Downloader(config);
    if (config.shouldCachedDownload()) {
      downloader = new CachedDownloader(downloader, config);
    }
    return downloader;
  }

}
