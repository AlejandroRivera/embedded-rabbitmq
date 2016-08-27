package io.arivera.oss.embedded.rabbitmq.helpers;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqCommand;
import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqCommandException;
import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class StartupHelper implements Callable<Future<ProcessResult>> {

  public static final String BROKER_STARTUP_COMPLETED = ".*completed with \\d+ plugins.*";
  private final EmbeddedRabbitMqConfig config;

  public StartupHelper(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  /**
   * Starts the RabbitMQ Server and blocks the current thread until the server is confirmed to have started.
   * <p>
   * This is useful to ensure no other interactions happen with the RabbitMQ Server until it's safe to do so
   *
   * @return an unfinished future representing the eventual result of the {@code rabbitmq-server} process running in "foreground".
   * @throws StartupException if anything fails while attempting to start and confirm successful initialization.
   * @see ShutdownHelper
   */
  @Override
  public Future<ProcessResult> call() throws StartupException {
    PatternFinderOutputStream initializationWatcher = new PatternFinderOutputStream(BROKER_STARTUP_COMPLETED);

    // Inform the initializationWatcher if the process ends before the expected output is produced.
    PublishingProcessListener rabbitMqProcessListener = new PublishingProcessListener();
    rabbitMqProcessListener.addSubscriber(initializationWatcher);

    Future<ProcessResult> resultFuture = startProcess(initializationWatcher, rabbitMqProcessListener);
    waitForConfirmation(initializationWatcher);

    return resultFuture;
  }

  private Future<ProcessResult> startProcess(PatternFinderOutputStream initializationWatcher,
                                             PublishingProcessListener rabbitMqProcessListener) {
    Future<ProcessResult> resultFuture;
    try {
      resultFuture = new RabbitMqServer(config)
          .writeOutputTo(initializationWatcher)
          .listeningToEventsWith(rabbitMqProcessListener)
          .start();
    } catch (RabbitMqCommandException e) {
      throw new StartupException("Could not start RabbitMQ Server", e);
    }
    return resultFuture;
  }

  private void waitForConfirmation(PatternFinderOutputStream initializationWatcher) {
    long timeout = config.getRabbitMqServerInitializationTimeoutInMillis();
    boolean match = initializationWatcher.waitForMatch(timeout, TimeUnit.MILLISECONDS);

    if (!match) {
      throw new StartupException(
          "Could not confirm RabbitMQ Server initialization completed successfully within " + timeout + "ms");
    }
  }

  /**
   * Notifies subscribers of process termination so they don't have to rely on blocking {@link Future#get()} of
   * {@link ProcessResult}s, which is returned by {@link RabbitMqCommand}s.
   */
  static class PublishingProcessListener extends ProcessListener {

    interface Subscriber {
      void processFinished(int exitValue);
    }

    private final List<Subscriber> subscribers;

    public PublishingProcessListener(Subscriber... subscribers) {
      this.subscribers = new ArrayList<>(Arrays.asList(subscribers));
    }

    @Override
    public void afterFinish(Process process, ProcessResult result) {
      super.afterFinish(process, result);
      for (Subscriber subscriber : subscribers) {
        subscriber.processFinished(result.getExitValue());
      }
    }

    public void addSubscriber(Subscriber subscriber) {
      this.subscribers.add(subscriber);
    }

  }

  /**
   * An output stream that compares each line with a given pattern.
   *
   * This class offers the ability to wait until the pattern is found or the given amount of time has passed.
   */
  static class PatternFinderOutputStream extends LogOutputStream implements PublishingProcessListener.Subscriber {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatternFinderOutputStream.class);

    private final Pattern pattern;
    private final Semaphore lock;
    private boolean matchFound;

    public PatternFinderOutputStream(String initializationMarkerPattern) {
      this(Pattern.compile(initializationMarkerPattern, Pattern.CASE_INSENSITIVE));
    }

    public PatternFinderOutputStream(Pattern initializationMarkerPattern) {
      try {
        lock = new Semaphore(1);
        lock.acquire();
      } catch (InterruptedException e) {
        throw new IllegalStateException("Could not acquire a lock we create right above?", e);
      }
      pattern = initializationMarkerPattern;
      matchFound = false;
    }

    @Override
    protected void processLine(String line) {
      if (pattern.matcher(line).matches()) {
        LOGGER.trace("Pattern '{}' found in line: {}", pattern, line);
        matchFound = true;
        lock.release();
      }
      LOGGER.trace("Pattern '{}' NOT found in line: {}", pattern, line);
    }

    @Override
    public void processFinished(int exitValue) {
      LOGGER.debug("No more output is expected since process finished (exit code: {})", exitValue);
      lock.release();
    }

    public boolean waitForMatch(long duration, TimeUnit timeUnit) {
      try {
        boolean acquired = lock.tryAcquire(duration, timeUnit);
        if (!acquired) {
          LOGGER.info("Waited for {} {} for pattern '{}' to appear but it didn't.", duration, timeUnit, pattern );
        }
      } catch (InterruptedException e) {
        LOGGER.warn("Error while waiting for process output that matches the pattern '{}'", pattern);
      }
      return isMatchFound();
    }

    public boolean isMatchFound() {
      return matchFound;
    }
  }
}
