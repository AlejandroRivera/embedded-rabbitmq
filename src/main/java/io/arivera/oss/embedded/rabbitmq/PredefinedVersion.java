package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;
import io.arivera.oss.embedded.rabbitmq.util.StringUtils;

import java.util.ArrayList;
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

  V3_8_0(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_8_0_MIN_ERLANG_VERSION),
  V3_7_18(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_7_18_MIN_ERLANG_VERSION),
  V3_7_7(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_7_7_MIN_ERLANG_VERSION),
  V3_7_6(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_7_X_MIN_ERLANG_VERSION),
  V3_7_5(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_7_X_MIN_ERLANG_VERSION),
  V3_7_4(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_7_X_MIN_ERLANG_VERSION),
  V3_7_3(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_7_X_MIN_ERLANG_VERSION),
  V3_7_2(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_7_X_MIN_ERLANG_VERSION),
  V3_7_1(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_7_X_MIN_ERLANG_VERSION),
  V3_7_0(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_7_X_MIN_ERLANG_VERSION),

  V3_6_16(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_15_MIN_ERLANG_VERSION),
  V3_6_15(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_15_MIN_ERLANG_VERSION),
  V3_6_14(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
  V3_6_13(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
  V3_6_12(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
  V3_6_11(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
  V3_6_10(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
  V3_6_9(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
  V3_6_8(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
  V3_6_7(ArchiveType.TAR_XZ, ArchiveType.ZIP, Constants.V3_6_X_MIN_ERLANG_VERSION),
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

  LATEST(V3_8_0);

  private static class Constants {
    static final String V3_8_0_MIN_ERLANG_VERSION = "21.3";
    static final String V3_7_18_MIN_ERLANG_VERSION = "20.3";
    static final String V3_7_7_MIN_ERLANG_VERSION = "19.3.6.4";
    static final String V3_7_X_MIN_ERLANG_VERSION = "19.3";
    static final String V3_6_15_MIN_ERLANG_VERSION = "19.3";
    static final String V3_6_X_MIN_ERLANG_VERSION = "R16B03";
    static final String V3_5_X_MIN_ERLANG_VERSION = "R13B03";
    static final String V3_4_X_MIN_ERLANG_VERSION = null;
  }

  private static final String EXTRACTION_FOLDER = "rabbitmq_server-%s";

  final List<Integer> versionComponents;
  final ArchiveType unixArchiveType;
  final ArchiveType windowsArchiveType;
  final String minErlangVersion;

  PredefinedVersion(ArchiveType unixArchiveType, ArchiveType windowsArchiveType, String minErlangVersion) {
    String[] versionComponents = name().replaceAll("V", "").split("_");
    this.versionComponents = new ArrayList<>(versionComponents.length);
    for (int i = 0; i < versionComponents.length; i++) {
      this.versionComponents.add(Integer.parseInt(versionComponents[i]));
    }
    this.unixArchiveType = unixArchiveType;
    this.windowsArchiveType = windowsArchiveType;
    this.minErlangVersion = minErlangVersion;
  }

  PredefinedVersion(PredefinedVersion version) {
    this.versionComponents =  version.getVersionComponents();
    this.unixArchiveType = version.getArchiveType(OperatingSystem.UNIX);
    this.windowsArchiveType = version.getArchiveType(OperatingSystem.WINDOWS);
    this.minErlangVersion = version.getMinimumErlangVersion();
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
