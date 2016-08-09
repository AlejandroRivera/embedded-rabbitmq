package io.arivera.oss.embedded.rabbitmq.util;

public enum ArchiveType {

  TAR_GZ, TAR_XZ;

  public String getExtension(){
    return name().toLowerCase().replace("_", ".");
  }
}
