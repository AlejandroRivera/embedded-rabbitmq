package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.download.DownloadException;
import io.arivera.oss.embedded.rabbitmq.download.DownloaderFactory;
import io.arivera.oss.embedded.rabbitmq.extract.ExtractException;
import io.arivera.oss.embedded.rabbitmq.extract.ExtractorFactory;

import org.zeroturnaround.exec.ProcessResult;

import java.util.concurrent.Future;


public class EmbeddedRabbitMq {

  private EmbeddedRabbitMqConfig config;
  private Future<ProcessResult> rabbitMqProcess;

  public EmbeddedRabbitMq(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  public void start() throws DownloadException, ExtractException, ProcessException {
    download();
    extract();
    run();
  }

  private void download() throws DownloadException {
    Runnable downloader = DownloaderFactory.getNewInstance(config);
    downloader.run();
  }

  private void extract() throws ExtractException {
    Runnable extractor = ExtractorFactory.getNewInstance(config);
    extractor.run();
  }

  private void run() throws ProcessException {
    rabbitMqProcess = new Starter(config).call();
  }

  public void stop() throws ShutDownException {
    if (rabbitMqProcess == null) {
      throw new IllegalStateException("Stop shouldn't be called unless 'start()' was successful.");
    }
    new Stopper(config, rabbitMqProcess).run();
  }

}
