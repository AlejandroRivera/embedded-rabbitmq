package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jOutputStream;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class EmbeddedRabbitMq {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedRabbitMq.class);

  private EmbeddedRabbitMqConfig config;
  private StartedProcess rabbitMqProcess;
  private PublishingProcessListener rabbitMqProcessListener;

  public EmbeddedRabbitMq(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  public EmbeddedRabbitMqConfig getConfig() {
    return config;
  }

  public void start() throws DownloadException, ProcessException {
    download();
    extract();
    run();
  }

  private void download() {
    Runnable downloader = new Downloader(this.getConfig());
    if (config.shouldCachedDownload()) {
      downloader = new CachedDownloader(downloader, config);
    }
    downloader.run();
  }

  private void extract() {
    Runnable extractor = new Extractor(config);
    if (config.shouldCachedDownload()) {
      extractor = new CachedExtractor(extractor, config);
    }
    extractor.run();
  }

  private void run() throws ProcessException {
    String command = "rabbitmq_server-3.6.4/sbin/rabbitmq-server";
    try {
      PatternFinderOutputStream initializationWatcher = new PatternFinderOutputStream(".*completed with \\d+ plugins.*");
      rabbitMqProcessListener = new PublishingProcessListener();
      rabbitMqProcessListener.addSubscriber(initializationWatcher);

      Slf4jOutputStream processLogger = new RabbitMqServerProcessLogger("rabbitmq-server");

      Slf4jOutputStream processOutputStream = Slf4jStream.of(EmbeddedRabbitMq.class).asInfo();
      rabbitMqProcess = new ProcessExecutor()
          .directory(config.getExtractionFolder())
          .command(command)
          .redirectError(processLogger)
          .redirectOutput(processLogger)
          .redirectOutputAlsoTo(initializationWatcher)
          .addListener(new LoggingProcessListenerDecorator(processOutputStream.getLogger(), rabbitMqProcessListener))
          .destroyOnExit()
          .start();

      boolean match = initializationWatcher.waitForMatch(config.getRabbitMqServerInitializationTimeoutInMillis(), TimeUnit.MILLISECONDS);
      if (!match) {
        throw new ProcessException("Could not start RabbitMQ Server. See logs for more details.");
      }
    } catch (IOException e) {
      throw new ProcessException("Could not execute RabbitMQ rabbitMqProcess", e);
    }
  }


  public void stop() throws ShutDownException {
    List<String> command = Arrays.asList("rabbitmq_server-3.6.4/sbin/rabbitmqctl", "stop");
    try {
      Slf4jStream loggingStream = Slf4jStream.of(EmbeddedRabbitMq.class, "Process.rabbitmqctl");

      ProcessResult rabbitMqCtlProcessResult = new ProcessExecutor()
          .directory(config.getExtractionFolder())
          .command(command)
          .redirectError(loggingStream.asError())
          .redirectOutput(loggingStream.asInfo())
          .addListener(new LoggingProcessListener(loggingStream.asDebug().getLogger()))
          .destroyOnExit()
          .start()
          .getFuture()
          .get(config.getDefaultRabbitMqCtlTimeoutInMillis(), TimeUnit.MILLISECONDS);

      int exitValue = rabbitMqCtlProcessResult.getExitValue();
      if (exitValue == 0) {
        LOGGER.info("Submitted command to stop RabbitMQ Server successfully.");
      } else {
        LOGGER.warn("Command '"+ StringUtils.join(command, " ")+"' exited with value: " + exitValue);
      }
    } catch (IOException e) {
      throw new ShutDownException("Could not successfully execute: " + StringUtils.join(command, " "), e);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new ShutDownException("Command '" + StringUtils.join(command, " ") + "' did not finish as expected", e);
    }

    try {
      Future<ProcessResult> processfuture = rabbitMqProcess.getFuture();
      ProcessResult rabbitMqProcessResult = processfuture.get(config.getDefaultRabbitMqCtlTimeoutInMillis(), TimeUnit.MILLISECONDS);
      int exitValue = rabbitMqProcessResult.getExitValue();
      if (exitValue == 0) {
        LOGGER.info("RabbitMQ Server stopped successfully.");
      } else {
        LOGGER.warn("RabbitMQ Server stopped with exit value: " + exitValue);
      }
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new ShutDownException("Error while waiting for RabbitMQ Server to shut down", e);
    }

  }

  private static class RabbitMqServerProcessLogger extends Slf4jOutputStream {

    public RabbitMqServerProcessLogger(String processName) {
      super(LoggerFactory.getLogger(EmbeddedRabbitMq.class.getName() + ".Process." + processName));
    }

    @Override
    protected void processLine(String line) {
      if (line.startsWith("ERROR:")) {
        log.error(line);
      } else {
        log.info(line);
      }
    }
  }

}
