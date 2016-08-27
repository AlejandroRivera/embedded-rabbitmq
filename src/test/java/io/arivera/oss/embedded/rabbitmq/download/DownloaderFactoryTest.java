package io.arivera.oss.embedded.rabbitmq.download;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DownloaderFactoryTest {

  private EmbeddedRabbitMqConfig.Builder configBuilder;

  @Before
  public void setUp() throws Exception {
    configBuilder = new EmbeddedRabbitMqConfig.Builder();
  }

  @Test
  public void downloaderWithCaching() throws Exception {
    configBuilder.useCachedDownload(true);

    DownloaderFactory downloaderFactory = new DownloaderFactory(configBuilder.build());
    Downloader downloader = downloaderFactory.getNewInstance();

    assertTrue(downloader.getClass().equals(CachedDownloader.class));
  }

  @Test
  public void downloaderWithoutCaching() throws Exception {
    configBuilder.useCachedDownload(false);

    DownloaderFactory downloaderFactory = new DownloaderFactory(configBuilder.build());
    Downloader downloader = downloaderFactory.getNewInstance();

    assertTrue(downloader.getClass().equals(BasicDownloader.class));
  }
}