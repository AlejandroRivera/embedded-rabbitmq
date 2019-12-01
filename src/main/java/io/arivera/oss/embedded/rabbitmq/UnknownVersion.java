package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import java.util.List;

/**
 * A class that represents the RabbitMQ Version that is downloaded from the {@link SingleArtifactRepository}.
 * <p>
 * The only thing we care about from the custom download artifact is the folder name where the RabbitMQ installation
 * can be found. We don't care about the version number, the Erlang version required, etc.
 */
class UnknownVersion implements Version {

  private final String appFolderName;

  public UnknownVersion(String appFolderName) {
    this.appFolderName = appFolderName;
  }

  @Override
  public String getExtractionFolder() {
    return appFolderName;
  }

  @Override
  public String getMinimumErlangVersion() {
    return ErlangVersion.UNKNOWN;
  }

  @Override
  public String getVersionAsString() {
    throw new RuntimeException("This value isn't needed for custom downloads.");
  }

  @Override
  public String getVersionAsString(CharSequence separator) {
    throw new RuntimeException("This value isn't needed for custom downloads.");
  }

  @Override
  public ArchiveType getArchiveType(OperatingSystem operatingSystem) {
    throw new RuntimeException("This value isn't needed for custom downloads.");
  }

  @Override
  public List<Integer> getVersionComponents() {
    throw new RuntimeException("This isn't needed for custom downloads.");
  }
}
