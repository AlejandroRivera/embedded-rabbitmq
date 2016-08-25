package io.arivera.oss.embedded.rabbitmq.extract;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

public class ExtractorFactory {

  /**
   * Returns an Extractor instance appropriate based on the given configuration.
   */
  public static Extractor getNewInstance(EmbeddedRabbitMqConfig config) {
    Extractor extractor = new BasicExtractor(config);
    if (config.shouldCachedDownload()) {
      extractor = new CachedExtractor(extractor, config);
    }
    return extractor;
  }

}
