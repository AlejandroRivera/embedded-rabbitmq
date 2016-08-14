package io.arivera.oss.embedded.rabbitmq.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//import DurationFormatUtils;

/**
 * Copied from Apache Commons Lang3 v3.4
 */
@SuppressWarnings("all")
public class StopWatch {

  private static final long NANO_2_MILLIS = 1000000L;

  /**
   * Enumeration type which indicates the status of stopwatch.
   */
  private enum State {

    UNSTARTED {
      @Override boolean isStarted() { return false; }
      @Override boolean isStopped() { return true;  }
      @Override boolean isSuspended() { return false; }
    },
    RUNNING {
      @Override boolean isStarted() { return true; }
      @Override boolean isStopped() { return false; }
      @Override boolean isSuspended() { return false; }
    },
    STOPPED {
      @Override boolean isStarted() { return false; }
      @Override boolean isStopped() { return true; }
      @Override boolean isSuspended() { return false; }
    },
    SUSPENDED {
      @Override boolean isStarted() { return true; }
      @Override boolean isStopped() { return false; }
      @Override  boolean isSuspended() { return true; }
    };

    /**
     * <p>
     * The method is used to find out if the StopWatch is started. A suspended
     * StopWatch is also started watch.
     * </p>

     * @return boolean
     *             If the StopWatch is started.
     */
    abstract boolean isStarted();

    /**
     * <p>
     * This method is used to find out whether the StopWatch is stopped. The
     * stopwatch which's not yet started and explicitly stopped stopwatch is
     * considered as stopped.
     * </p>
     *
     * @return boolean
     *             If the StopWatch is stopped.
     */
    abstract boolean isStopped();

    /**
     * <p>
     * This method is used to find out whether the StopWatch is suspended.
     * </p>
     *
     * @return boolean
     *             If the StopWatch is suspended.
     */
    abstract boolean isSuspended();
  }

  /**
   * Enumeration type which indicates the split status of stopwatch.
   */
  private enum SplitState {
    SPLIT,
    UNSPLIT
  }
  /**
   * The current running state of the StopWatch.
   */
  private StopWatch.State runningState = StopWatch.State.UNSTARTED;

  /**
   * Whether the stopwatch has a split time recorded.
   */
  private StopWatch.SplitState splitState = StopWatch.SplitState.UNSPLIT;

  /**
   * The start time.
   */
  private long startTime;

  /**
   * The start time in Millis - nanoTime is only for elapsed time so we
   * need to also store the currentTimeMillis to maintain the old
   * getStartTime API.
   */
  private long startTimeMillis;

  /**
   * The stop time.
   */
  private long stopTime;

  /**
   * <p>
   * Constructor.
   * </p>
   */
  public StopWatch() {
    super();
  }

  /**
   * <p>
   * Start the stopwatch.
   * </p>
   *
   * <p>
   * This method starts a new timing session, clearing any previous values.
   * </p>
   *
   * @throws IllegalStateException
   *             if the StopWatch is already running.
   */
  public void start() {
    if (this.runningState == StopWatch.State.STOPPED) {
      throw new IllegalStateException("StopWatch must be reset before being restarted. ");
    }
    if (this.runningState != StopWatch.State.UNSTARTED) {
      throw new IllegalStateException("StopWatch already started. ");
    }
    this.startTime = System.nanoTime();
    this.startTimeMillis = System.currentTimeMillis();
    this.runningState = StopWatch.State.RUNNING;
  }


  /**
   * <p>
   * Stop the stopwatch.
   * </p>
   *
   * <p>
   * This method ends a new timing session, allowing the time to be retrieved.
   * </p>
   *
   * @throws IllegalStateException
   *             if the StopWatch is not running.
   */
  public void stop() {
    if (this.runningState != StopWatch.State.RUNNING && this.runningState != StopWatch.State.SUSPENDED) {
      throw new IllegalStateException("StopWatch is not running. ");
    }
    if (this.runningState == StopWatch.State.RUNNING) {
      this.stopTime = System.nanoTime();
    }
    this.runningState = StopWatch.State.STOPPED;
  }

  /**
   * <p>
   * Resets the stopwatch. Stops it if need be.
   * </p>
   *
   * <p>
   * This method clears the internal values to allow the object to be reused.
   * </p>
   */
  public void reset() {
    this.runningState = StopWatch.State.UNSTARTED;
    this.splitState = StopWatch.SplitState.UNSPLIT;
  }

  /**
   * <p>
   * Split the time.
   * </p>
   *
   * <p>
   * This method sets the stop time of the watch to allow a time to be extracted. The start time is unaffected,
   * enabling {@link #unsplit()} to continue the timing from the original start point.
   * </p>
   *
   * @throws IllegalStateException
   *             if the StopWatch is not running.
   */
  public void split() {
    if (this.runningState != StopWatch.State.RUNNING) {
      throw new IllegalStateException("StopWatch is not running. ");
    }
    this.stopTime = System.nanoTime();
    this.splitState = StopWatch.SplitState.SPLIT;
  }

  /**
   * <p>
   * Remove a split.
   * </p>
   *
   * <p>
   * This method clears the stop time. The start time is unaffected, enabling timing from the original start point to
   * continue.
   * </p>
   *
   * @throws IllegalStateException
   *             if the StopWatch has not been split.
   */
  public void unsplit() {
    if (this.splitState != StopWatch.SplitState.SPLIT) {
      throw new IllegalStateException("StopWatch has not been split. ");
    }
    this.splitState = StopWatch.SplitState.UNSPLIT;
  }

  /**
   * <p>
   * Suspend the stopwatch for later resumption.
   * </p>
   *
   * <p>
   * This method suspends the watch until it is resumed. The watch will not include time between the suspend and
   * resume calls in the total time.
   * </p>
   *
   * @throws IllegalStateException
   *             if the StopWatch is not currently running.
   */
  public void suspend() {
    if (this.runningState != StopWatch.State.RUNNING) {
      throw new IllegalStateException("StopWatch must be running to suspend. ");
    }
    this.stopTime = System.nanoTime();
    this.runningState = StopWatch.State.SUSPENDED;
  }

  /**
   * <p>
   * Resume the stopwatch after a suspend.
   * </p>
   *
   * <p>
   * This method resumes the watch after it was suspended. The watch will not include time between the suspend and
   * resume calls in the total time.
   * </p>
   *
   * @throws IllegalStateException
   *             if the StopWatch has not been suspended.
   */
  public void resume() {
    if (this.runningState != StopWatch.State.SUSPENDED) {
      throw new IllegalStateException("StopWatch must be suspended to resume. ");
    }
    this.startTime += System.nanoTime() - this.stopTime;
    this.runningState = StopWatch.State.RUNNING;
  }

  /**
   * <p>
   * Get the time on the stopwatch.
   * </p>
   *
   * <p>
   * This is either the time between the start and the moment this method is called, or the amount of time between
   * start and stop.
   * </p>
   *
   * @return the time in milliseconds
   */
  public long getTime() {
    return getNanoTime() / NANO_2_MILLIS;
  }
  /**
   * <p>
   * Get the time on the stopwatch in nanoseconds.
   * </p>
   *
   * <p>
   * This is either the time between the start and the moment this method is called, or the amount of time between
   * start and stop.
   * </p>
   *
   * @return the time in nanoseconds
   * @since 3.0
   */
  public long getNanoTime() {
    if (this.runningState == StopWatch.State.STOPPED || this.runningState == StopWatch.State.SUSPENDED) {
      return this.stopTime - this.startTime;
    } else if (this.runningState == StopWatch.State.UNSTARTED) {
      return 0;
    } else if (this.runningState == StopWatch.State.RUNNING) {
      return System.nanoTime() - this.startTime;
    }
    throw new RuntimeException("Illegal running state has occurred.");
  }

  /**
   * <p>
   * Get the split time on the stopwatch.
   * </p>
   *
   * <p>
   * This is the time between start and latest split.
   * </p>
   *
   * @return the split time in milliseconds
   *
   * @throws IllegalStateException
   *             if the StopWatch has not yet been split.
   * @since 2.1
   */
  public long getSplitTime() {
    return getSplitNanoTime() / NANO_2_MILLIS;
  }
  /**
   * <p>
   * Get the split time on the stopwatch in nanoseconds.
   * </p>
   *
   * <p>
   * This is the time between start and latest split.
   * </p>
   *
   * @return the split time in nanoseconds
   *
   * @throws IllegalStateException
   *             if the StopWatch has not yet been split.
   * @since 3.0
   */
  public long getSplitNanoTime() {
    if (this.splitState != StopWatch.SplitState.SPLIT) {
      throw new IllegalStateException("StopWatch must be split to get the split time. ");
    }
    return this.stopTime - this.startTime;
  }

  /**
   * Returns the time this stopwatch was started.
   *
   * @return the time this stopwatch was started
   * @throws IllegalStateException
   *             if this StopWatch has not been started
   * @since 2.4
   */
  public long getStartTime() {
    if (this.runningState == StopWatch.State.UNSTARTED) {
      throw new IllegalStateException("StopWatch has not been started");
    }
    // System.nanoTime is for elapsed time
    return this.startTimeMillis;
  }
//
//  /**
//   * <p>
//   * Gets a summary of the time that the stopwatch recorded as a string.
//   * </p>
//   *
//   * <p>
//   * The format used is ISO 8601-like, <i>hours</i>:<i>minutes</i>:<i>seconds</i>.<i>milliseconds</i>.
//   * </p>
//   *
//   * @return the time as a String
//   */
//  @Override
//  public String toString() {
//    return DurationFormatUtils.formatDurationHMS(getTime());
//  }
//
//  /**
//   * <p>
//   * Gets a summary of the split time that the stopwatch recorded as a string.
//   * </p>
//   *
//   * <p>
//   * The format used is ISO 8601-like, <i>hours</i>:<i>minutes</i>:<i>seconds</i>.<i>milliseconds</i>.
//   * </p>
//   *
//   * @return the split time as a String
//   * @since 2.1
//   */
//  public String toSplitString() {
//    return DurationFormatUtils.formatDurationHMS(getSplitTime());
//  }

  /**
   * <p>
   * The method is used to find out if the StopWatch is started. A suspended
   * StopWatch is also started watch.
   * </p>
   *
   * @return boolean
   *             If the StopWatch is started.
   * @since 3.2
   */
  public boolean isStarted() {
    return runningState.isStarted();
  }

  /**
   * <p>
   * This method is used to find out whether the StopWatch is suspended.
   * </p>
   *
   * @return boolean
   *             If the StopWatch is suspended.
   * @since 3.2
   */
  public boolean isSuspended() {
    return runningState.isSuspended();
  }

  /**
   * <p>
   * This method is used to find out whether the StopWatch is stopped. The
   * stopwatch which's not yet started and explicitly stopped stopwatch is
   * considered as stopped.
   * </p>
   *
   * @return boolean
   *             If the StopWatch is stopped.
   * @since 3.2
   */
  public boolean isStopped() {
    return runningState.isStopped();
  }

}

