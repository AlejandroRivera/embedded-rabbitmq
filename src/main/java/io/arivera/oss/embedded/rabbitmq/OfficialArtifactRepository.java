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

  /**
   * @deprecated in favor of {@link #GITHUB} since starting with v3.7.0, this repository is no longer updated.
   *        More info: <a href="http://www.rabbitmq.com/blog/2018/02/05/whats-new-in-rabbitmq-3-7/">Package Distribution Changes</a>.
   */
  @Deprecated
  RABBITMQ("http://www.rabbitmq.com/releases/rabbitmq-server/%sv%s/rabbitmq-server-%s-%s.%s") {
    @Override
    public URL getUrl(Version version, OperatingSystem operatingSystem) {
      if (Version.VERSION_COMPARATOR.compare(version, PredefinedVersion.V3_7_0) >= 0) {
        throw new IllegalStateException(name() + " Repository does not store distributions for "
            + PredefinedVersion.V3_7_0.getVersionAsString() + " or higher. See 'Package Distribution' in "
            + "http://www.rabbitmq.com/blog/2018/02/05/whats-new-in-rabbitmq-3-7/ for more info"
        );
      }

      return super.getUrl(version, operatingSystem);
    }
  },
  GITHUB("https://github.com/rabbitmq/rabbitmq-server/releases/download/%sv%s/rabbitmq-server-%s-%s.%s") {

    @Override
    protected String getFolderPrefix(Version version) {
      if (Version.VERSION_COMPARATOR.compare(version, PredefinedVersion.V3_7_0) < 0) {
        return "rabbitmq_";
      }
      return super.getFolderPrefix(version);
    }

    @Override
    protected String getFolderVersion(Version version) {
      if (Version.VERSION_COMPARATOR.compare(version, PredefinedVersion.V3_7_0) < 0) {
        return version.getVersionAsString("_");
      }
      return super.getFolderVersion(version);
    }

  },
  BINTRAY("https://dl.bintray.com/rabbitmq/all/rabbitmq-server/%s%s/rabbitmq-server-%s-%s.%s"),
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
    String artifactPlatform = getArtifactPlatform(version, operatingSystem);
    ArchiveType archiveType = version.getArchiveType(operatingSystem);

    String filenameVersion = version.getVersionAsString();
    String folderPrefix = getFolderPrefix(version);
    String folderVersion = getFolderVersion(version);

    String url = String.format(urlPattern,
        folderPrefix, folderVersion, artifactPlatform, filenameVersion, archiveType.getExtension());
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Download URL is invalid: " + url, e);
    }
  }

  /**
   * RabbitMQ releases used to include a special binary package for macOS that bundled a supported version of
   * Erlang/OTP. As of September 2019, this package has been discontinued.
   * It will no longer be produced for new RabbitMQ releases.
   * <p/>
   * MacOS users should use the Homebrew formula or the generic binary build (requires a supported version of Erlang
   * to be installed separately) to provision RabbitMQ.
   *
   * @see <a href="https://www.rabbitmq.com/install-standalone-mac.html">Announcement</a>
   */
  protected String getArtifactPlatform(Version version, OperatingSystem operatingSystem) {
    if (operatingSystem == OperatingSystem.MAC_OS && version instanceof PredefinedVersion
        && PredefinedVersion.V3_7_18.compareTo((PredefinedVersion) version) >= 0) {
      // v3.7.18 was the first Sep. 2019 release
      return downloadPlatformName.get(OperatingSystem.UNIX);
    }
    return downloadPlatformName.get(operatingSystem);
  }

  protected String getFolderVersion(Version version) {
    return version.getVersionAsString();
  }

  protected String getFolderPrefix(Version version) {
    return "";
  }

}
