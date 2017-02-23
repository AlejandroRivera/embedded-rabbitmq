package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

/**
 * A list of RabbitMQ versions pre-configured to match the binaries distributed officially by RabbitMQ.
 * <p>
 * Use this enum while building the {@link EmbeddedRabbitMqConfig} instance to specify a version to
 * {@link EmbeddedRabbitMq#start() start}
 *
 * @see io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig.Builder#version(Version)
 */
public enum PredefinedVersion implements Version {

  V3_6_6(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
  V3_6_5(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
  V3_6_4(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
  V3_6_3(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
  V3_6_2(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
  V3_6_1(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
  V3_6_0(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),

  V3_5_7(ArchiveType.TAR_GZ, ArchiveType.ZIP, Constants.V3_5_X_MIN_ERLANG_VERSION),
  V3_5_6(ArchiveType.TAR_GZ, ArchiveType.ZIP, Constants.V3_5_X_MIN_ERLANG_VERSION),
  V3_5_5(ArchiveType.TAR_GZ, ArchiveType.ZIP, Constants.V3_5_X_MIN_ERLANG_VERSION),
  V3_5_4(ArchiveType.TAR_GZ, ArchiveType.ZIP, Constants.V3_5_X_MIN_ERLANG_VERSION),
  V3_5_3(ArchiveType.TAR_GZ, ArchiveType.ZIP, Constants.V3_5_X_MIN_ERLANG_VERSION),
  V3_5_2(ArchiveType.TAR_GZ, ArchiveType.ZIP, Constants.V3_5_X_MIN_ERLANG_VERSION),
  V3_5_1(ArchiveType.TAR_GZ, ArchiveType.ZIP, Constants.V3_5_X_MIN_ERLANG_VERSION),
  V3_5_0(ArchiveType.TAR_GZ, ArchiveType.ZIP, Constants.V3_5_X_MIN_ERLANG_VERSION),

  V3_4_4(ArchiveType.TAR_GZ, ArchiveType.ZIP, Constants.V3_4_X_MIN_ERLANG_VERSION),
  V3_4_3(ArchiveType.TAR_GZ, ArchiveType.ZIP, Constants.V3_4_X_MIN_ERLANG_VERSION),
  V3_4_2(ArchiveType.TAR_GZ, ArchiveType.ZIP, Constants.V3_4_X_MIN_ERLANG_VERSION),
  V3_4_1(ArchiveType.TAR_GZ, ArchiveType.ZIP, Constants.V3_4_X_MIN_ERLANG_VERSION),
  V3_4_0(ArchiveType.TAR_GZ, ArchiveType.ZIP, Constants.V3_4_X_MIN_ERLANG_VERSION),

  LATEST(V3_6_6);

  private static class Constants {
    static final String V3_6_X_MIN_ERLANG_VERSION = "R16B03";
    static final String V3_5_X_MIN_ERLANG_VERSION = "R13B03";
    static final String V3_4_X_MIN_ERLANG_VERSION = null;
  }

  private static final String EXTRACTION_FOLDER = "rabbitmq_server-%s";

  final String version;
  final ArchiveType unixArchiveType;
  final ArchiveType windowsArchiveType;
  final String minErlangVersion;

  PredefinedVersion(ArchiveType unixArchiveType, ArchiveType windowsArchiveType, String minErlangVersion) {
    this.version = name().replaceAll("V", "").replaceAll("_", ".");
    this.unixArchiveType = unixArchiveType;
    this.windowsArchiveType = windowsArchiveType;
    this.minErlangVersion = minErlangVersion;
  }

  PredefinedVersion(PredefinedVersion version) {
    this.version = version.getVersionAsString();
    this.unixArchiveType = version.getArchiveType(OperatingSystem.UNIX);
    this.windowsArchiveType = version.getArchiveType(OperatingSystem.WINDOWS);
    this.minErlangVersion = version.getMinimumErlangVersion();
  }

  @Override
  public String getVersionAsString() {
    return version;
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
