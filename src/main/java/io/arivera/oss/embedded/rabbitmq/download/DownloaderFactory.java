package io.arivera.oss.embedded.rabbitmq.download;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

public class DownloaderFactory {

  /**
   * @return an appropriate instance depending on the given configuration.
   */
  public static Downloader getNewInstance(EmbeddedRabbitMqConfig config) {
    Downloader downloader = new BasicDownloader(config);
    if (config.shouldCachedDownload()) {
      downloader = new CachedDownloader(downloader, config);
    }
    return downloader;
  }

}
