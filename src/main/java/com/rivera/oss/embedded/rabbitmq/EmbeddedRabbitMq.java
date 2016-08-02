package com.rivera.oss.embedded.rabbitmq;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.StopWatch;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EmbeddedRabbitMq {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedRabbitMq.class);

  private final long downloadReadTimeoutInMillis;
  private final long downloadConnectionTimeoutInMillis;
  private final long defaultRabbitMqCtlTimeoutInMillis;
  private final long rabbitMqServerInitializationTimeoutInMillis;

  private URL downloadSource;
  private File downloadTarget;
  private File extractionFolder;
  private StartedProcess rabbitMqProcess;
  private PublishingProcessListener rabbitMqProcessListener;

  public EmbeddedRabbitMq(URL downloadSource, File downloadTarget,
                          long connectionTimeoutInMillis,
                          long downloadTimeoutInMillis,
                          File extractionFolder,
                          long rabbitMqServerInitializationTimeoutInMillis,
                          long defaultRabbitMqCtlTimeout) {
    this.downloadSource = downloadSource;
    this.downloadTarget = downloadTarget;

    this.extractionFolder = extractionFolder;

    this.downloadConnectionTimeoutInMillis = connectionTimeoutInMillis;
    this.downloadReadTimeoutInMillis = downloadTimeoutInMillis;

    this.rabbitMqServerInitializationTimeoutInMillis = rabbitMqServerInitializationTimeoutInMillis;
    this.defaultRabbitMqCtlTimeoutInMillis = defaultRabbitMqCtlTimeout;
  }

  public void start() throws DownloadException, ProcessException {
    download();
    extract();
    run();
  }

  private void download() {
    LOGGER.info("Downloading '{}'...", downloadSource);
    LOGGER.debug("Downloading to '{}' with {}ms connection and {}ms download timeout...",
          downloadTarget, downloadConnectionTimeoutInMillis, downloadReadTimeoutInMillis);

    final StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    final Semaphore semaphore = new Semaphore(1);
    Thread downloadThread = new Thread(new DownloadTask(stopWatch, semaphore));
    downloadThread.start();
    indicateDownloadProgress(semaphore);
    try {
      downloadThread.join();
    } catch (InterruptedException e) {
      LOGGER.error("Should not have been interrupted!", e);
    }
  }

  private void indicateDownloadProgress(final Semaphore semaphore) {
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      throw new IllegalStateException("Acquire should work!");
    }
    while (!semaphore.tryAcquire()) {
      try {
        LOGGER.debug("Downloaded {} bytes", downloadTarget.length());
        Thread.sleep(500);
      } catch (InterruptedException e) {
        LOGGER.debug("Download indicator interrupted");
      }
    }
    LOGGER.trace("Download indicator finished normally");
  }

  private void extract() {
    TarArchiveInputStream archive;
    try {
      archive = new TarArchiveInputStream(new XZInputStream(new BufferedInputStream(new FileInputStream(downloadTarget))));
    } catch (IOException e) {
      throw new DownloadException("Download file '" + downloadTarget + "' was not found or is not accessible.", e);
    }

    TarArchiveEntry fileToExtract;
    try {
      fileToExtract = archive.getNextTarEntry();
    } catch (IOException e) {
      throw new DownloadException(
          "Could not extract files from file '" + downloadTarget + "' due to: " + e.getLocalizedMessage(), e);
    }

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    int fileCounter = 0;
    while (fileToExtract != null) {
      fileCounter++;
      File destPath = new File(extractionFolder, fileToExtract.getName());

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
          LOGGER.warn("Error extracting '" + fileToExtract.getName() + "'.", e);
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
    LOGGER.info("Finished extracting {} files from '{}' in {}ms", fileCounter, downloadTarget, stopWatch.getTime());
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
          .directory(extractionFolder)
          .command(command)
          .redirectError(processLogger)
          .redirectOutput(processLogger)
          .redirectOutputAlsoTo(initializationWatcher)
          .addListener(new LoggingProcessListenerDecorator(processOutputStream.getLogger(), rabbitMqProcessListener))
          .destroyOnExit()
          .start();

      boolean match = initializationWatcher.waitForMatch(rabbitMqServerInitializationTimeoutInMillis, TimeUnit.MILLISECONDS);
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
          .directory(extractionFolder)
          .command(command)
          .redirectError(loggingStream.asError())
          .redirectOutput(loggingStream.asInfo())
          .addListener(new LoggingProcessListener(loggingStream.asDebug().getLogger()))
          .destroyOnExit()
          .start()
          .getFuture()
          .get(defaultRabbitMqCtlTimeoutInMillis, TimeUnit.MILLISECONDS);

      int exitValue = rabbitMqCtlProcessResult.getExitValue();
      if (exitValue == 0) {
        LOGGER.info("Submitted command to stop RabbitMQ Server successfully.");
      } else {
        LOGGER.warn("Command '"+StringUtils.join(command, ' ')+"' exited with value: " + exitValue);
      }
    } catch (IOException e) {
      throw new ShutDownException("Could not successfully execute: " + StringUtils.join(command, ' '), e);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new ShutDownException("Command '" + StringUtils.join(command, ' ') + "' did not finish as expected", e);
    }

    try {
      Future<ProcessResult> processfuture = rabbitMqProcess.getFuture();
      ProcessResult rabbitMqProcessResult = processfuture.get(defaultRabbitMqCtlTimeoutInMillis, TimeUnit.MILLISECONDS);
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


  public static class Builder {

    public static final String GENERIC_UNIX_V3_6_4 =
        "http://www.rabbitmq.com/releases/rabbitmq-server/v3.6.4/rabbitmq-server-generic-unix-3.6.4.tar.xz";
    public static final String WINDOWS_V3_6_4 =
        "http://www.rabbitmq.com/releases/rabbitmq-server/v3.6.4/rabbitmq-server-windows-3.6.4.zip";

    private URL downloadSource;
    private File downloadTarget;
    private File extractionFolder;
    private long connectionTimeoutInMillis;
    private long readTimeoutInMillis;
    private long defaultRabbitMqCtlTimeoutInMillis;
    private long rabbitMqServerInitializationTimeoutInMillis;

    public Builder() {
      try {
        this.downloadSource = new URL(SystemUtils.IS_OS_WINDOWS ? WINDOWS_V3_6_4 : GENERIC_UNIX_V3_6_4);
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
      this.connectionTimeoutInMillis = TimeUnit.SECONDS.toMillis(2);
      this.readTimeoutInMillis = TimeUnit.SECONDS.toMillis(30);
      this.defaultRabbitMqCtlTimeoutInMillis = TimeUnit.SECONDS.toMillis(2);
      this.rabbitMqServerInitializationTimeoutInMillis = TimeUnit.SECONDS.toMillis(3);
    }

    public Builder downloadFrom(URL url) {
      this.downloadSource = url;
      return this;
    }

    public Builder saveDownloadTo(File file) {
      this.downloadTarget = file;
      return this;
    }

    public Builder connectionReadTimeout(long duration, TimeUnit timeUnit) {
      this.readTimeoutInMillis = timeUnit.toMillis(duration);
      return this;
    }

    public Builder extractTo(File folder) {
      this.extractionFolder = folder;
      return this;
    }

    public EmbeddedRabbitMq build() {
      if (downloadTarget == null) {
        String filename = downloadSource.getPath().substring(downloadSource.getPath().lastIndexOf("/"));
        this.downloadTarget = new File(SystemUtils.JAVA_IO_TMPDIR + File.separator + filename);
      }
      if (extractionFolder == null) {
        this.extractionFolder = new File(SystemUtils.JAVA_IO_TMPDIR);
      }

      return new EmbeddedRabbitMq(
          downloadSource, downloadTarget,
          connectionTimeoutInMillis, readTimeoutInMillis,
          extractionFolder,
          rabbitMqServerInitializationTimeoutInMillis,
          defaultRabbitMqCtlTimeoutInMillis);
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

  private class DownloadTask implements Runnable {
    private final StopWatch stopWatch;
    private final Semaphore semaphore;

    public DownloadTask(StopWatch stopWatch, Semaphore semaphore) {
      this.stopWatch = stopWatch;
      this.semaphore = semaphore;
    }

    public void run() {
      try {
        FileUtils.copyURLToFile(downloadSource, downloadTarget, (int) downloadConnectionTimeoutInMillis, (int) downloadReadTimeoutInMillis);
        stopWatch.stop();
        LOGGER.info("Download finished in {}ms", stopWatch.getTime());
      } catch (IOException e) {
        throw new DownloadException("Could not download '" + downloadSource + "' to '" + downloadTarget + "'", e);
      } finally {
        semaphore.release();
      }
    }
  }
}
