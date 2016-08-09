package io.arivera.oss.embedded.rabbitmq.util;

public enum OperatingSystem {

  WINDOWS("windows"), MAC_OS("mac-standalone"), UNIX("generic-unix");

  public static OperatingSystem detect() {
    if (SystemUtils.IS_OS_MAC) {
      return MAC_OS;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      return WINDOWS;
    } else {
      return UNIX;
    }
  }

  private String downloadName;

  OperatingSystem(String downloadName) {
    this.downloadName = downloadName;
  }

  public String getDownloadNameComponent() {
    return downloadName;
  }
}
