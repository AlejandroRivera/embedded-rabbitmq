package io.arivera.oss.embedded.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

class CachedDownloader implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CachedDownloader.class);

  private final Runnable downloader;
  private final EmbeddedRabbitMqConfig config;

  CachedDownloader(Runnable downloader, EmbeddedRabbitMqConfig config) {
    this.downloader = downloader;
    this.config = config;
  }

  @Override
  public void run() {
    if (isDownloadAlreadyCached()) {
      LOGGER.debug("RabbitMQ has been downloaded before. Using file: {}", config.getDownloadTarget());
    } else {
      download();
    }
  }

  private boolean isDownloadAlreadyCached() {
    File downloadTarget = config.getDownloadTarget();
    return downloadTarget.exists() && downloadTarget.isFile() && downloadTarget.canRead() && downloadTarget.length() > 0;
  }

  private void download() {
    try {
      downloader.run();
    } catch (DownloadException e) {
      if (config.shouldDeleteCachedFileOnErrors()) {
        boolean deleted = config.getDownloadTarget().delete();
        if (deleted) {
          LOGGER.info("Removed partially downloaded file: {}", config.getDownloadTarget());
        } else {
          LOGGER.warn("Could not remove partially downloaded file. Please remove it manually: {}", config.getDownloadTarget());
        }
      } else {
        LOGGER.info("Partially downloaded file will not be deleted: {}", config.getDownloadTarget());
      }
      throw e;
    }
  }
}
