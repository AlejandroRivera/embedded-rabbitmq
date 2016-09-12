package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import java.net.URL;

/**
 * Class used to allow for the user to specify a custom repository to download the RabbitMQ binary from.
 *
 * @see EmbeddedRabbitMqConfig.Builder#downloadFrom(ArtifactRepository)
 * @see EmbeddedRabbitMqConfig.Builder#downloadFrom(URL, String)
 */
class CustomArtifactRepository implements ArtifactRepository {

  private final URL downloadSource;

  CustomArtifactRepository(URL downloadSource) {
    this.downloadSource = downloadSource;
  }

  @Override
  public URL getUrl(Version version, OperatingSystem operatingSystem) {
    return downloadSource;
  }
}
