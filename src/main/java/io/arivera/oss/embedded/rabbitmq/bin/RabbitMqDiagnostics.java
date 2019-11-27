package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * This is a helper class meant to facilitate invoking commands from {@code rabbitmq-diagnostics}.
 *
 * @see <a href="https://www.rabbitmq.com/rabbitmq-diagnostics.8.html">rabbitmq-diagnostics(8) manual page</a>
 */
public class RabbitMqDiagnostics {

  /**
   * Default list of Environment Variables to discard from {@link EmbeddedRabbitMqConfig#getEnvVars()}, as
   * it is known that some of them cause conflicts when executing commands.
   */
  private static final Set<String> DEFAULT_ENV_VARS_TO_DISCARD = new HashSet<>(Arrays.asList("RABBITMQ_NODE_PORT"));
  private static final String COMMAND = "rabbitmq-diagnostics";

  private RabbitMqCommand.ProcessExecutorFactory peFactory;
  private File appFolder;
  private Map<String, String> envVars;

  public RabbitMqDiagnostics(EmbeddedRabbitMqConfig config) {
    this(config, Collections.EMPTY_MAP);
  }

  /**
   * A constructor that allows additional env vars while also discarding default known vars that cause issues.
   *
   * @see #DEFAULT_ENV_VARS_TO_DISCARD
   */
  public RabbitMqDiagnostics(EmbeddedRabbitMqConfig config, Map<String, String> extraEnvVars) {
    this(config, DEFAULT_ENV_VARS_TO_DISCARD, extraEnvVars);
  }

  /**
   * A constructor that allows additional env vars while also allowing to override the default env vars to discard.
   *
   * @see #DEFAULT_ENV_VARS_TO_DISCARD
   */
  public RabbitMqDiagnostics(EmbeddedRabbitMqConfig config,
                             Set<String> envVarsToDiscard,
                             Map<String, String> envVarsToAdd) {
    this(config.getProcessExecutorFactory(), config.getAppFolder(),
        mapFilterAndAppend(config.getEnvVars(), envVarsToDiscard, envVarsToAdd));
  }

  /**
   * Full-fledged constructor.
   */
  public RabbitMqDiagnostics(RabbitMqCommand.ProcessExecutorFactory processExecutorFactory,
                             File appFolder, Map<String, String> envVars) {
    this.peFactory = processExecutorFactory;
    this.appFolder = appFolder;
    this.envVars = envVars;
  }

  protected static Map<String, String> mapFilterAndAppend(Map<String, String> envVars,
                                                          Set<String> envVarsToDiscard,
                                                          Map<String, String> envVarsToAdd) {
    Map<String, String> tmpEnvVars = envVars;
    if (!envVarsToDiscard.isEmpty() || envVarsToAdd.isEmpty()) {
      tmpEnvVars = new HashMap<>(tmpEnvVars);
      for (String var : envVarsToDiscard) {
        tmpEnvVars.remove(var);
      }
      tmpEnvVars.putAll(envVarsToAdd);
    }
    return tmpEnvVars;
  }

  /**
   * This method exposes a way to invoke a command with any arguments. This is useful when the class methods
   * don't expose the desired functionality.
   * <p>
   * For example:
   * <pre><code>
   * RabbitMqDiagnostics command = new RabbitMqDiagnostics(config);
   * command.execute("list_users");
   * </code></pre>
   */
  public Future<ProcessResult> execute(String... arguments) throws RabbitMqCommandException {
    return new RabbitMqCommand(peFactory, envVars, appFolder, getCommand(), arguments)
        .call()
        .getFuture();
  }

  protected String getCommand() {
    return COMMAND;
  }

}
