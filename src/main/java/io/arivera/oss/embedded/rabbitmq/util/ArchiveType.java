package io.arivera.oss.embedded.rabbitmq.util;

import java.util.Locale;

public enum ArchiveType {

  TAR_GZ, TAR_XZ, ZIP;

  public String getExtension() {
    return name().toLowerCase(Locale.US).replace("_", ".");
  }

  public boolean matches(String filesname) {
    return filesname.endsWith(getExtension());
  }
}
