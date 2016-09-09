package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

import org.zeroturnaround.exec.ProcessResult;

import java.util.concurrent.Future;

/**
 * A wrapper around the RabbitMqCommand to execute '{@value #COMMAND}' commands.
 *
 * @see <a href="https://www.rabbitmq.com/man/rabbitmq-plugins.1.man.html">rabbitmq-plugins(1) manual page</a>
 */
public class RabbitMqPlugins {

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
   */
  public Future<ProcessResult> execute(String... arguments) throws RabbitMqCommandException {
    return new RabbitMqCommand(config, COMMAND, arguments)
        .call()
        .getFuture();
  }

  public Future<ProcessResult> list() {
    return execute("list");
  }

  public Future<ProcessResult> enable(String plugin) {
    return execute("enable", plugin);
  }

  public Future<ProcessResult> disable(String plugin) {
    return execute("disable", plugin);
  }
}
