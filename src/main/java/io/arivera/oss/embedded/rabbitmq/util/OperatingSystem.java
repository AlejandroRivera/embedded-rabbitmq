package io.arivera.oss.embedded.rabbitmq.util;

public enum OperatingSystem {

  WINDOWS, MAC_OS, UNIX;

  public static OperatingSystem detect() {
    if (SystemUtils.IS_OS_MAC) {
      return MAC_OS;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      return WINDOWS;
    } else {
      return UNIX;
    }
  }

}
