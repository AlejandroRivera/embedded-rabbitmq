package io.arivera.oss.embedded.rabbitmq;

import org.zeroturnaround.exec.StartedProcess;


public class EmbeddedRabbitMq {

  private EmbeddedRabbitMqConfig config;
  private StartedProcess rabbitMqProcess;

  public EmbeddedRabbitMq(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  public void start() throws DownloadException, ProcessException {
    download();
    extract();
    run();
  }

  private void download() throws DownloadException {
    Runnable downloader = new Downloader(config);
    if (config.shouldCachedDownload()) {
      downloader = new CachedDownloader(downloader, config);
    }
    downloader.run();
  }

  private void extract() throws DownloadException {
    Runnable extractor = new Extractor(config);
    if (config.shouldCachedDownload()) {
      extractor = new CachedExtractor(extractor, config);
    }
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
