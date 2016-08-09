package io.arivera.oss.embedded.rabbitmq;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;

public class EmbeddedRabbitMqTest {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EmbeddedRabbitMqTest.class);

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void start() throws Exception {
    EmbeddedRabbitMqConfig config = new EmbeddedRabbitMqConfig.Builder()
//        .downloadFolder(new File(System.getProperty("user.home"), ".embeddedrabbitmq"))
//        .downloadTarget(new File("/tmp/rabbitmq.tar.xz"))
//        .downloadSource(
//            new URL("https://github.com/rabbitmq/rabbitmq-server/releases/download/rabbitmq_v3_6_5/rabbitmq-server-generic-unix-3.6.5.tar.xz"), "3.6.5")
//        .version(PredefinedVersion.LATEST)
        .extractionFolder(temporaryFolder.newFolder("extracted"))
//        .useCachedDownload(false)
        .build();

    EmbeddedRabbitMq rabbitMq = new EmbeddedRabbitMq(config);
    rabbitMq.start();
    LOGGER.info("Back in the test!");

    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost("localhost");
    connectionFactory.setVirtualHost("/");
    connectionFactory.setUsername("guest");
    connectionFactory.setPassword("guest");

    Connection connection = connectionFactory.newConnection();
    assertThat(connection.isOpen(), equalTo(true));
    Channel channel = connection.createChannel();
    assertThat(channel.isOpen(), equalTo(true));

    Thread.sleep(1000);

    channel.close();
    connection.close();

    rabbitMq.stop();
  }

}