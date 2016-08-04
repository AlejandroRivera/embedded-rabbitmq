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

import java.net.URL;

public class EmbeddedRabbitMqTest {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EmbeddedRabbitMqTest.class);

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void start() throws Exception {
    EmbeddedRabbitMqConfig config = new EmbeddedRabbitMqConfig.Builder()
        .downloadTarget(temporaryFolder.newFile("download"))
        .extractionFolder(temporaryFolder.newFolder("extracted"))
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

    Thread.sleep(2000);

    channel.close();
    connection.close();

    rabbitMq.stop();
  }

}