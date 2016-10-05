package io.arivera.oss.embedded.rabbitmq.bin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.apache.commons.io.FileUtils;
import io.arivera.oss.embedded.rabbitmq.util.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Level;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

/**
 * A wrapper for the command "<code>{@value ErlangShell#COMMAND}</code>", used for checking/testing the Erlang version.
 */
public class ErlangShell {
  private static final String COMMAND = "erl";
  private static final String LOGGER_TEMPLATE = "%s.Process.%s";

  private final EmbeddedRabbitMqConfig config;
  private final RabbitMqCommand.ProcessExecutorFactory processExecutorFactory;

  private final Logger processOutputLogger;

  /**
   * Generic Constructor.
   */
  public ErlangShell(final EmbeddedRabbitMqConfig config) {
    this.config = config;
    this.processExecutorFactory = config.getProcessExecutorFactory();

    this.processOutputLogger = LoggerFactory.getLogger(
      String.format(LOGGER_TEMPLATE, this.getClass().getName(), COMMAND));
  }

  /**
   * Fire up the Erlang shell to get the version information; if the process fails to run, no Erlang is installed,
   * or available on the path.
   */
  public void checkErlangExistence() throws ErlangShellException {
    final ProcessResult result = execute();
    final int exitVal = result.getExitValue();
    final String results = result.outputUTF8();

    if (exitVal != 0 || results.toLowerCase().contains("not found")) {
      throw new ErlangShellException("Erlang not found");
    }
  }

  private ProcessResult execute() throws RuntimeException {
    if (!config.getAppFolder().mkdirs()) {
      throw new ErlangShellException("Could not create temporary directory.");
    }

    final File outputFile = new File(config.getAppFolder(), "test.erl");

    try {
      final InputStream inputStream = getClass().getResourceAsStream("/test.erl");
      FileUtils.copyInputStreamToFile(inputStream, outputFile);
    } catch (final IOException ie) {
      throw new ErlangShellException("Could not create temporary file.", ie);
    }

    final Slf4jStream loggingStream = Slf4jStream.of(processOutputLogger);

    final ProcessExecutor processExecutor = new ProcessExecutor()
      .directory(config.getAppFolder())
      .command("erlc", "test.erl")
      .timeout(10L, TimeUnit.SECONDS)
      .redirectError(loggingStream.as(Level.WARN))     // Logging for output made to STDERR
      .redirectOutput(loggingStream.as(Level.INFO))     // Logging for output made to STDOUT
      .redirectOutputAlsoTo(new NullOutputStream())         // Pipe stdout to this stream for the application to process
      .redirectErrorAlsoTo(new NullOutputStream())     // Pipe stderr to this stream for the application to process
      .destroyOnExit()
      .readOutput(true);

    try {
      return processExecutor.execute();
    } catch (final IOException | InterruptedException | TimeoutException ex) {
      throw new ErlangShellException("Failed to execute: " + StringUtils.join(processExecutor.getCommand(), " "), ex);
    }
  }
}
