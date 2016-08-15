package io.arivera.oss.embedded.rabbitmq.util;

public enum ArchiveType {

  TAR_GZ, TAR_XZ, ZIP;

  public String getExtension() {
    return name().toLowerCase().replace("_", ".");
  }

  public boolean matches(String filesname) {
    return filesname.endsWith(getExtension());
  }
}
