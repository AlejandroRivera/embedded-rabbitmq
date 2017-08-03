package io.arivera.oss.embedded.rabbitmq.download;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.apache.commons.io.FileUtils;
import io.arivera.oss.embedded.rabbitmq.apache.commons.lang3.StopWatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

class BasicDownloader implements Runnable, Downloader {

  private static final Logger LOGGER = LoggerFactory.getLogger(BasicDownloader.class);
  
  private final EmbeddedRabbitMqConfig config;

  BasicDownloader(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  @Override
  public void run() throws DownloadException {
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
        copyUrlToFile(
            config.getDownloadSource(),
            config.getDownloadTarget(),
            (int) config.getDownloadConnectionTimeoutInMillis(),
            (int) config.getDownloadReadTimeoutInMillis(),
            config.getDownloadProxy());
        stopWatch.stop();
        LOGGER.info("Download finished in {}ms", stopWatch.getTime());
      } catch (IOException e) {
        throw new DownloadException(
            "Could not download '" + config.getDownloadSource() + "' to '" + config.getDownloadTarget() + "'", e);
      } finally {
        notifyListeners();
      }
    }

    /**
     * Copies bytes from the URL <code>source</code> to a file
     * <code>destination</code>. The directories up to <code>destination</code>
     * will be created if they don't already exist. <code>destination</code>
     * will be overwritten if it already exists.
     *
     * @param source            the <code>URL</code> to copy bytes from, must not be {@code null}
     * @param destination       the non-directory <code>File</code> to write bytes to
     *                          (possibly overwriting), must not be {@code null}
     * @param connectionTimeout the number of milliseconds until this method
     *                          will timeout if no connection could be established to the <code>source</code>
     * @param readTimeout       the number of milliseconds until this method will
     *                          timeout if no data could be read from the <code>source</code>
     * @param proxy             the proxy to use to open connection
     * @throws IOException if <code>source</code> URL cannot be opened
     * @throws IOException if <code>destination</code> is a directory
     * @throws IOException if <code>destination</code> cannot be written
     * @throws IOException if <code>destination</code> needs creating but can't be
     * @throws IOException if an IO error occurs during copying
     */
    public static void copyUrlToFile( URL source, File destination,
                                      int connectionTimeout, int readTimeout, Proxy proxy ) throws IOException {
      URLConnection connection = source.openConnection( proxy );
      connection.setConnectTimeout(connectionTimeout);
      connection.setReadTimeout(readTimeout);
      InputStream input = connection.getInputStream();
      FileUtils.copyInputStreamToFile(input, destination);
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
