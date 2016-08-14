package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.StringUtils;

import org.slf4j.Logger;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;

class LoggingProcessListener extends ProcessListener {

  private final Logger logger;

  LoggingProcessListener(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void beforeStart(ProcessExecutor executor) {
    logger.debug("Executing '{}'...", StringUtils.join(executor.getCommand(), " "));
  }

  @Override
  public void afterStart(Process process, ProcessExecutor executor) {
    logger.debug("Process started.");
  }

  @Override
  public void afterFinish(Process process, ProcessResult result) {
    logger.info("Process finished (exit code: {}).", result.getExitValue());
  }

  @Override
  public void afterStop(Process process) {
    logger.debug("Process stopped");
  }
}