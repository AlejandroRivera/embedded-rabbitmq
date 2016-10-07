package io.arivera.oss.embedded.rabbitmq.helpers;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.bin.ErlangShell;
import io.arivera.oss.embedded.rabbitmq.bin.ErlangShellException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that helps enforce the existence and version requirements of Erlang to run RabbitMQ.
 */
public class ErlangVersionChecker {

  private static final Logger LOGGER = LoggerFactory.getLogger(ErlangVersionChecker.class);

  private final ErlangShell erlangShell;
  private final String minErlangVersion;

  public ErlangVersionChecker(EmbeddedRabbitMqConfig config) {
    this(config.getVersion().getMinimumErlangVersion(), new ErlangShell(config));
  }

  public ErlangVersionChecker(String minErlangVersion, ErlangShell erlangShell) {
    this.minErlangVersion = minErlangVersion;
    this.erlangShell = erlangShell;
  }

  /**
   * Retrieves the current system's Erlang version to compare it to the minimum required version.
   * <p>
   * The system's Erlang version is always retrieved, but the comparison might be skipped if the RabbitMQ version
   * doesn't specify a minimum required version.
   *
   * @throws ErlangVersionException if the minimum required version is not met or if it can't be determined.
   */
  public void check() throws ErlangVersionException {
    String erlangVersion;
    try {
      erlangVersion = erlangShell.getErlangVersion();
      LOGGER.debug("Erlang version installed in this system: {}", erlangVersion);
    } catch (ErlangShellException e) {
      throw new ErlangVersionException("Could not determine Erlang version. Ensure Erlang is correctly installed.", e);
    }

    if (minErlangVersion == null) {
      LOGGER.debug("RabbitMQ version to execute doesn't specify a minimum Erlang version. Will skip this check.");
      return;
    } else {
      LOGGER.debug("RabbitMQ version to execute requires Erlang version {} or above.", minErlangVersion);
    }

    double expected;
    double actual;
    try {
      expected = parse(minErlangVersion);
      actual = parse(erlangVersion);
    } catch (RuntimeException e) {
      LOGGER.warn("Error parsing Erlang version: " + minErlangVersion + " or " + erlangVersion + ". Ignoring check...");
      return;
    }

    if (actual < expected) {
      throw new ErlangVersionException(
          String.format("Minimum required Erlang version not found. Expected '%s' or higher. Actual is: '%s'",
              minErlangVersion, erlangVersion));
    }
  }

  /**
   * @return a numeric value useful for comparing versions.
   */
  static double parse(String erlangVersion) {
    int major;
    int minor;
    int patch;
    if (erlangVersion.startsWith("R")) {
      erlangVersion = erlangVersion.substring(1);                     // "R15B03-1" -> "15B03-1"
      String[] components = erlangVersion.split("\\D", 2);            // "15B03-1" -> ["15", "03-1"]
      major = Integer.parseInt(components[0]);                        // "15" -> 15
      minor = erlangVersion.replaceAll("[^A-Z]", "").charAt(0);       // "15B03-1-" -> "B" -> 66
      if (components.length >= 2
          && !components[1].isEmpty()
          && components[1].indexOf("-") != 0) {
        patch = Integer.parseInt(components[1].split("-", 2)[0]);     // "03-1" -> "03" -> 3
      } else {
        patch = 0;
      }
    } else {
      String[] components = erlangVersion.split("\\.", 3);
      major = Integer.parseInt(components[0]);
      minor = components.length > 1 ? Integer.parseInt(components[1]) : 0;
      patch = components.length > 2 ? Integer.parseInt(components[2]) : 0;
    }
    return Double.valueOf(String.format("%d.%03d0%03d", major, minor, patch));
  }
}
