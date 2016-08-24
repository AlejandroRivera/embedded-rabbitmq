package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.util.StringUtils;

import org.slf4j.Logger;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;

class LoggingProcessListener extends ProcessListener {

  private final Logger logger;
  private ProcessExecutor executor;

  LoggingProcessListener(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void beforeStart(ProcessExecutor executor) {
    this.executor = executor;
    logger.debug("Executing '{}' with environment vars: {}",
        StringUtils.join(executor.getCommand(), " "), executor.getEnvironment());
  }

  @Override
  public void afterStart(Process process, ProcessExecutor executor) {
    logger.debug("Process started.");
  }

  @Override
  public void afterFinish(Process process, ProcessResult result) {
    assert executor != null;  // "beforeStart()" must be called previously
    try {
      executor.checkExitValue(result);
      logger.debug("Process finished (exit code: {}).", result.getExitValue());
    } catch (InvalidExitValueException e) {
      logger.error("Process finished with unexpected exit code: {}.", result.getExitValue());
    }
  }

  @Override
  public void afterStop(Process process) {
    logger.debug("Process stopped");
  }
}