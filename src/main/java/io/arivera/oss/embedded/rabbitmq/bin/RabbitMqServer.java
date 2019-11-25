package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

import org.apache.commons.io.output.NullOutputStream;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.io.OutputStream;
import java.util.concurrent.Future;

/**
 * A wrapper for the command "<code>{@value RabbitMqServer#COMMAND}</code>", used for starting the RabbitMQ broker.
 */
public class RabbitMqServer {

  private static final String COMMAND = "rabbitmq-server";

  private final EmbeddedRabbitMqConfig config;

  private OutputStream outputStream;
  private ProcessListener listener;

  /**
   * Creates a new RabbitMqServer with NOOP settings for output capturing and event listening.
   *
   * @see #writeOutputTo(OutputStream)
   * @see #listeningToEventsWith(ProcessListener)
   */
  public RabbitMqServer(EmbeddedRabbitMqConfig config) {
    this.config = config;
    this.outputStream = new NullOutputStream();
    this.listener = new NullProcessListener();
  }

  /**
   * Use this method if you wish the output of the process is streamed somewhere as it happens.
   *
   * @return this same instance of the class to allow for chaining calls.
   *
   * @see RabbitMqCommand#writeOutputTo(OutputStream)
   */
  public RabbitMqServer writeOutputTo(OutputStream outputStream) {
    this.outputStream = outputStream;
    return this;
  }

  /**
   * Use this method to register a listener to be notified of process events, like start, stop, etc.
   */
  public RabbitMqServer listeningToEventsWith(ProcessListener listener) {
    this.listener = listener;
    return this;
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
   * <li> wait for the returning Future to finish and use {@link ProcessResult} output getter methods, or </li>
   * <li> provide an Output Stream through {@link #writeOutputTo(OutputStream)} to receive it as it happens. </li>
   * </ul>
   * <p>
   * To be notified of process events, such as the process starting or finishing, provide a
   */
  public Future<ProcessResult> start() throws RabbitMqCommandException {
    return execute();
  }

  /**
   * Start the RabbitMq Server in a detached state.
   * <p>
   * This means the process will exit immediately and no PID file will be written to file.
   */
  public Future<ProcessResult> startDetached() throws RabbitMqCommandException {
    return execute("-detached");
  }

  private Future<ProcessResult> execute(String... arguments) throws RabbitMqCommandException {
    return new RabbitMqCommand(config, COMMAND, arguments)
        .writeOutputTo(outputStream)
        .listenToEvents(listener)
        .enableEnvVars()
        .call()
        .getFuture();
  }

  private static class NullProcessListener extends ProcessListener {
  }
}
