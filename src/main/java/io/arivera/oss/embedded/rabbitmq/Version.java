package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

/**
 * A class that provides information about a specific distribution artifact version of RabbitMQ.
 */
public interface Version {

  /**
   * @return a String formatted like {@code "3.6.5"}
   */
  String getVersionAsString();

  /**
   * @return correct Archive Type for the given OS.
   */
  ArchiveType getArchiveType(OperatingSystem operatingSystem);

  /**
   * @return folder name under which the compressed application is extracted to.
   */
  String getExtractionFolder();

  /**
   * @return minimum version required to run this RabbitMQ version. Example: {@code "R16B3"} or {code null}
   */
  String getMinimumErlangVersion();

}
