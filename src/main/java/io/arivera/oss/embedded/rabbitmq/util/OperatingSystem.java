package io.arivera.oss.embedded.rabbitmq.util;

import io.arivera.oss.embedded.rabbitmq.apache.commons.lang3.SystemUtils;

public enum OperatingSystem {

  WINDOWS, MAC_OS, UNIX;

  /**
   * Returns the right instance of the Operation System.
   *
   * @see SystemUtils#IS_OS_MAC
   * @see SystemUtils#IS_OS_WINDOWS
   */
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
