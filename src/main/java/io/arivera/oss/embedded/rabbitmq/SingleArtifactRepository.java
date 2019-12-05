package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import java.net.URL;

/**
 * Class used to allow for the user to specify a custom URL to download the RabbitMQ binary from.
 * <p>
 * Since this is basically a hardcoded URL, there's no ability to change the artifact to be downloaded based on
 * the OS the system is currently running. Use a {@link BaseVersion} if that capability is needed.
 *
 * @see EmbeddedRabbitMqConfig.Builder#downloadFrom(ArtifactRepository)
 * @see EmbeddedRabbitMqConfig.Builder#downloadFrom(URL, String)
 */
class SingleArtifactRepository implements ArtifactRepository {

  private final URL downloadSource;

  SingleArtifactRepository(URL downloadSource) {
    this.downloadSource = downloadSource;
  }

  @Override
  public URL getUrl(Version version, OperatingSystem operatingSystem) {
    return downloadSource;
  }

}
