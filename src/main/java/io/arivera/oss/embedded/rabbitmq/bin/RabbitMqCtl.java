package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * This is a helper class meant to facilitate invoking commands from {@code rabbitmqctl}.
 * <p>
 * The methods contained in this class aren't exhaustive. Please refer to the manual for a complete list.
 *
 * @see <a href="https://www.rabbitmq.com/rabbitmqctl.8.html">rabbitmqctl(8) manual page</a>
 */
public class RabbitMqCtl extends RabbitMqDiagnostics {

  public static final String COMMAND = "rabbitmqctl";

  public RabbitMqCtl(EmbeddedRabbitMqConfig config) {
    super(config);
  }

  public RabbitMqCtl(EmbeddedRabbitMqConfig config, Map<String, String> extraEnvVars) {
    super(config, extraEnvVars);
  }

  public RabbitMqCtl(EmbeddedRabbitMqConfig config, Set<String> envVarsToDiscard, Map<String, String> envVarsToAdd) {
    super(config, envVarsToDiscard, envVarsToAdd);
  }

  public RabbitMqCtl(RabbitMqCommand.ProcessExecutorFactory processExecutorFactory, File appFolder,
                     Map<String, String> envVars) {
    super(processExecutorFactory, appFolder, envVars);
  }

  /**
   * Stops the Erlang node on which RabbitMQ is running.
   */
  public Future<ProcessResult> stop() throws RabbitMqCommandException {
    return execute("stop");
  }

  /**
   * Stops the RabbitMQ application, leaving the Erlang node running.
   * <p>
   * This command is typically run prior to performing other management actions that require the
   * RabbitMQ application to be stopped, e.g. {@link #reset()}.
   */
  public Future<ProcessResult> stopApp() throws RabbitMqCommandException {
    return execute("stop_app");
  }

  /**
   * Starts the RabbitMQ application.
   * <p>
   * This command is typically run after performing other management actions that required the
   * RabbitMQ application to be stopped, e.g. {@link #reset()}.
   */
  public Future<ProcessResult> startApp() throws RabbitMqCommandException {
    return execute("start_app");
  }

  /**
   * Return a RabbitMQ node to its virgin state.
   * <p>
   * Removes the node from any cluster it belongs to, removes all data from the management database,
   * such as configured users and vhosts, and deletes all persistent messages.
   * <p>
   * For reset and force_reset to succeed the RabbitMQ application must have been stopped, e.g. with {@link #stopApp()}
   */
  public Future<ProcessResult> reset() throws RabbitMqCommandException {
    return execute("reset");
  }

  /**
   * Forcefully return a RabbitMQ node to its virgin state.
   * <p>
   * The force_reset command differs from reset in that it resets the node unconditionally,
   * regardless of the current management database state and cluster configuration.
   * It should only be used as a last resort if the database or cluster configuration has been corrupted.
   * <p>
   * For reset and force_reset to succeed the RabbitMQ application must have been stopped, e.g. with {@link #stopApp()}
   */
  public Future<ProcessResult> forceReset() throws RabbitMqCommandException {
    return execute("force_reset");
  }

  @Override
  protected String getCommand() {
    return COMMAND;
  }
}
