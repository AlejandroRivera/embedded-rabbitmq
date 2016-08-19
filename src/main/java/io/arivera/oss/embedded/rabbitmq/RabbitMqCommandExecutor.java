package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.SystemUtils;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class RabbitMqCommandExecutor implements Callable<StartedProcess> {

  private static final String LOGGER_NAME_TEMPLATE = "Process.%s";

  private static final String EXECUTABLE_FOLDER = "sbin";
  private static final String COMMAND_TEMPLATE = "%s%s%s%s";

  private static final boolean IS_WIN = SystemUtils.IS_OS_WINDOWS;
  private static final String WIN_EXT = ".bat";
  private static final String UNIT_EXT = "";

  private final EmbeddedRabbitMqConfig config;
  private final String command;
  private final String executableCommand;
  private final List<String> arguments;

  /**
   * @param config the configuration information used to launch the process with the correct context.
   * @param command command name, without any path or extension. For example, for a command like
   *                  "{@code rabbitmq-plugins.bat list}", use "{@code rabbitmq-plugins}" as value
   * @param arguments list of arguments to pass to the executable. For example, for a command like
   *                  "{@code ./rabbitmq-plugins enable foo}", utilize {@code ["enable", "foo"]} as value
   */
  public RabbitMqCommandExecutor(EmbeddedRabbitMqConfig config, String command, String... arguments) {
    this.config = config;
    String extension = IS_WIN ? WIN_EXT : UNIT_EXT;
    this.command = command;
    this.executableCommand = String.format(COMMAND_TEMPLATE, EXECUTABLE_FOLDER, File.separator, command, extension);
    this.arguments = Arrays.asList(arguments);
  }

  @Override
  public StartedProcess call() throws IOException {

    List<String> fullCommand = new ArrayList<>(arguments);
    fullCommand.add(0, executableCommand);

    ProcessExecutor processExecutor = new ProcessExecutor()
        .environment(config.getEnvVars())
        .directory(config.getAppFolder())
        .command(fullCommand)
        .destroyOnExit();

    processExecutor = configureListeners(processExecutor);
    return processExecutor.start();
  }

  private ProcessExecutor configureListeners(ProcessExecutor executor) {
    Slf4jStream loggingStream = Slf4jStream.of(EmbeddedRabbitMq.class, String.format(LOGGER_NAME_TEMPLATE, command));
    LoggingProcessListener loggingListener = new LoggingProcessListener(loggingStream.asDebug().getLogger());

    return executor.addListener(loggingListener)
        .redirectError(loggingStream.asError())
        .redirectOutput(loggingStream.asInfo());
  }
}
