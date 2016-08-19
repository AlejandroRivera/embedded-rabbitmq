package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMq;
import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.util.SystemUtils;

import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class RabbitMqCommandExecutor implements Callable<StartedProcess> {

  private static final String LOGGER_TEMPLATE = "%s.Process.%s";

  private static final String EXECUTABLE_FOLDER = "sbin";
  private static final String COMMAND_TEMPLATE = "%s%s%s%s";

  private static final boolean IS_WIN = SystemUtils.IS_OS_WINDOWS;
  private static final String WIN_EXT = ".bat";
  private static final String UNIT_EXT = "";

  private final EmbeddedRabbitMqConfig config;
  private final String executableCommand;
  private final List<String> arguments;

  private Logger processOutputLogger;
  private OutputStream outputStream;

  /**
   * @param config the configuration information used to launch the process with the correct context.
   * @param command command name, without any path or extension. For example, for a command like
   *                  "{@code rabbitmq-plugins.bat list}", use "{@code rabbitmq-plugins}" as value
   * @param arguments list of arguments to pass to the executable. For example, for a command like
   *                  "{@code ./rabbitmq-plugins enable foo}", utilize {@code ["enable", "foo"]} as value
   */
  public RabbitMqCommandExecutor(EmbeddedRabbitMqConfig config, String command, String... arguments) {
    String extension = IS_WIN ? WIN_EXT : UNIT_EXT;
    this.config = config;
    this.executableCommand = String.format(COMMAND_TEMPLATE, EXECUTABLE_FOLDER, File.separator, command, extension);
    this.arguments = Arrays.asList(arguments);

    this.outputStream = new NullOutputStream();
    this.processOutputLogger = LoggerFactory.getLogger(
        String.format(LOGGER_TEMPLATE, EmbeddedRabbitMq.class.getName(), command));
  }

  /**
   * Output from the process will be written here as it happens.
   */
  public RabbitMqCommandExecutor captureOutput(OutputStream outputStream) {
    this.outputStream = outputStream;
    return this;
  }

  public RabbitMqCommandExecutor loggingWith(Logger logger) {
    this.processOutputLogger = logger;
    return this;
  }

  @Override
  public StartedProcess call() throws IOException {

    List<String> fullCommand = new ArrayList<>(arguments);
    fullCommand.add(0, executableCommand);

    Slf4jStream loggingStream = Slf4jStream.of(processOutputLogger);
    LoggingProcessListener loggingListener = new LoggingProcessListener(processOutputLogger);

    ProcessExecutor processExecutor = new ProcessExecutor()
        .environment(config.getEnvVars())
        .directory(config.getAppFolder())
        .command(fullCommand)
        .destroyOnExit()
        .addListener(loggingListener)               // Logs process events (like start, stop...)
        .redirectError(loggingStream.asError())     // Logging for output made to STDERR
        .redirectOutput(loggingStream.asInfo())     // Logging for output made to STDOUT
        .redirectOutputAlsoTo(outputStream);

    return processExecutor.start();
  }

}
