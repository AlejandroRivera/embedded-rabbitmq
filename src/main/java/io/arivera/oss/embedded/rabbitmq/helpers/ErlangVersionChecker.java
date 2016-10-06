package io.arivera.oss.embedded.rabbitmq.helpers;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.bin.ErlangShell;
import io.arivera.oss.embedded.rabbitmq.bin.ErlangShellException;

public class ErlangVersionChecker {

  private final EmbeddedRabbitMqConfig config;

  public ErlangVersionChecker(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  public void check() throws ErlangVersionException {
    try {
      String erlangVersion = new ErlangShell(config).getErlangVersion();
      double parse = parse(erlangVersion);
      // TODO: define min. version req.
      if (parse < 14) {
        throw new ErlangVersionException("Minimum Erlang version not present. Instead: " + erlangVersion);
      }
    } catch (ErlangShellException e) {
      throw new ErlangVersionException("Could not determine Erlang version.", e);
    }
  }

  private double parse(String erlangVersion) {
    // TODO: do proper parsing.
    return 42;
  }
}
