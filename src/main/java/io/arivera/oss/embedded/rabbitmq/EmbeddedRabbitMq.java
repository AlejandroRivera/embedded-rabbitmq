package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.download.DownloadException;
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

  public void start() throws DownloadException, ExtractionException, StartupException {
    if (rabbitMqProcess != null) {
      throw new IllegalStateException("Start shouldn't be called more than once unless stop() has been called before.");
    }
    download();
    extract();
    run();
  }

  private void download() throws DownloadException {
    Runnable downloader = DownloaderFactory.getNewInstance(config);
    downloader.run();
  }

  private void extract() throws ExtractionException {
    Runnable extractor = ExtractorFactory.getNewInstance(config);
    extractor.run();
  }

  private void run() throws StartupException {
    rabbitMqProcess = new StartupHelper(config).call();
  }

  public void stop() throws ShutDownException {
    if (rabbitMqProcess == null) {
      throw new IllegalStateException("Stop shouldn't be called unless 'start()' was successful.");
    }
    new ShutdownHelper(config, rabbitMqProcess).run();
    rabbitMqProcess = null;
  }

}
