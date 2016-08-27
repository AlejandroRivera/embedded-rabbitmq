package io.arivera.oss.embedded.rabbitmq.extract;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

public class ExtractorFactory {

  private EmbeddedRabbitMqConfig config;

  public ExtractorFactory(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  /**
   * Returns an Extractor instance appropriate based on the given configuration.
   */
  public Extractor getNewInstance() {
    Extractor extractor = new BasicExtractor(config);
    if (config.shouldCachedDownload()) {
      extractor = new CachedExtractor(extractor, config);
    }
    return extractor;
  }

}
