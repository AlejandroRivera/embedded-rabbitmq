package com.rivera.oss.embedded.rabbitmq;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;

public class LoggingProcessListener extends ProcessListener {

  private final Logger logger;

  public LoggingProcessListener(Logger logger) {
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