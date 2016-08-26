package io.arivera.oss.embedded.rabbitmq.extract;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ExtractorFactoryTest {

  private EmbeddedRabbitMqConfig.Builder builder;

  @Before
  public void setUp() throws Exception {
    builder = new EmbeddedRabbitMqConfig.Builder();
  }

  @Test
  public void withoutCaching() throws Exception {
    builder.useCachedDownload(false);
    Extractor extractor = new ExtractorFactory(builder.build()).getNewInstance();

    assertTrue(extractor.getClass().equals(BasicExtractor.class));
  }

  @Test
  public void withCaching() throws Exception {
    builder.useCachedDownload(true);
    Extractor extractor = new ExtractorFactory(builder.build()).getNewInstance();

    assertTrue(extractor.getClass().equals(CachedExtractor.class));
  }
}