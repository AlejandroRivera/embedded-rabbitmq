package io.arivera.oss.embedded.rabbitmq.extract;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CachedExtractor extends Extractor.Decorator {

  private static final Logger LOGGER = LoggerFactory.getLogger(CachedExtractor.class);

  private final EmbeddedRabbitMqConfig config;

  CachedExtractor(Extractor extractor, EmbeddedRabbitMqConfig config) {
    super(extractor);
    this.config = config;
  }

  @Override
  public void run() throws ExtractionException {
    try {
      innerExtractor.run();
    } catch (ExtractionException e) {
      if (config.shouldDeleteCachedFileOnErrors()) {
        boolean deleted = config.getDownloadTarget().delete();
        if (deleted) {
          LOGGER.info("Removed downloaded file because it's possibly corrupted: {}", config.getDownloadTarget());
        } else {
          LOGGER.warn("Could not delete downloaded file. Please remove it manually: {}", config.getDownloadTarget());
        }
      } else {
        LOGGER.info("Downloaded file is possibly corrupted but won't be removed: {}", config.getDownloadTarget());
      }
      throw e;
    }
  }
}
