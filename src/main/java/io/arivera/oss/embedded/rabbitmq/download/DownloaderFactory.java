package io.arivera.oss.embedded.rabbitmq.download;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

public class DownloaderFactory {

  EmbeddedRabbitMqConfig config;

  public DownloaderFactory(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  /**
   * @return an appropriate instance depending on the given configuration.
   */
  public Downloader getNewInstance() {
    Downloader downloader = new BasicDownloader(config);
    if (config.shouldCachedDownload()) {
      downloader = new CachedDownloader(downloader, config);
    }
    return downloader;
  }

}
