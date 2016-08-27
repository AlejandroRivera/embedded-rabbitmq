package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import java.net.URL;

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
