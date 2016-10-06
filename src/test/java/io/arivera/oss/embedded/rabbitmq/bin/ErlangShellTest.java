package io.arivera.oss.embedded.rabbitmq.bin;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.PredefinedVersion;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class ErlangShellTest {
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private RabbitMqCommand.ProcessExecutorFactory factory = new RabbitMqCommand.ProcessExecutorFactory();

  private PredefinedVersion version = PredefinedVersion.LATEST;
  private EmbeddedRabbitMqConfig.Builder configBuilder;

  @Before
  public void setUp() throws Exception {
    configBuilder = new EmbeddedRabbitMqConfig.Builder()
      .envVars(new HashMap<String, String>())
      .extractionFolder(tempFolder.getRoot())
      .version(this.version)
      .processExecutorFactory(this.factory);
  }

  /**
   * This test can throw all sorts of noise and yet still succeed, because we don't *know* if you have Erlang installed!
   * @throws Exception If things go sideways
   */
  @Test
  public void checkForErlang() throws Exception {
    final ErlangShell shell = new ErlangShell(configBuilder.build());
    String erlangVersion = shell.getErlangVersion();
    assertThat(erlangVersion.isEmpty(), is(false));
    System.out.println(erlangVersion);
  }

}