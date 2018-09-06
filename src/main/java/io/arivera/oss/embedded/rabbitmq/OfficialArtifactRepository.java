package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A list of the official repositories where RabbitMQ publishes their artifacts.
 *
 * @see EmbeddedRabbitMqConfig.Builder#downloadFrom(ArtifactRepository)
 */
public enum OfficialArtifactRepository implements ArtifactRepository {

  GITHUB("https://github.com/rabbitmq/rabbitmq-server/releases/download/%sv%s/rabbitmq-server-%s-%s.%s", VersionSupport.ANY),
  RABBITMQ("http://www.rabbitmq.com/releases/rabbitmq-server/%sv%s/rabbitmq-server-%s-%s.%s", VersionSupport.BELOW),
  BINTRAY("https://dl.bintray.com/rabbitmq/all/rabbitmq-server/%s%s/rabbitmq-server-%s-%s.%s", VersionSupport.ANY),
  ;

  private static enum VersionSupport {
    BELOW,
    ANY,
    ;
  }

  private static Map<OperatingSystem, String> downloadPlatformName = new HashMap<>(3);
  static {
    downloadPlatformName.put(OperatingSystem.MAC_OS, "mac-standalone");
    downloadPlatformName.put(OperatingSystem.UNIX, "generic-unix");
    downloadPlatformName.put(OperatingSystem.WINDOWS, "windows");
  }

  private final String urlPattern;

  private VersionSupport versionSupport;

  OfficialArtifactRepository(String urlPattern, VersionSupport versionSupport) {
    this.urlPattern = urlPattern;
    this.versionSupport = versionSupport;
  }

  @Override
  public URL getUrl(Version version, OperatingSystem operatingSystem) {

    String artifactPlatform = downloadPlatformName.get(operatingSystem);
    ArchiveType archiveType = version.getArchiveType(operatingSystem);

    String versionAsString = version.getVersionAsString();
    String[] versionParts = versionAsString.split("\\.");

    String prefix = "";
    String stringVersion = versionAsString;
    
    if (Integer.parseInt(versionParts[0]) <= 3 && Integer.parseInt(versionParts[1]) < 7) {
      if (this == GITHUB) {
        stringVersion = String.format("%s_%s_%s", versionParts[0], versionParts[1], versionParts[2]);
        prefix = "rabbitmq_";
      }
    } else {
      if (versionSupport == VersionSupport.BELOW) {
        throw new IllegalStateException(String.format("Repository %s doesn't support versions above 3.7", name()));
      }
    }

    String url = String.format(urlPattern, prefix, stringVersion, artifactPlatform,
        versionAsString, archiveType.getExtension());
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Download URL is invalid: " + url, e);
    }
  }

}
