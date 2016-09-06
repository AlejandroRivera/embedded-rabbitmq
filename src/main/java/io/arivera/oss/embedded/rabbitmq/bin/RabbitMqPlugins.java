package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.bin.plugins.PluginDetails;

import org.zeroturnaround.exec.ProcessResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A wrapper around the RabbitMqCommand to execute '{@value #COMMAND}' commands.
 *
 * @see <a href="https://www.rabbitmq.com/man/rabbitmq-plugins.1.man.html">rabbitmq-plugins(1) manual page</a>
 */
public class RabbitMqPlugins {

  private static final String LIST = "list";
  private static final String COMMAND = "rabbitmq-plugins";

  private final EmbeddedRabbitMqConfig config;

  public RabbitMqPlugins(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  /**
   * This method exposes a way to invoke '{@value COMMAND}' command with any arguments.
   * This is useful when the class methods don't expose the desired functionality.
   * <p>
   * For example:
   * <pre><code>
   * RabbitMqPlugins command = new RabbitMqPlugins(config);
   * command.execute("list", "-v", "management");
   * </code></pre>
   *
   * @throws RabbitMqCommandException if the command cannot be executed
   */
  public Future<ProcessResult> execute(String... arguments) throws RabbitMqCommandException {
    return new RabbitMqCommand(config, COMMAND, arguments)
        .call()
        .getFuture();
  }

  /**
   * Executes the {@code rabbitmq-plugins list} command and returns a grouped representation of the parsed output.
   *
   * @throws RabbitMqCommandException if the command cannot be executed, it doesn't {@link EmbeddedRabbitMqConfig.Builder#defaultRabbitMqCtlTimeoutInMillis(long)
   *                                  finish in time} or exits unexpectedly
   */
  public Map<PluginDetails.PluginState, Set<PluginDetails>> list() throws RabbitMqCommandException {
    String[] args = {LIST};
    String executionErrorMessage = String.format("Error executing: %s %s", COMMAND, LIST);
    String unexpectedExitCodeMessage = "Listing of plugins failed with exit code: ";

    ProcessResult processResult = getProcessResult(args, executionErrorMessage, unexpectedExitCodeMessage);

    List<String> lines = processResult.getOutput().getLinesAsUTF8();
    return parseListOutput(lines);
  }

  /**
   * Executes the command {@code rabbitmq-plugins enable {plugin}} and blocks until the call finishes.
   *
   * @param plugin the name of the plugin to enable.
   *
   * @throws RabbitMqCommandException if the command cannot be executed, it doesn't {@link EmbeddedRabbitMqConfig.Builder#defaultRabbitMqCtlTimeoutInMillis(long)
   *                                  finish in time} or exits unexpectedly
   */
  public void enable(String plugin) throws RabbitMqCommandException {
    String[] args = {"enable", plugin};
    String executionErrorMessage = "Error while enabling plugin '" + plugin + "'";
    String unexpectedExitCodeMessage = "Enabling of plugin '" + plugin + "' failed with exit code: ";

    getProcessResult(args, executionErrorMessage, unexpectedExitCodeMessage);
  }

  /**
   * Disables the given plugin by executing {@code rabbitmq-plugins disable {plugin}} and blocks until the call is
   * finished.
   *
   * @param plugin the name of the plugin to disable.
   *
   * @throws RabbitMqCommandException if the command cannot be executed, it doesn't {@link EmbeddedRabbitMqConfig.Builder#defaultRabbitMqCtlTimeoutInMillis(long)
   *                                  finish in time} or exits unexpectedly
   */
  public void disable(String plugin) throws RabbitMqCommandException {
    String[] args = {"disable", plugin};
    String executionErrorMessage = "Error while disabling plugin '" + plugin + "'";
    String unexpectedExitCodeMessage = "Disabling of plugin '" + plugin + "' failed with exit code: ";

    getProcessResult(args, executionErrorMessage, unexpectedExitCodeMessage);
  }

  private ProcessResult getProcessResult(String[] args, String executionErrorMessage, String unexpectedExitCodeMessage) {
    ProcessResult processResult;
    try {
      Future<ProcessResult> startedProcess = execute(args);
      processResult = startedProcess.get(config.getDefaultRabbitMqCtlTimeoutInMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RabbitMqCommandException(executionErrorMessage, e);
    }

    int exitValue = processResult.getExitValue();
    if (exitValue != 0) {
      throw new RabbitMqCommandException(unexpectedExitCodeMessage + exitValue);
    }
    return processResult;
  }

  Map<PluginDetails.PluginState, Set<PluginDetails>> parseListOutput(List<String> lines) {
    Map<PluginDetails.PluginState, Set<PluginDetails>> groupedPlugins = new HashMap<>();
    for (PluginDetails.PluginState state : PluginDetails.PluginState.values()) {
      groupedPlugins.put(state, new TreeSet<PluginDetails>());
    }

    for (String line : lines) {
      PluginDetails pluginDetails = PluginDetails.fromString(line);
      if (pluginDetails != null) {
        for (PluginDetails.PluginState pluginState : pluginDetails.getState()) {
          groupedPlugins.get(pluginState).add(pluginDetails);
        }
      }
    }
    return groupedPlugins;
  }

}
