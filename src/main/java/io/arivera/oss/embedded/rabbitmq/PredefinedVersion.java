package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import java.util.List;

/**
 * A list of RabbitMQ versions pre-configured to match the binaries distributed officially by RabbitMQ.
 * <p>
 * Use this enum while building the {@link EmbeddedRabbitMqConfig} instance to specify a version to
 * {@link EmbeddedRabbitMq#start() start}
 *
 * @see io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig.Builder#version(Version)
 */
public enum PredefinedVersion implements Version {

  V3_8_0(new BaseVersion("3.8.0", ErlangVersion.V21_3)),

  V3_7_18(new BaseVersion("3.7.18", ErlangVersion.V20_3)),
  V3_7_7(new BaseVersion("3.7.7", ErlangVersion.V19_3_6_4)),
  V3_7_6(new BaseVersion("3.7.6", ErlangVersion.V19_3)),
  V3_7_5(new BaseVersion("3.7.5", ErlangVersion.V19_3)),
  V3_7_4(new BaseVersion("3.7.4", ErlangVersion.V19_3)),
  V3_7_3(new BaseVersion("3.7.3", ErlangVersion.V19_3)),
  V3_7_2(new BaseVersion("3.7.2", ErlangVersion.V19_3)),
  V3_7_1(new BaseVersion("3.7.1", ErlangVersion.V19_3)),
  V3_7_0(new BaseVersion("3.7.0", ErlangVersion.V19_3)),

  V3_6_16(new BaseVersion("3.6.16", ErlangVersion.V19_3)),
  V3_6_15(new BaseVersion("3.6.15", ErlangVersion.V19_3)),
  V3_6_14(new BaseVersion("3.6.14", ErlangVersion.R16B03)),
  V3_6_13(new BaseVersion("3.6.13", ErlangVersion.R16B03)),
  V3_6_12(new BaseVersion("3.6.12", ErlangVersion.R16B03)),
  V3_6_11(new BaseVersion("3.6.11", ErlangVersion.R16B03)),
  V3_6_10(new BaseVersion("3.6.10", ErlangVersion.R16B03)),
  V3_6_9(new BaseVersion("3.6.9", ErlangVersion.R16B03)),
  V3_6_8(new BaseVersion("3.6.8", ErlangVersion.R16B03)),
  V3_6_7(new BaseVersion("3.6.7", ErlangVersion.R16B03)),
  V3_6_6(new BaseVersion("3.6.6", ErlangVersion.R16B03)),
  V3_6_5(new BaseVersion("3.6.5", ErlangVersion.R16B03)),
  V3_6_4(new BaseVersion("3.6.4", ErlangVersion.R16B03)),
  V3_6_3(new BaseVersion("3.6.3", ErlangVersion.R16B03)),
  V3_6_2(new BaseVersion("3.6.2", ErlangVersion.R16B03)),
  V3_6_1(new BaseVersion("3.6.1", ErlangVersion.R16B03)),
  V3_6_0(new BaseVersion("3.6.0", ErlangVersion.R16B03)),

  V3_5_7(new BaseVersion("3.5.7", ErlangVersion.R13B03, ArchiveType.TAR_GZ)),
  V3_5_6(new BaseVersion("3.5.6", ErlangVersion.R13B03, ArchiveType.TAR_GZ)),
  V3_5_5(new BaseVersion("3.5.5", ErlangVersion.R13B03, ArchiveType.TAR_GZ)),
  V3_5_4(new BaseVersion("3.5.4", ErlangVersion.R13B03, ArchiveType.TAR_GZ)),
  V3_5_3(new BaseVersion("3.5.3", ErlangVersion.R13B03, ArchiveType.TAR_GZ)),
  V3_5_2(new BaseVersion("3.5.2", ErlangVersion.R13B03, ArchiveType.TAR_GZ)),
  V3_5_1(new BaseVersion("3.5.1", ErlangVersion.R13B03, ArchiveType.TAR_GZ)),
  V3_5_0(new BaseVersion("3.5.0", ErlangVersion.R13B03, ArchiveType.TAR_GZ)),

  V3_4_4(new BaseVersion("3.4.4", ErlangVersion.UNKNOWN, ArchiveType.TAR_GZ)),
  V3_4_3(new BaseVersion("3.4.3", ErlangVersion.UNKNOWN, ArchiveType.TAR_GZ)),
  V3_4_2(new BaseVersion("3.4.2", ErlangVersion.UNKNOWN, ArchiveType.TAR_GZ)),
  V3_4_1(new BaseVersion("3.4.1", ErlangVersion.UNKNOWN, ArchiveType.TAR_GZ)),
  V3_4_0(new BaseVersion("3.4.0", ErlangVersion.UNKNOWN, ArchiveType.TAR_GZ)),

  LATEST(V3_8_0);

  final Version version;

  PredefinedVersion(Version version) {
    this.version = version;
  }

  @Override
  public List<Integer> getVersionComponents() {
    return version.getVersionComponents();
  }

  @Override
  public String getVersionAsString() {
    return version.getVersionAsString();
  }

  @Override
  public String getVersionAsString(CharSequence separator) {
    return version.getVersionAsString(separator);
  }

  @Override
  public ArchiveType getArchiveType(OperatingSystem operatingSystem) {
    return version.getArchiveType(operatingSystem);
  }

  @Override
  public String getExtractionFolder() {
    return version.getExtractionFolder();
  }

  @Override
  public String getMinimumErlangVersion() {
    return version.getMinimumErlangVersion();
  }

}
