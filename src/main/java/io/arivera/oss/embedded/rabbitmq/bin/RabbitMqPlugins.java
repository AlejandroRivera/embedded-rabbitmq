package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.bin.plugins.Plugin;

import org.zeroturnaround.exec.ProcessResult;

import java.util.Collection;
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
 * A wrapper around the RabbitMqCommand to execute '{@value #EXECUTABLE}' commands.
 *
 * @see <a href="https://www.rabbitmq.com/man/rabbitmq-plugins.1.man.html">rabbitmq-plugins(1) manual page</a>
 */
public class RabbitMqPlugins {

  private static final String LIST_COMMAND = "list";
  private static final String EXECUTABLE = "rabbitmq-plugins";

  private final EmbeddedRabbitMqConfig config;

  public RabbitMqPlugins(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  /**
   * This method exposes a way to invoke '{@value EXECUTABLE}' command with any arguments.
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
    return new RabbitMqCommand(config, EXECUTABLE, arguments)
        .call()
        .getFuture();
  }

  /**
   * Same as {@link #list()} but it returns the plugins grouped by state.
   */
  public Map<Plugin.State, Set<Plugin>> groupedList() throws RabbitMqCommandException {
    Collection<Plugin> plugins = list().values();
    return groupPluginsByState(plugins);
  }

  private Map<Plugin.State, Set<Plugin>> groupPluginsByState(Collection<Plugin> plugins) {
    Map<Plugin.State, Set<Plugin>> groupedPlugins = new HashMap<>();
    for (Plugin.State state : Plugin.State.values()) {
      groupedPlugins.put(state, new TreeSet<Plugin>());
    }

    for (Plugin plugin : plugins) {
      for (Plugin.State state : plugin.getState()) {
        groupedPlugins.get(state).add(plugin);
      }
    }
    return groupedPlugins;
  }

  /**
   * Executes the {@code rabbitmq-plugins list} command
   *
   * @return a Map where the key is the plugin name and the value is the full plugin details parsed from the output.
   *
   * @throws RabbitMqCommandException if the command cannot be executed, it doesn't
   *                                  {@link EmbeddedRabbitMqConfig.Builder#defaultRabbitMqCtlTimeoutInMillis(long)
   *                                  finish in time} or exits unexpectedly
   * @see #groupedList()
   */
  public Map<String, Plugin> list() {
    String[] args = {LIST_COMMAND};
    String executionErrorMessage = String.format("Error executing: %s %s", EXECUTABLE, LIST_COMMAND);
    String unexpectedExitCodeMessage = "Listing of plugins failed with exit code: ";

    ProcessResult processResult = getProcessResult(args, executionErrorMessage, unexpectedExitCodeMessage);

    List<Plugin> plugins = parseListOutput(processResult);
    Map<String, Plugin> result = mapPluginsByName(plugins);
    return result;
  }

  private List<Plugin> parseListOutput(ProcessResult processResult) {
    List<String> lines = processResult.getOutput().getLinesAsUTF8();
    return Plugin.fromStrings(lines);
  }

  private Map<String, Plugin> mapPluginsByName(List<Plugin> plugins) {
    Map<String, Plugin> result = new HashMap<>(plugins.size());
    for (Plugin plugin : plugins){
      result.put(plugin.getName(), plugin);
    }
    return result;
  }

  /**
   * Executes the command {@code rabbitmq-plugins enable {plugin}} and blocks until the call finishes.
   *
   * @param plugin the name of the plugin to enable.
   *
   * @throws RabbitMqCommandException if the command cannot be executed, it doesn't
   *                                  {@link EmbeddedRabbitMqConfig.Builder#defaultRabbitMqCtlTimeoutInMillis(long)
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
   * @throws RabbitMqCommandException if the command cannot be executed, it doesn't
   *                                  {@link EmbeddedRabbitMqConfig.Builder#defaultRabbitMqCtlTimeoutInMillis(long)
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

}
