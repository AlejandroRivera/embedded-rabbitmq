package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.PredefinedVersion;
import io.arivera.oss.embedded.rabbitmq.bin.plugins.PluginDetails;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessOutput;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMqPluginsTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock
  private RabbitMqCommand.ProcessExecutorFactory factory;
  @Mock
  private StartedProcess startedProcess;
  @Mock
  private Future futureResult;
  @Mock
  private ProcessOutput processOutput;
  @Mock
  private ProcessResult result;

  private RabbitMqPlugins rabbitMqPlugins;
  private ProcessExecutor processExecutor;
  private File executableFile;

  @Before
  public void setUp() throws Exception {
    EmbeddedRabbitMqConfig embeddedRabbitMqConfig = new EmbeddedRabbitMqConfig.Builder()
        .extractionFolder(tempFolder.getRoot())
        .processExecutorFactory(factory)
        .build();

    this.processExecutor = Mockito.mock(ProcessExecutor.class, new Answer() {
      @Override
      public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
        if (invocationOnMock.getMethod().getName().equals("start")){
          return startedProcess;
        }
        return invocationOnMock.getMock();
      }
    });

    when(factory.createInstance()).thenReturn(processExecutor);

    rabbitMqPlugins = new RabbitMqPlugins(embeddedRabbitMqConfig);

    String appFolder = PredefinedVersion.LATEST.getExtractionFolder();
    File executableFilesFolder = tempFolder.newFolder(appFolder, RabbitMqCommand.BINARIES_FOLDER);
    executableFile = new File(executableFilesFolder, "rabbitmq-plugins" + RabbitMqCommand.getCommandExtension());
    assertTrue("Fake executable file couldn't be created!", executableFile.createNewFile());
  }

  @Test
  public void testListParsing() throws Exception {
    futureResult = mock(Future.class);
    result = mock(ProcessResult.class);
    when(startedProcess.getFuture())
        .thenReturn(futureResult);
    when(futureResult.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(result);
    when(result.getExitValue())
        .thenReturn(0);
    processOutput = mock(ProcessOutput.class);
    when(result.getOutput())
        .thenReturn(processOutput);

    List<String> output = Arrays.asList(
        " Configured: E = explicitly enabled; e = implicitly enabled     ",
        " | Status:   * = running on rabbit@rivera-mbp                   ",
        " |/                                                             ",
        "[e*] amqp_client                       3.5.7                    ",
        "[  ] cowboy                            0.5.0-rmq3.5.7-git4b93c2d",
        "[e*] mochiweb                          2.7.0-rmq3.5.7-git680dba8",
        "[  ] rabbitmq_amqp1_0                  3.5.7                    ",
        "[E*] rabbitmq_management               3.5.7                    ",
        "[e*] rabbitmq_management_agent         3.5.7                    "
    );
    when(processOutput.getLinesAsUTF8())
        .thenReturn(output);

    Map<PluginDetails.PluginState, Set<PluginDetails>> groupedPlugins =
        rabbitMqPlugins.list();

    assertThat(groupedPlugins.get(PluginDetails.PluginState.RUNNING).size(), equalTo(4));
    assertThat(groupedPlugins.get(PluginDetails.PluginState.ENABLED_EXPLICITLY).size(), equalTo(1));
    assertThat(groupedPlugins.get(PluginDetails.PluginState.ENABLED_IMPLICITLY).size(), equalTo(3));
    assertThat(groupedPlugins.get(PluginDetails.PluginState.NOT_ENABLED).size(), equalTo(2));
    assertThat(groupedPlugins.get(PluginDetails.PluginState.NOT_RUNNING).size(), equalTo(2));
  }

  @Test
  public void testUnexpectedExitCode() throws Exception {
    futureResult = mock(Future.class);
    result = mock(ProcessResult.class);
    when(startedProcess.getFuture())
        .thenReturn(futureResult);
    when(futureResult.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(result);
    int exitCode = new Random().nextInt(10) + 1;
    when(result.getExitValue())
        .thenReturn(exitCode);

    expectedException.expect(instanceOf(RabbitMqCommandException.class));
    expectedException.expectMessage("exit code: " + exitCode);

    rabbitMqPlugins.list();
  }

  @Test
  public void testExecutionError() throws Exception {
    futureResult = mock(Future.class);
    result = mock(ProcessResult.class);
    when(startedProcess.getFuture())
        .thenReturn(futureResult);
    TimeoutException timeoutException = new TimeoutException("Fake timeout");
    when(futureResult.get(anyLong(), any(TimeUnit.class)))
        .thenThrow(timeoutException);

    expectedException.expect(instanceOf(RabbitMqCommandException.class));
    expectedException.expectMessage("rabbitmq-plugins list");
    expectedException.expectCause(sameInstance(timeoutException));

    rabbitMqPlugins.list();
  }
}