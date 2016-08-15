package io.arivera.oss.embedded.rabbitmq.util;

import java.util.Locale;

public enum ArchiveType {

  TAR_GZ, TAR_XZ;

  public String getExtension() {
    return name().toLowerCase(Locale.US).replace("_", ".");
  }
}
