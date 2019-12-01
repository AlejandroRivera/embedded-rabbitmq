package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;
import io.arivera.oss.embedded.rabbitmq.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used when user wants to use a RabbitMQ version that's not defined in {@link PredefinedVersion} but that
 * still follows the binary artifact conventions.
 *
 * @see EmbeddedRabbitMqConfig.Builder#version(Version)
 */
public class BaseVersion implements Version {

  private static final String EXTRACTION_FOLDER = "rabbitmq_server-%s";

  final List<Integer> versionComponents;
  final ArchiveType unixArchiveType;
  final ArchiveType windowsArchiveType;
  final String minErlangVersion;

  public BaseVersion(String semanticVersion) {
    this(semanticVersion, ErlangVersion.UNKNOWN);
  }

  public BaseVersion(String semanticVersion, String minErlangVersion) {
    this(semanticVersion, minErlangVersion, ArchiveType.TAR_XZ);
  }

  public BaseVersion(String semanticVersion, String minErlangVersion, ArchiveType unixArchiveType) {
    this(semanticVersion, minErlangVersion, unixArchiveType, ArchiveType.ZIP);
  }

  /**
   * @param semanticVersion The semantic version in a string format, like {@code "3.8.0"}
   * @param minErlangVersion The minimum version of Erlang required to execute this version of RabbitMQ
   *                         or @{code null} if no version check should be performed.
   * @param unixArchiveType The type of packaging used for the Unix/Mac binaries, typically {@link ArchiveType#TAR_XZ}
   * @param windowsArchiveType The type of packaging used for Windows binaries, typically {@link ArchiveType#ZIP}
   *
   * @see <a href="https://www.rabbitmq.com/which-erlang.html">RabbitMQ Erlang Version Requirements</a>
   */
  public BaseVersion(String semanticVersion, String minErlangVersion,
                     ArchiveType unixArchiveType, ArchiveType windowsArchiveType) {
    String[] versionComponents = semanticVersion.split("\\.");
    this.versionComponents = new ArrayList<>(versionComponents.length);
    for (String versionComponent : versionComponents) {
      this.versionComponents.add(Integer.parseInt(versionComponent));
    }
    this.unixArchiveType = unixArchiveType;
    this.windowsArchiveType = windowsArchiveType;
    this.minErlangVersion = minErlangVersion;
  }

  @Override
  public List<Integer> getVersionComponents() {
    return versionComponents;
  }

  @Override
  public String getVersionAsString() {
    return getVersionAsString(".");
  }

  @Override
  public String getVersionAsString(CharSequence separator) {
    return StringUtils.join(versionComponents, separator);
  }

  @Override
  public ArchiveType getArchiveType(OperatingSystem operatingSystem) {
    return operatingSystem == OperatingSystem.WINDOWS ? windowsArchiveType : unixArchiveType;
  }

  @Override
  public String getExtractionFolder() {
    return String.format(EXTRACTION_FOLDER, this.getVersionAsString());
  }

  @Override
  public String getMinimumErlangVersion() {
    return minErlangVersion;
  }

}
