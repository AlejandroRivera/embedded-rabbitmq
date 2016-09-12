package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import java.net.URL;

/**
 * @see EmbeddedRabbitMqConfig.Builder#downloadFrom(ArtifactRepository)
 */
public interface ArtifactRepository {

  /**
   * @return a valid URL specific to the Version and Operating System from where to download the RabbitMQ.
   */
  URL getUrl(Version version, OperatingSystem operatingSystem);

}