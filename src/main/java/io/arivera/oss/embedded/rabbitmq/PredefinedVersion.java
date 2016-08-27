package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

public enum PredefinedVersion implements Version {

  V3_6_5(ArchiveType.TAR_XZ, ArchiveType.ZIP),
  V3_6_4(ArchiveType.TAR_XZ, ArchiveType.ZIP),
  V3_6_3(ArchiveType.TAR_XZ, ArchiveType.ZIP),
  V3_6_2(ArchiveType.TAR_XZ, ArchiveType.ZIP),
  V3_6_1(ArchiveType.TAR_XZ, ArchiveType.ZIP),
  V3_6_0(ArchiveType.TAR_XZ, ArchiveType.ZIP),

  V3_5_7(ArchiveType.TAR_GZ, ArchiveType.ZIP),
  V3_5_6(ArchiveType.TAR_GZ, ArchiveType.ZIP),
  V3_5_5(ArchiveType.TAR_GZ, ArchiveType.ZIP),
  V3_5_4(ArchiveType.TAR_GZ, ArchiveType.ZIP),
  V3_5_3(ArchiveType.TAR_GZ, ArchiveType.ZIP),
  V3_5_2(ArchiveType.TAR_GZ, ArchiveType.ZIP),
  V3_5_1(ArchiveType.TAR_GZ, ArchiveType.ZIP),
  V3_5_0(ArchiveType.TAR_GZ, ArchiveType.ZIP),

  V3_4_4(ArchiveType.TAR_GZ, ArchiveType.ZIP),
  V3_4_3(ArchiveType.TAR_GZ, ArchiveType.ZIP),
  V3_4_2(ArchiveType.TAR_GZ, ArchiveType.ZIP),
  V3_4_1(ArchiveType.TAR_GZ, ArchiveType.ZIP),
  V3_4_0(ArchiveType.TAR_GZ, ArchiveType.ZIP),

  LATEST(V3_6_5, ArchiveType.TAR_XZ, ArchiveType.ZIP),
  ;

  private static final String EXTRACTION_FOLDER = "rabbitmq_server-%s";

  final String version;
  final ArchiveType unixArchiveType;
  final ArchiveType windowsArchiveType;

  PredefinedVersion(ArchiveType unixArchiveType, ArchiveType windowsArchiveType) {
    this.version = name().replaceAll("V", "").replaceAll("_", ".");
    this.unixArchiveType = unixArchiveType;
    this.windowsArchiveType = windowsArchiveType;
  }

  PredefinedVersion(PredefinedVersion version, ArchiveType unixArchiveType, ArchiveType windowsArchiveType) {
    this.version = version.getVersionAsString();
    this.unixArchiveType = unixArchiveType;
    this.windowsArchiveType = windowsArchiveType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getVersionAsString(){
    return version;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ArchiveType getArchiveType(OperatingSystem operatingSystem) {
    return operatingSystem == OperatingSystem.WINDOWS ? windowsArchiveType : unixArchiveType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getExtractionFolder() {
    return String.format(EXTRACTION_FOLDER, this.getVersionAsString());
  }

}
