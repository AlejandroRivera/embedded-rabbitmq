package io.arivera.oss.embedded.rabbitmq.bin;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LoggingProcessListenerTest {

  @Mock
  Logger logger;
  private ProcessExecutor processExecutor;
  private String command;
  private LoggingProcessListener loggingProcessListener;

  @Before
  public void setUp() throws Exception {
    command = RandomStringUtils.randomAlphabetic(5);
    processExecutor = new ProcessExecutor(command);

    loggingProcessListener = new LoggingProcessListener(logger);
    loggingProcessListener.beforeStart(processExecutor);
  }

  @Test
  public void logsCommandToExecute() throws Exception {
    ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> commandCaptor = ArgumentCaptor.forClass(String.class);
    verify(logger).debug(msgCaptor.capture(), commandCaptor.capture(), anyString());
    assertThat(msgCaptor.getValue().toLowerCase(), containsString("executing"));
    assertThat(msgCaptor.getValue().toLowerCase(), containsString("env"));
    assertThat(msgCaptor.getValue().toLowerCase(), containsString("vars"));
    assertThat(commandCaptor.getValue(), containsString(command));
  }

  @Test
  public void expectedExitValueDoesNotLogError() throws Exception {
    processExecutor.exitValue(0);

    loggingProcessListener.afterFinish(null, new ProcessResult(0, null));

    ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
    verify(logger).debug(msgCaptor.capture(), Matchers.any());
    assertThat(msgCaptor.getValue().toLowerCase(), containsString("process finished"));

    verify(logger, never()).error(anyString(), anyString());
  }

  @Test
  public void expectedExitValueDoesLogError() throws Exception {
    String command = RandomStringUtils.randomAlphabetic(5);
    ProcessExecutor processExecutor = new ProcessExecutor(command).exitValue(0);
    LoggingProcessListener loggingProcessListener = new LoggingProcessListener(logger);
    loggingProcessListener.beforeStart(processExecutor);
    loggingProcessListener.afterFinish(null, new ProcessResult(1, null));

    ArgumentCaptor<String> msg = ArgumentCaptor.forClass(String.class);
    verify(logger).error(msg.capture(), anyString());

    assertThat(msg.getValue(), containsString("unexpected exit code"));
  }
}