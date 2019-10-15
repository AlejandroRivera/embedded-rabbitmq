package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import org.junit.Test;

import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class OfficialArtifactRepositoryTest {

  @Test
  public void downloadForWindows() throws Exception {
    URL url = OfficialArtifactRepository.RABBITMQ
        .getUrl(PredefinedVersion.V3_6_5, OperatingSystem.WINDOWS);

    assertThat(url.toString(),
        equalTo("http://www.rabbitmq.com/releases/rabbitmq-server"
            + "/v3.6.5/rabbitmq-server-windows-3.6.5.zip"));
  }

  @Test
  public void downloadForMac() throws Exception {
    URL url = OfficialArtifactRepository.RABBITMQ
        .getUrl(PredefinedVersion.V3_6_5, OperatingSystem.MAC_OS);

    assertThat(url.toString(),
        equalTo("http://www.rabbitmq.com/releases/rabbitmq-server"
            + "/v3.6.5/rabbitmq-server-mac-standalone-3.6.5.tar.xz"));
  }

  @Test
  public void downloadForUnix() throws Exception {
    URL url = OfficialArtifactRepository.RABBITMQ
        .getUrl(PredefinedVersion.V3_6_5, OperatingSystem.UNIX);

    assertThat(url.toString(),
        equalTo("http://www.rabbitmq.com/releases/rabbitmq-server"
            + "/v3.6.5/rabbitmq-server-generic-unix-3.6.5.tar.xz"));
  }

  @Test
  public void githubRepoOldForMac() throws Exception {
    URL url = OfficialArtifactRepository.GITHUB
        .getUrl(PredefinedVersion.V3_6_5, OperatingSystem.MAC_OS);

    assertThat(url.toString(),
        equalTo("https://github.com/rabbitmq/rabbitmq-server/releases/download"
            + "/rabbitmq_v3_6_5/rabbitmq-server-mac-standalone-3.6.5.tar.xz"));
  }

  @Test
  public void githubRepoNewForMac() throws Exception {
      URL url = OfficialArtifactRepository.GITHUB
          .getUrl(PredefinedVersion.V3_7_7, OperatingSystem.MAC_OS);

      assertThat(url.toString(),
          equalTo("https://github.com/rabbitmq/rabbitmq-server/releases/download"
              + "/v3.7.7/rabbitmq-server-mac-standalone-3.7.7.tar.xz"));
  }

  @Test
  public void githubRepoNewForMacAfterV3_7_18() throws Exception {
    URL url = OfficialArtifactRepository.GITHUB
        .getUrl(PredefinedVersion.V3_7_18, OperatingSystem.MAC_OS);

    assertThat(url.toString(),
        equalTo("https://github.com/rabbitmq/rabbitmq-server/releases/download"
            + "/v3.7.18/rabbitmq-server-generic-unix-3.7.18.tar.xz"));

    url = OfficialArtifactRepository.GITHUB
        .getUrl(PredefinedVersion.V3_8_0, OperatingSystem.MAC_OS);

    assertThat(url.toString(),
        equalTo("https://github.com/rabbitmq/rabbitmq-server/releases/download"
            + "/v3.8.0/rabbitmq-server-generic-unix-3.8.0.tar.xz"));
  }

  @Test
  public void githubRepoOldForUnix() throws Exception {
    URL url = OfficialArtifactRepository.GITHUB
        .getUrl(PredefinedVersion.V3_6_13, OperatingSystem.UNIX);

    assertThat(url.toString(),
        equalTo("https://github.com/rabbitmq/rabbitmq-server/releases/download"
            + "/rabbitmq_v3_6_13/rabbitmq-server-generic-unix-3.6.13.tar.xz"));
  }

  @Test
  public void githubRepoNewForUnix() throws Exception {
      URL url = OfficialArtifactRepository.GITHUB
          .getUrl(PredefinedVersion.V3_7_3, OperatingSystem.UNIX);

      assertThat(url.toString(),
          equalTo("https://github.com/rabbitmq/rabbitmq-server/releases/download"
              + "/v3.7.3/rabbitmq-server-generic-unix-3.7.3.tar.xz"));
  }

  @Test
  public void bintrayRepoNewForMac() throws Exception {
      URL url = OfficialArtifactRepository.BINTRAY
          .getUrl(PredefinedVersion.V3_7_7, OperatingSystem.MAC_OS);

      assertThat(url.toString(),
          equalTo("https://dl.bintray.com/rabbitmq/all/rabbitmq-server"
              + "/3.7.7/rabbitmq-server-mac-standalone-3.7.7.tar.xz"));
  }

  @Test(expected = IllegalStateException.class)
  public void rabbitMqRepoWontGenerateUrlForVersion3_7andHigher() throws Exception {
    OfficialArtifactRepository.RABBITMQ.getUrl(PredefinedVersion.V3_7_0, null);
  }

  @Test
  public void rabbitMqRepoWillGenerateUrlForVersionsBelow3_7() {
    URL url = OfficialArtifactRepository.RABBITMQ.getUrl(PredefinedVersion.V3_6_13, null);
    assertNotNull(url);
  }
}