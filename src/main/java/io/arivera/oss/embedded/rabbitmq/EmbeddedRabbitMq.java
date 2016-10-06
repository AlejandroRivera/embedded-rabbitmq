package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.download.DownloadException;
import io.arivera.oss.embedded.rabbitmq.download.Downloader;
import io.arivera.oss.embedded.rabbitmq.download.DownloaderFactory;
import io.arivera.oss.embedded.rabbitmq.extract.ExtractionException;
import io.arivera.oss.embedded.rabbitmq.extract.Extractor;
import io.arivera.oss.embedded.rabbitmq.extract.ExtractorFactory;
import io.arivera.oss.embedded.rabbitmq.helpers.ErlangVersionChecker;
import io.arivera.oss.embedded.rabbitmq.helpers.ErlangVersionException;
import io.arivera.oss.embedded.rabbitmq.helpers.ShutDownException;
import io.arivera.oss.embedded.rabbitmq.helpers.ShutdownHelper;
import io.arivera.oss.embedded.rabbitmq.helpers.StartupException;
import io.arivera.oss.embedded.rabbitmq.helpers.StartupHelper;

import org.zeroturnaround.exec.ProcessResult;

import java.util.concurrent.Future;

/**
 * This is the main class to interact with RabbitMQ.
 * <p>
 * Example use:
 * <pre>
 * {@code
 *   EmbeddedRabbitMqConfig config = new EmbeddedRabbitMqConfig.Builder().build();
 *   EmbeddedRabbitMq rabbitMq = new EmbeddedRabbitMq(config);
 *   rabbitMq.start();
 *   // ...
 *   rabbitMq.stop();
 * }
 * </pre>
 *
 * @see EmbeddedRabbitMqConfig
 */
public class EmbeddedRabbitMq {

  private EmbeddedRabbitMqConfig config;
  private Future<ProcessResult> rabbitMqProcess;

  public EmbeddedRabbitMq(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  /**
   * Starts the RabbitMQ server process and blocks the current thread until the initialization is completed.
   *
   * @throws ErlangVersionException when there's an issue with the system's Erlang version
   * @throws DownloadException    when there's an issue downloading the appropriate artifact
   * @throws ExtractionException  when there's an issue extracting the files from the downloaded artifact
   * @throws StartupException     when there's an issue starting the RabbitMQ server
   */
  public void start() throws ErlangVersionException, DownloadException, ExtractionException, StartupException {
    if (rabbitMqProcess != null) {
      throw new IllegalStateException("Start shouldn't be called more than once unless stop() has been called before.");
    }

    check();
    download();
    extract();
    run();
  }

  private void check() throws ErlangVersionException {
    new ErlangVersionChecker(config).check();
  }

  private void download() throws DownloadException {
    Downloader downloader = new DownloaderFactory(config).getNewInstance();
    downloader.run();
  }

  private void extract() throws ExtractionException {
    Extractor extractor = new ExtractorFactory(config).getNewInstance();
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
