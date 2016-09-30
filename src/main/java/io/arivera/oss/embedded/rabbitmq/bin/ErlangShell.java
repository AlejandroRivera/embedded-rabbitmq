package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.util.StringUtils;

import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Level;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
    int exitVal;

    try {
      final ErlangShell shell = new ErlangShell(config);
      final Future<ProcessResult> future = shell.execute();
      final ProcessResult result = future.get();
      exitVal = result.getExitValue();
    } catch (final InterruptedException | ExecutionException ex) {
      throw new ErlangShellException("Could not start/execute Erlang shell.", ex);
    }

    if (exitVal != 0) {
      throw new ErlangShellException("Could not retrieve Erlang version.");
    }
  }

  private Future<ProcessResult> execute() throws RuntimeException {
    final List<String> fullCommand = Arrays.asList("erl", "-eval", "'{ok, Version} = "
        + "file:read_file(filename:join([code:root_dir(), \"releases\", "
        + "erlang:system_info(otp_release), \"OTP_VERSION\"])), "
        + "erlang:display(erlang:binary_to_list(Version)), halt().'", "-noshell");

    final Slf4jStream loggingStream = Slf4jStream.of(processOutputLogger);

    final ProcessExecutor processExecutor = processExecutorFactory.createInstance()
        .environment(config.getEnvVars())
        .directory(config.getAppFolder())
        .command(fullCommand)
        .destroyOnExit()
        .redirectError(loggingStream.as(Level.WARN))     // Logging for output made to STDERR
        .redirectOutput(loggingStream.as(Level.INFO))     // Logging for output made to STDOUT
        .redirectOutputAlsoTo(new NullOutputStream())         // Pipe stdout to this stream for the application to process
        .redirectErrorAlsoTo(new NullOutputStream())     // Pipe stderr to this stream for the application to process
        .readOutput(true);                   // Store the output in the ProcessResult as well.

    try {
      final StartedProcess startedProcess = processExecutor.start();
      return startedProcess.getFuture();
    } catch (IOException e) {
      throw new RabbitMqCommandException("Failed to execute: " + StringUtils.join(fullCommand, " "), e);
    }
  }
}
