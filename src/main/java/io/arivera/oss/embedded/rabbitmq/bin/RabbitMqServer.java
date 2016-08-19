package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

import org.apache.commons.io.output.NullOutputStream;
import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Future;

/**
 * A wrapper for the command "<code>{@value #COMMAND}</code>", used for starting the RabbitMQ broker.
 */
public class RabbitMqServer {

  public static final String COMMAND = "rabbitmq-server";

  private final EmbeddedRabbitMqConfig config;
  private final OutputStream outputStream;

  public RabbitMqServer(EmbeddedRabbitMqConfig config) {
    this(config, new NullOutputStream());
  }

  public RabbitMqServer(EmbeddedRabbitMqConfig config, OutputStream outputStream) {
    this.config = config;
    this.outputStream = outputStream;
  }

  /**
   * Starts the RabbitMQ Server and keeps the process running until it's stopped.
   * <p>
   * Running rabbitmq-server in the foreground displays a banner message, and reports on progress in the startup
   * sequence, concluding with the message "{@code completed with [N] plugins.}", indicating that the
   * RabbitMQ broker has been started successfully.
   * <p>
   * To read the output, either:
   * <ul>
   *   <li>
   *     wait for the Future to finish and use {@link ProcessResult} output methods, or
   *   </li>
   *   <li>
   *     provide an Output Stream through the {@link RabbitMqServer#RabbitMqServer(EmbeddedRabbitMqConfig, OutputStream) constructor}
   *     to receive it as it happens.
   *   </li>
   * </ul>
   */
  public Future<ProcessResult> start() throws IOException {
    return execute();
  }

  /**
   * Start the RabbitMq Server in a detached state.
   * <p>
   * This means the process will exit immediately and no PID file will be written to file.
   */
  public Future<ProcessResult> startDetached() throws IOException {
    return execute("-detached");
  }

  public Future<ProcessResult> execute(String... arguments) throws IOException {
    return new RabbitMqCommandExecutor(config, COMMAND, arguments)
        .captureOutput(outputStream)
        .call()
        .getFuture();
  }

}
