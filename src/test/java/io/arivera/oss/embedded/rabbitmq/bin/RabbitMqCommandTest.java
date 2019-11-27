package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.PredefinedVersion;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.listener.ProcessListener;
import org.zeroturnaround.exec.stream.slf4j.Level;
import org.zeroturnaround.exec.stream.slf4j.Slf4jErrorOutputStream;
import org.zeroturnaround.exec.stream.slf4j.Slf4jInfoOutputStream;
import org.zeroturnaround.exec.stream.slf4j.Slf4jOutputStream;
import org.zeroturnaround.exec.stream.slf4j.Slf4jWarnOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMqCommandTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqCommandTest.class);

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock
  private RabbitMqCommand.ProcessExecutorFactory factory;

  @Mock
  private StartedProcess startedProcess;

  private ProcessExecutor processExecutor;

  private String command;
  private RabbitMqCommand rabbitMqCommand;
  private File executableFile;
  private PredefinedVersion version;
  private EmbeddedRabbitMqConfig.Builder configBuilder;

  @Before
  public void setUp() throws Exception {
    version = PredefinedVersion.LATEST;
    configBuilder = new EmbeddedRabbitMqConfig.Builder()
        .extractionFolder(tempFolder.getRoot())
        .version(this.version)
        .processExecutorFactory(this.factory);
    command = RandomStringUtils.randomAlphabetic(10);

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

    String appFolder = version.getExtractionFolder();
    File executableFilesFolder = tempFolder.newFolder(appFolder, RabbitMqCommand.BINARIES_FOLDER);
    executableFile = new File(executableFilesFolder, command + RabbitMqCommand.getCommandExtension());
    assertTrue("Fake executable file couldn't be created!", executableFile.createNewFile());
  }

  @Test
  public void whenExecutableIsNotFound() throws Exception {
    executableFile.delete();
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(containsString("could not be found"));
    expectedException.expectMessage(containsString(command));

    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
  }

  @Test
  public void processExecutesCommandWithArguments() throws Exception {
    String[] args = new String[RandomUtils.nextInt(0,10)];
    for(int i = 0; i < args.length; i++){
      args[i] = RandomStringUtils.randomAlphanumeric(5);
    }

    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command, args);
    rabbitMqCommand.call();

    String[] commandAndArgs = ArrayUtils.add(args, 0, executableFile.toString());
    verify(processExecutor).command(Arrays.asList(commandAndArgs));
  }

  @Test
  public void processIncludesEnvironmentVars() throws Exception {
    Map<String, String> envVars = new HashMap<>();
    envVars.put(RandomStringUtils.randomAlphanumeric(5), RandomStringUtils.randomAlphanumeric(10));
    envVars.put(RandomStringUtils.randomAlphanumeric(5), RandomStringUtils.randomAlphanumeric(10));

    configBuilder.envVars(envVars);

    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.call();

    verify(processExecutor).environment(envVars);
  }

  @Test
  public void processIsLaunchedFromAppDirectory() throws Exception {
    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.call();

    File binariesFolder = executableFile.getParentFile();
    File appFolder = binariesFolder.getParentFile();
    verify(processExecutor).directory(appFolder);
  }

  @Test
  public void processEventsAreLogged() throws Exception {
    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.call();

    ArgumentCaptor<ProcessListener> listenerCaptor = ArgumentCaptor.forClass(ProcessListener.class);
    verify(processExecutor, atLeastOnce()).addListener(listenerCaptor.capture());

    List<ProcessListener> listeners = listenerCaptor.getAllValues();
    boolean found = false;
    for (ProcessListener listener : listeners) {
      if (listener instanceof LoggingProcessListener){
        found = true;
        break;
      }
    }
    assertThat("Expected Listener was not found!", found, is(true));
  }

  @Test
  public void processEventsCanBeListenedTo() throws Exception {
    ProcessListener fakeListener = Mockito.mock(ProcessListener.class);

    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.listenToEvents(fakeListener);
    rabbitMqCommand.call();

    ArgumentCaptor<ProcessListener> listenerCaptor = ArgumentCaptor.forClass(ProcessListener.class);
    verify(processExecutor, atLeastOnce()).addListener(listenerCaptor.capture());

    List<ProcessListener> listeners = listenerCaptor.getAllValues();
    assertThat(listeners, hasItem(equalTo(fakeListener)));
  }

  @Test
  public void outputCanBeStreamed() throws Exception {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.writeOutputTo(byteArrayOutputStream);
    rabbitMqCommand.call();

    verify(processExecutor).redirectOutputAlsoTo(byteArrayOutputStream);
  }

  @Test
  public void errorOutputCanBeStreamed() throws Exception {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.writeErrorOutputTo(byteArrayOutputStream);
    rabbitMqCommand.call();

    verify(processExecutor).redirectErrorAlsoTo(byteArrayOutputStream);
  }

  @Test
  public void outputStorageCanBeDisabled() throws Exception {
    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.storeOutput(false);
    rabbitMqCommand.call();

    verify(processExecutor).readOutput(false);
  }

  @Test
  public void outputLoggingLevelsDefaultsToInfo() throws Exception {
    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.call();

    ArgumentCaptor<OutputStream> osCaptor = ArgumentCaptor.forClass(OutputStream.class);
    verify(processExecutor).redirectOutput(osCaptor.capture());

    OutputStream os = osCaptor.getValue();
    assertThat(os, instanceOf(Slf4jInfoOutputStream.class));
  }

  @Test
  public void errorLoggingLevelDefaultsToWarn() throws Exception {
    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.call();

    ArgumentCaptor<OutputStream> osCaptor = ArgumentCaptor.forClass(OutputStream.class);
    verify(processExecutor).redirectError(osCaptor.capture());

    OutputStream os = osCaptor.getValue();
    assertThat(os, instanceOf(Slf4jWarnOutputStream.class));
  }

  @Test
  public void errorLoggingLevelsCanBeChanged() throws Exception {
    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.logStandardErrorOutputAs(Level.ERROR);
    rabbitMqCommand.call();

    ArgumentCaptor<OutputStream> osCaptor = ArgumentCaptor.forClass(OutputStream.class);
    verify(processExecutor).redirectError(osCaptor.capture());

    OutputStream os = osCaptor.getValue();
    assertThat(os, instanceOf(Slf4jErrorOutputStream.class));
  }

  @Test
  public void outputLoggingLevelsCanBeChanged() throws Exception {
    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.logStandardOutputAs(Level.ERROR);
    rabbitMqCommand.call();

    ArgumentCaptor<OutputStream> osCaptor = ArgumentCaptor.forClass(OutputStream.class);
    verify(processExecutor).redirectOutput(osCaptor.capture());

    OutputStream os = osCaptor.getValue();
    assertThat(os, instanceOf(Slf4jErrorOutputStream.class));
  }


  @Test
  public void loggerCanBeChanged() throws Exception {
    Logger logger = Mockito.mock(Logger.class);

    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.logWith(logger);
    rabbitMqCommand.call();

    ArgumentCaptor<OutputStream> osCaptor = ArgumentCaptor.forClass(OutputStream.class);
    verify(processExecutor).redirectOutput(osCaptor.capture());

    OutputStream os = osCaptor.getValue();
    assertThat(os, instanceOf(Slf4jOutputStream.class));

    Logger actualLogger = ((Slf4jOutputStream) os).getLogger();
    assertThat(actualLogger, is(logger));
  }

  @Test
  public void logNameMatchesCommandByDefault() throws Exception {
    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.call();

    ArgumentCaptor<OutputStream> osCaptor = ArgumentCaptor.forClass(OutputStream.class);
    verify(processExecutor).redirectOutput(osCaptor.capture());

    OutputStream os = osCaptor.getValue();
    assertThat(os, instanceOf(Slf4jOutputStream.class));

    Logger logger = ((Slf4jOutputStream) os).getLogger();
    assertThat(logger.getName(), endsWith(command));
  }

  @Test
  public void processStartErrorIsWrapped() throws Exception {
    IOException fakeException = new IOException("fake!");
    when(processExecutor.start())
        .thenThrow(fakeException);

    expectedException.expect(RabbitMqCommandException.class);
    expectedException.expectMessage(containsString(command));
    expectedException.expectCause(equalTo(fakeException));

    rabbitMqCommand = new RabbitMqCommand(configBuilder.build(), command);
    rabbitMqCommand.call();
  }

  @Test
  public void defaultProcessExecutor(){
    ProcessExecutor executor = new RabbitMqCommand.ProcessExecutorFactory().createInstance();
    assertThat(executor, instanceOf(ProcessExecutor.class));
  }

  @Test
  public void defaultConstructor() throws Exception {
    new RabbitMqCommand(configBuilder.build(), command);
  }
}