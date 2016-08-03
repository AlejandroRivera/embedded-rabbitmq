package io.arivera.oss.embedded.rabbitmq;

import org.slf4j.Logger;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;

class LoggingProcessListenerDecorator extends ProcessListener {

  private final LoggingProcessListener loggingProcessListener;
  private final ProcessListener inner;

  public LoggingProcessListenerDecorator(Logger logger, ProcessListener inner) {
    this(inner, new LoggingProcessListener(logger));
  }

  public LoggingProcessListenerDecorator(ProcessListener inner, LoggingProcessListener loggingProcessListener) {
    this.loggingProcessListener = loggingProcessListener;
    this.inner = inner;
  }

  @Override
  public void beforeStart(ProcessExecutor executor) {
    loggingProcessListener.beforeStart(executor);
    inner.beforeStart(executor);
  }

  @Override
  public void afterStart(Process process, ProcessExecutor executor) {
    loggingProcessListener.afterStart(process, executor);
    inner.afterStart(process, executor);
  }

  @Override
  public void afterFinish(Process process, ProcessResult result) {
    loggingProcessListener.afterFinish(process, result);
    inner.afterFinish(process, result);
  }

  @Override
  public void afterStop(Process process) {
    loggingProcessListener.afterStop(process);
    inner.afterStop(process);
  }
}
