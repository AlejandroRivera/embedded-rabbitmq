package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import org.junit.Test;

import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class OfficialArtifactRepositoryTest {

  @Test
  public void downloadForWindows() throws Exception {
    URL url = OfficialArtifactRepository.RABBITMQ
        .getUrl(PredefinedVersion.V3_6_5, OperatingSystem.WINDOWS);

    assertThat(url.toString(),
        equalTo("http://www.rabbitmq.com/releases/rabbitmq-server/v3.6.5/rabbitmq-server-windows-3.6.5.zip"));
  }

  @Test
  public void downloadForMac() throws Exception {
    URL url = OfficialArtifactRepository.RABBITMQ
        .getUrl(PredefinedVersion.V3_6_5, OperatingSystem.MAC_OS);

    assertThat(url.toString(),
        equalTo("http://www.rabbitmq.com/releases/rabbitmq-server/v3.6.5/rabbitmq-server-mac-standalone-3.6.5.tar.xz"));
  }

  @Test
  public void downloadForUnix() throws Exception {
    URL url = OfficialArtifactRepository.RABBITMQ
        .getUrl(PredefinedVersion.V3_6_5, OperatingSystem.UNIX);

    assertThat(url.toString(),
        equalTo("http://www.rabbitmq.com/releases/rabbitmq-server/v3.6.5/rabbitmq-server-generic-unix-3.6.5.tar.xz"));
  }

  @Test
  public void githubRepo() throws Exception {
    URL url = OfficialArtifactRepository.GITHUB
        .getUrl(PredefinedVersion.V3_6_5, OperatingSystem.MAC_OS);

    assertThat(url.toString(),
        equalTo("https://github.com/rabbitmq/rabbitmq-server/releases/"
            + "download/rabbitmq_v3_6_5/rabbitmq-server-mac-standalone-3.6.5.tar.xz"));
  }

}