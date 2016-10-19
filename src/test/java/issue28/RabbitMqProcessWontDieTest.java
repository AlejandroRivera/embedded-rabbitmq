package issue28;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMq;
import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

public class RabbitMqProcessWontDieTest {

  static EmbeddedRabbitMq rabbitMq;

  @BeforeClass
  public static void init() throws IOException {
    EmbeddedRabbitMqConfig config = new EmbeddedRabbitMqConfig.Builder()
        .rabbitMqServerInitializationTimeoutInMillis(5000)
        .build();
    rabbitMq = new EmbeddedRabbitMq(config);
    rabbitMq.start();
  }

//  @AfterClass
//  public static void destroy() {
//    rabbitMq.stop();
//  }

  @Test
  public void rabbitTest() throws InterruptedException {
    Thread.sleep(10000);
    throw new RuntimeException("fake");
  }
}