package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;

public enum PredefinedVersion implements Version {

  LATEST("3.6.5", ArchiveType.TAR_XZ),

  V3_6_5(ArchiveType.TAR_XZ),
  V3_6_4(ArchiveType.TAR_XZ),
  V3_6_3(ArchiveType.TAR_XZ),
  V3_6_2(ArchiveType.TAR_XZ),
  V3_6_1(ArchiveType.TAR_XZ),
  V3_6_0(ArchiveType.TAR_XZ),

  V3_5_7(ArchiveType.TAR_GZ),
  V3_5_6(ArchiveType.TAR_GZ),
  V3_5_5(ArchiveType.TAR_GZ),
  V3_5_4(ArchiveType.TAR_GZ),
  V3_5_3(ArchiveType.TAR_GZ),
  V3_5_2(ArchiveType.TAR_GZ),
  V3_5_1(ArchiveType.TAR_GZ),
  V3_5_0(ArchiveType.TAR_GZ),

  V3_4_4(ArchiveType.TAR_GZ),
  V3_4_3(ArchiveType.TAR_GZ),
  V3_4_2(ArchiveType.TAR_GZ),
  V3_4_1(ArchiveType.TAR_GZ),
  V3_4_0(ArchiveType.TAR_GZ),

  ;

  private final String version;
  private final ArchiveType unixArchiveType;
  private final ArchiveType windowsArchiveType;

  PredefinedVersion(ArchiveType unixArchiveType) {
    this.version = name().replaceAll("V", "").replaceAll("_", ".");
    this.unixArchiveType = unixArchiveType;
    this.windowsArchiveType = ArchiveType.ZIP;
  }

  PredefinedVersion(String version, ArchiveType unixArchiveType) {
    this.version = version;
    this.unixArchiveType = unixArchiveType;
    this.windowsArchiveType = ArchiveType.ZIP;
  }

  @Override
  public DefaultPathProvider getPathsProvider() {
    return new DefaultPathProvider(version, unixArchiveType, windowsArchiveType);
  }

}
