package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public enum OfficialArtifactRepository implements ArtifactRepository {

  RABBITMQ("http://www.rabbitmq.com/releases/rabbitmq-server/v%s.%s.%s/rabbitmq-server-%s-%s.%s"),
  GITHUB("https://github.com/rabbitmq/rabbitmq-server/releases/download/rabbitmq_v%s_%s_%s/rabbitmq-server-%s-%s.%s"),
  ;

  private static Map<OperatingSystem, String> downloadPlatformName = new HashMap<>(3);
  static {
    downloadPlatformName.put(OperatingSystem.MAC_OS, "mac-standalone");
    downloadPlatformName.put(OperatingSystem.UNIX, "generic-unix");
    downloadPlatformName.put(OperatingSystem.WINDOWS, "windows");
  }

  private final String urlPattern;

  OfficialArtifactRepository(String urlPattern) {
    this.urlPattern = urlPattern;
  }

  @Override
  public URL getUrl(Version version, OperatingSystem operatingSystem) {

    String artifactPlatform = downloadPlatformName.get(operatingSystem);
    ArchiveType archiveType = version.getArchiveType(operatingSystem);

    String versionAsString = version.getVersionAsString();
    String[] versionParts = versionAsString.split("\\.");
    String url = String.format(urlPattern, versionParts[0], versionParts[1], versionParts[2], artifactPlatform,
        versionAsString, archiveType.getExtension());
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Download URL is invalid: " + url, e);
    }
  }

}
