package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.download.DownloadException;
import io.arivera.oss.embedded.rabbitmq.download.Downloader;
import io.arivera.oss.embedded.rabbitmq.download.DownloaderFactory;
import io.arivera.oss.embedded.rabbitmq.extract.ExtractionException;
import io.arivera.oss.embedded.rabbitmq.extract.ExtractorFactory;
import io.arivera.oss.embedded.rabbitmq.helpers.ShutDownException;
import io.arivera.oss.embedded.rabbitmq.helpers.ShutdownHelper;
import io.arivera.oss.embedded.rabbitmq.helpers.StartupException;
import io.arivera.oss.embedded.rabbitmq.helpers.StartupHelper;

import org.zeroturnaround.exec.ProcessResult;

import java.util.concurrent.Future;


public class EmbeddedRabbitMq {

  private EmbeddedRabbitMqConfig config;
  private Future<ProcessResult> rabbitMqProcess;

  public EmbeddedRabbitMq(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  /**
   * Starts the RabbitMQ server process and blocks the current thread until the initialization is completed.
   *
   * @throws DownloadException when there's an issue downloading the appropriate artifact
   * @throws ExtractionException when there's an issue extracting the files from the downloaded artifact
   * @throws StartupException when there's an issue starting the RabbitMQ server
   */
  public void start() throws DownloadException, ExtractionException, StartupException {
    if (rabbitMqProcess != null) {
      throw new IllegalStateException("Start shouldn't be called more than once unless stop() has been called before.");
    }
    download();
    extract();
    run();
  }

  private void download() throws DownloadException {
    Downloader downloader = DownloaderFactory.getNewInstance(config);
    downloader.run();
  }

  private void extract() throws ExtractionException {
    Runnable extractor = ExtractorFactory.getNewInstance(config);
    extractor.run();
  }

  private void run() throws StartupException {
    rabbitMqProcess = new StartupHelper(config).call();
  }

  /**
   * Submits the command to stop RabbitMQ and blocks the current thread until the shutdown is completed.
   *
   * @throws ShutDownException if there's an issue shutting down the RabbitMQ server
   */
  public void stop() throws ShutDownException {
    if (rabbitMqProcess == null) {
      throw new IllegalStateException("Stop shouldn't be called unless 'start()' was successful.");
    }
    new ShutdownHelper(config, rabbitMqProcess).run();
    rabbitMqProcess = null;
  }

}
