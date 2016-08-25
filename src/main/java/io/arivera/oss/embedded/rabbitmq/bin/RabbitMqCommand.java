package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMq;
import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.util.StringUtils;
import io.arivera.oss.embedded.rabbitmq.util.SystemUtils;

import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.listener.ProcessListener;
import org.zeroturnaround.exec.stream.slf4j.Level;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A generic way of executing any of the commands found under the {@code sbin} folder of the RabbitMQ installation.
 * <p>
 * Example usage:
 * <pre> <code>   RabbitMqCommand command = new RabbitMqCommand(config, "rabbitmq-server", "-detached");
 *  StartedProcess process = command.call();
 *  // ...
 * </code>
 * </pre>
 * <p>
 * To read the output as it happens, use the {@link #writeOutputTo(OutputStream)} method.
 * <p>
 * To be notified of the process ending without blocking for the result of the finished process, use
 * {@link #listenToEvents(ProcessListener)} method.
 *
 * @see RabbitMqCtl
 * @see RabbitMqServer
 */
public class RabbitMqCommand implements Callable<StartedProcess> {

  static final String EXECUTABLE_FOLDER = "sbin";

  private static final String LOGGER_TEMPLATE = "%s.Process.%s";

  private static final ProcessListener NULL_LISTENER = new NullProcessListener();
  private static final NullOutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

  private static final boolean IS_WIN = SystemUtils.IS_OS_WINDOWS;
  private static final String WIN_EXT = ".bat";
  private static final String UNIT_EXT = "";

  private final EmbeddedRabbitMqConfig config;
  private final String command;
  private final File executableFile;
  private final List<String> arguments;

  private final ProcessExecutorFactory processExecutorFactory;

  private Logger processOutputLogger;
  private OutputStream outputStream;
  private OutputStream errorOutputStream;
  private ProcessListener eventsListener;
  private boolean storeOutput;
  private Level stdOutLogLevel;
  private Level stdErrLogLevel;

  /**
   * Constructs a new instance this class to execute arbitrary RabbitMQ commands with arbitrary arguments.
   *
   * By default:
   * <ul>
   *   <li>
   *     the resulting processes's output will be logged using a Logger with a name matching the command.
   *     See {@link #logWith(Logger)} to use another Logger
   *   </li>
   *   <li>
   *     the output from STDOUT will be logged as {@code INFO}
   *   </li>
   *   <li>
   *     the output from STDERR will e logged as {@code WARN}
   *   </li>
   *   <li>
   *     the output can be programmatically accessed by retrieving the {@link org.zeroturnaround.exec.ProcessResult}
   *     from the resulting {@link #call()} execution. To disable storing the output see {@link #storeOutput(boolean)}
   *     <p>
   *     To obtain the output as a stream as it's being produced,
   *     see {@link #writeOutputTo(OutputStream)} and {@link #writeErrorOutputTo(OutputStream)}.
   *   </li>
   *   <li>
   *     the process' events will be ignored. See {@link #listenToEvents(ProcessListener)} to define a listener.
   *   </li>
   * </ul>
   *
   * @param config    the configuration information used to launch the process with the correct context.
   * @param command   command name, without any path or extension. For example, for a command like
   *                  "{@code rabbitmq-plugins.bat list}", use "{@code rabbitmq-plugins}" as value
   * @param arguments list of arguments to pass to the executable. For example, for a command like
   *                  "{@code ./rabbitmq-plugins enable foo}", utilize {@code ["enable", "foo"]} as value
   */
  public RabbitMqCommand(EmbeddedRabbitMqConfig config, String command, String... arguments) {
    this(new ProcessExecutorFactory(), config, command, arguments);
  }

  /**
   * @param factory the class from which new {@link ProcessExecutor}'s will be obtained.
   *
   * @see #RabbitMqCommand(EmbeddedRabbitMqConfig, String, String...)
   */
  public RabbitMqCommand(ProcessExecutorFactory factory, EmbeddedRabbitMqConfig config,
                  String command, String... arguments) {
    this.processExecutorFactory = factory;
    this.config = config;
    this.command = command + getCommandExtension();
    this.executableFile = new File(new File(config.getAppFolder(), EXECUTABLE_FOLDER), this.command);
    if (!(executableFile.exists())) {
      throw new IllegalArgumentException("The given command could not be found using the path: " + executableFile);
    }

    this.arguments = Arrays.asList(arguments);
    this.processOutputLogger = LoggerFactory.getLogger(
        String.format(LOGGER_TEMPLATE, EmbeddedRabbitMq.class.getName(), command));

    this.outputStream = NULL_OUTPUT_STREAM;
    this.errorOutputStream = NULL_OUTPUT_STREAM;
    this.eventsListener = NULL_LISTENER;  // Null listener

    this.storeOutput = true;
    this.stdOutLogLevel = Level.INFO;
    this.stdErrLogLevel = Level.WARN;
  }

  static String getCommandExtension() {
    return IS_WIN ? WIN_EXT : UNIT_EXT;
  }

  /**
   * Output from the process will be written here as it happens.
   *
   * @see #writeErrorOutputTo(OutputStream)
   */
  public RabbitMqCommand writeOutputTo(OutputStream outputStream) {
    this.outputStream = outputStream;
    return this;
  }

  /**
   * Error output from the process will be written here as it happens.
   *
   * @see #writeOutputTo(OutputStream)
   */
  public RabbitMqCommand writeErrorOutputTo(OutputStream outputStream) {
    this.errorOutputStream = outputStream;
    return this;
  }

  /**
   * Defines which SLF4J logger to use log the process output as it would have been dumped to STDOUT and STDERR.
   */
  public RabbitMqCommand logWith(Logger logger) {
    this.processOutputLogger = logger;
    return this;
  }

  /**
   * Registers a unique listener to be notified of process events, such as start and finish.
   */
  public RabbitMqCommand listenToEvents(ProcessListener listener) {
    this.eventsListener = listener;
    return this;
  }

  /**
   * Used to define if the output of the process should be stored for retrieval after the ProcessResult future is
   * completed.
   *
   * Default is {@code true}
   */
  public RabbitMqCommand storeOutput(boolean storeOutput) {
    this.storeOutput = storeOutput;
    return this;
  }

  /**
   * Defines which logging level to use for the process' standard output.
   * <p>
   * Default is {@code INFO}
   */
  public RabbitMqCommand logStandardOutputAs(Level level) {
    this.stdOutLogLevel = level;
    return this;
  }

  /**
   * Defines which logging level to use for the processes' standard error output.
   * <p>
   * Default is {@code WARN}
   */
  public RabbitMqCommand logStandardErrorOutputAs(Level level) {
    this.stdErrLogLevel = level;
    return this;
  }

  @Override
  public StartedProcess call() throws RabbitMqCommandException {

    List<String> fullCommand = new ArrayList<>(arguments);
    fullCommand.add(0, executableFile.toString());

    Slf4jStream loggingStream = Slf4jStream.of(processOutputLogger);
    LoggingProcessListener loggingListener = new LoggingProcessListener(processOutputLogger);

    ProcessExecutor processExecutor = processExecutorFactory.createInstance()
        .environment(config.getEnvVars())
        .directory(config.getAppFolder())
        .command(fullCommand)
        .destroyOnExit()
        .addListener(loggingListener)               // Logs process events (like start, stop...)
        .addListener(eventsListener)                // Notifies asynchronously of process events (start/finish/stop)
        .redirectError(loggingStream.as(stdErrLogLevel))     // Logging for output made to STDERR
        .redirectOutput(loggingStream.as(stdOutLogLevel))     // Logging for output made to STDOUT
        .redirectOutputAlsoTo(outputStream)         // Pipe stdout to this stream for the application to process
        .redirectErrorAlsoTo(errorOutputStream)     // Pipe stderr to this stream for the application to process
        .readOutput(storeOutput);                   // Store the output in the ProcessResult as well.

    try {
      return processExecutor.start();
    } catch (IOException e) {
      throw new RabbitMqCommandException("Failed to execute: " + StringUtils.join(fullCommand, " "), e);
    }
  }

  public static class ProcessExecutorFactory {
    public ProcessExecutor createInstance() {
      return new ProcessExecutor();
    }
  }

  private static class NullProcessListener extends ProcessListener {
  }
}
