package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.FileUtils;
import io.arivera.oss.embedded.rabbitmq.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

class Downloader {

  private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);
  
  private final EmbeddedRabbitMqConfig config;

  Downloader(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  void download() {
    DownloadProgressNotifier progressNotifier = new DownloadProgressNotifier(config);
    DownloadTask downloadTask = new DownloadTask(config);
    downloadTask.addListener(progressNotifier);

    Thread notifierThread = new Thread(progressNotifier, "RabbitMQ-Download-Watcher");
    notifierThread.start();
    downloadTask.run();
  }

  private static class DownloadTask implements Runnable {
    private final StopWatch stopWatch;
    private final EmbeddedRabbitMqConfig config;
    private final List<DownloadListener> downloadListeners;

    public DownloadTask(EmbeddedRabbitMqConfig config) {
      this.config = config;
      this.stopWatch = new StopWatch();
      this.downloadListeners = new ArrayList<>();
    }

    public void addListener(DownloadListener downloadListener) {
      this.downloadListeners.add(downloadListener);
    }

    public void run() {
      LOGGER.info("Downloading '{}'...", config.getDownloadSource());
      LOGGER.debug("Downloading to '{}' with {}ms connection and {}ms download timeout...",
          config.getDownloadTarget(),
          config.getDownloadConnectionTimeoutInMillis(),
          config.getDownloadReadTimeoutInMillis());

      try {
        stopWatch.start();
        FileUtils.copyURLToFile(
            config.getDownloadSource(),
            config.getDownloadTarget(),
            (int) config.getDownloadConnectionTimeoutInMillis(),
            (int) config.getDownloadReadTimeoutInMillis());
        stopWatch.stop();
        LOGGER.info("Download finished in {}ms", stopWatch.getTime());
      } catch (IOException e) {
        throw new DownloadException(
            "Could not download '" + config.getDownloadSource() + "' to '" + config.getDownloadTarget() + "'", e);
      } finally {
        notifyListeners();
      }
    }

    private void notifyListeners() {
      for (DownloadListener downloadListener : downloadListeners) {
        downloadListener.downloadFinished();
      }
    }

  }

  private interface DownloadListener {
    void downloadFinished();
  }

  private static class DownloadProgressNotifier implements Runnable, DownloadListener {

    private final Semaphore semaphore;
    private final EmbeddedRabbitMqConfig config;

    DownloadProgressNotifier(EmbeddedRabbitMqConfig config) {
      this.semaphore = new Semaphore(1);
      this.config = config;
    }

    @Override
    public void downloadFinished() {
      semaphore.release();
    }

    @Override
    public void run() {
      try {
        semaphore.acquire();
      } catch (InterruptedException e) {
        throw new IllegalStateException("Acquire should work!");
      }
      while (!semaphore.tryAcquire()) {
        try {
          LOGGER.debug("Downloaded {} bytes", config.getDownloadTarget().length());
          Thread.sleep(500);
        } catch (InterruptedException e) {
          LOGGER.trace("Download indicator interrupted");
        }
      }
      LOGGER.trace("Download indicator finished normally");
    }
  }


}
