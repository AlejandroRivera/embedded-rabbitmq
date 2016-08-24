package io.arivera.oss.embedded.rabbitmq.extract;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

public class ExtractorFactory {

  /**
   * Returns an Extractor instance appropriate based on the given configuration.
   */
  public static Runnable getNewInstance(EmbeddedRabbitMqConfig config) {
    Runnable extractor = new Extractor(config);
    if (config.shouldCachedDownload()) {
      extractor = new CachedExtractor(extractor, config);
    }
    return extractor;
  }

}
