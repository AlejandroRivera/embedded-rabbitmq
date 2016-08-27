package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

/**
 * Class used when user wants to define a custom RabbitMQ version and/or download source.
 */
class CustomVersion implements Version {

  private final String appFolderName;

  CustomVersion(String appFolderName) {
    this.appFolderName = appFolderName;
  }

  @Override
  public String getVersionAsString() {
    throw new RuntimeException("This value isn't needed for custom downloads.");
  }

  @Override
  public ArchiveType getArchiveType(OperatingSystem operatingSystem) {
    throw new RuntimeException("This value isn't needed for custom downloads.");
  }

  @Override
  public String getExtractionFolder() {
    return appFolderName;
  }
}
