package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import java.net.URL;

public interface ArtifactRepository {

  URL getUrl(Version version, OperatingSystem operatingSystem);

}