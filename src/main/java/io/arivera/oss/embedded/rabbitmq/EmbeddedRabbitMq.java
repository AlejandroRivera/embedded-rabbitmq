package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.StopWatch;
import io.arivera.oss.embedded.rabbitmq.util.StringUtils;
import io.arivera.oss.embedded.rabbitmq.util.SystemUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tukaani.xz.XZInputStream;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jOutputStream;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    Downloader downloader = new Downloader(this.getConfig());
    downloader.download();
  }

  private void extract() {
    TarArchiveInputStream archive;
    try {
      archive = new TarArchiveInputStream(new XZInputStream(new BufferedInputStream(new FileInputStream(config.getDownloadTarget()))));
    } catch (IOException e) {
      throw new DownloadException("Download file '" + config.getDownloadTarget() + "' was not found or is not accessible.", e);
    }

    TarArchiveEntry fileToExtract;
    try {
      fileToExtract = archive.getNextTarEntry();
    } catch (IOException e) {
      throw new DownloadException(
          "Could not extract files from file '" + config.getDownloadTarget() + "' due to: " + e.getLocalizedMessage(), e);
    }

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    int fileCounter = 0;
    while (fileToExtract != null) {
      fileCounter++;
      File destPath = new File(config.getExtractionFolder(), fileToExtract.getName());

      if (fileToExtract.isDirectory()) {
        boolean mkdirs = destPath.mkdirs();
        if (!mkdirs) {
          LOGGER.warn("Directory '{}' could not be created. Will attempt to continue...", destPath);
        }
      } else {
        try {
          boolean newFile = destPath.createNewFile();
          if (!newFile) {
            LOGGER.warn("File '{}' already exists. Will attempt to continue...", destPath);
          }
        } catch (IOException e) {
          LOGGER.warn("Could not extract file '" + destPath + "'. Will attempt to continue...", e);
        }

        if (!SystemUtils.IS_OS_WINDOWS) {
          int mode = fileToExtract.getMode();     // example: 764
          int ownerBits = mode >> 2;              // owner bits: 7
          int isExecutable = ownerBits & 1;       // bits: RWX, where X = executable bit
          boolean madeExecutable = destPath.setExecutable(isExecutable == 1);
          if (!madeExecutable) {
            LOGGER.warn("File '{}' (original mode {}) could not be made executable probably due to permission issues.",
                fileToExtract.getName(), mode);
          }
        }

        BufferedOutputStream output = null;
        try {
          LOGGER.debug("Extracting '" + fileToExtract.getName() + "'...");
          output = new BufferedOutputStream(new FileOutputStream(destPath));
          IOUtils.copy(archive, output);
        } catch (IOException e) {
          throw new DownloadException("Error extracting file '" + fileToExtract.getName() + "' " +
              "from downloaded file: " + config.getDownloadTarget(), e);
        } finally {
          IOUtils.closeQuietly(output);
        }
      }

      try {
        fileToExtract = archive.getNextTarEntry();
      } catch (IOException e) {
        LOGGER.error("Could not find next file to extract.", e);
        break;
      }
    }
    stopWatch.stop();
    IOUtils.closeQuietly(archive);
    LOGGER.info("Finished extracting {} files from '{}' in {}ms", fileCounter, config.getDownloadTarget(), stopWatch.getTime());
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
