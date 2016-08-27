package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

public interface Version {

  /**
   * @return a String formatted like {@code "3.6.5"}
   */
  String getVersionAsString();

  /**
   * @return the correct Archive Type for the given OS
   */
  ArchiveType getArchiveType(OperatingSystem operatingSystem);

  /**
   * @return the folder name under which the compressed application is extracted to.
   */
  String getExtractionFolder();

}
