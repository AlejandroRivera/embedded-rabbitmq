package com.sample.project;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMq;
import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.OfficialArtifactRepository;
import io.arivera.oss.embedded.rabbitmq.PredefinedVersion;
import io.arivera.oss.embedded.rabbitmq.RabbitMqEnvVar;
import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqCtl;
import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqPlugins;
import io.arivera.oss.embedded.rabbitmq.bin.plugins.Plugin;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class EmbeddedRabbitMqTest {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EmbeddedRabbitMqTest.class);
  public static final int PORT = 5673;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private EmbeddedRabbitMq rabbitMq;

  @Test
  public void start() throws Exception {
    File configFile = temporaryFolder.newFile("rabbitmq.config");
    FileOutputStream fileOutputStream = new FileOutputStream(configFile);
    fileOutputStream.write(String.format("[{rabbit, [{tcp_listeners, [%d]}]}].", PORT).getBytes("utf-8"));
    fileOutputStream.close();

    EmbeddedRabbitMqConfig config = new EmbeddedRabbitMqConfig.Builder()
        .version(PredefinedVersion.V3_5_7)
          .downloadFrom(OfficialArtifactRepository.RABBITMQ)
  //        .downloadFrom(new URL("https://github.com/rabbitmq/rabbitmq-server/releases/download/rabbitmq_v3_6_6_milestone1/rabbitmq-server-mac-standalone-3.6.5.901.tar.xz"), "rabbitmq_server-3.6.5.901")
//        .envVar(RabbitMqEnvVar.NODE_PORT, String.valueOf(PORT))
        .envVar(RabbitMqEnvVar.CONFIG_FILE, configFile.toString().replace(".config", ""))
        .extractionFolder(temporaryFolder.newFolder("extracted"))
        .rabbitMqServerInitializationTimeoutInMillis(TimeUnit.SECONDS.toMillis(5))
        .defaultRabbitMqCtlTimeoutInMillis(TimeUnit.SECONDS.toMillis(3))
//        .useCachedDownload(false)
        .build();

    rabbitMq = new EmbeddedRabbitMq(config);
    rabbitMq.start();
    LOGGER.info("Back in the test!");

    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost("localhost");
    connectionFactory.setPort(PORT);
    connectionFactory.setVirtualHost("/");
    connectionFactory.setUsername("guest");
    connectionFactory.setPassword("guest");

    Connection connection = connectionFactory.newConnection();
    assertThat(connection.isOpen(), equalTo(true));
    Channel channel = connection.createChannel();
    assertThat(channel.isOpen(), equalTo(true));

    ProcessResult listUsersResult = new RabbitMqCtl(config)
        .execute("list_users")
        .get();

    assertThat(listUsersResult.getExitValue(), is(0));
    assertThat(listUsersResult.getOutput().getString(), containsString("guest"));

    RabbitMqPlugins rabbitMqPlugins = new RabbitMqPlugins(config);
    Map<Plugin.State, Set<Plugin>> groupedPlugins = rabbitMqPlugins.groupedList();
    assertThat(groupedPlugins.get(Plugin.State.ENABLED_EXPLICITLY).size(), equalTo(0));

    rabbitMqPlugins.enable("rabbitmq_management");

    Plugin plugin = rabbitMqPlugins.list().get("rabbitmq_management");
    assertThat(plugin, is(notNullValue()));
    assertThat(plugin.getState(),
        hasItems(Plugin.State.ENABLED_EXPLICITLY, Plugin.State.RUNNING));

    HttpURLConnection urlConnection = (HttpURLConnection) new URL("http://localhost:15672").openConnection();
    urlConnection.setRequestMethod("GET");
    urlConnection.connect();

    assertThat(urlConnection.getResponseCode(), equalTo(200));
    urlConnection.disconnect();

    rabbitMqPlugins.disable("rabbitmq_management");

    channel.close();
    connection.close();
  }

  @After
  public void tearDown() throws Exception {
    rabbitMq.stop();
  }
}