package com.rivera.oss.embedded.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

class PatternFinderOutputStream extends LogOutputStream implements PublishingProcessListener.Subscriber {

  private static final Logger LOGGER = LoggerFactory.getLogger(PatternFinderOutputStream.class);

  private final Pattern pattern;
  private final Semaphore lock;
  private boolean matchFound;

  public PatternFinderOutputStream(String initializationMarkerPattern) {
    this(Pattern.compile(initializationMarkerPattern, Pattern.CASE_INSENSITIVE));
  }

  public PatternFinderOutputStream(Pattern initializationMarkerPattern) {
    lock = new Semaphore(1);
    lock.tryAcquire();
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
        LOGGER.warn("Waited for {} {} for pattern '{}' to appear but it didn't. Will attempt to continue as usual...",
            duration, timeUnit, pattern );
      }
    } catch (InterruptedException e) {
      LOGGER.warn("Error while waiting for process output that matches the pattern '{}'", pattern);
    }
    return matchFound;
  }
}
