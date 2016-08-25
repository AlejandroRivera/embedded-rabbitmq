package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DefaultPathProvider implements PathsProvider {

  private static final String URL_PATTERN = "http://www.rabbitmq.com/releases/rabbitmq-server/v%s/rabbitmq-server-%s-%s.%s";
  private static final String EXTRACTION_PATH = "rabbitmq_server-%s";

  private static Map<OperatingSystem, String> downloadPlatformName = new HashMap<>(3);

  static {
    downloadPlatformName.put(OperatingSystem.MAC_OS, "mac-standalone");
    downloadPlatformName.put(OperatingSystem.UNIX, "generic-unix");
    downloadPlatformName.put(OperatingSystem.WINDOWS, "windows");
  }

  private final String version;
  private final ArchiveType unixArchiveType;
  private final ArchiveType windowsArchiveType;

  public DefaultPathProvider(String version, ArchiveType unixArchiveType, ArchiveType windowsArchiveType) {
    this.version = version;
    this.unixArchiveType = unixArchiveType;
    this.windowsArchiveType = windowsArchiveType;
  }

  @Override
  public URL getDownloadUrl(OperatingSystem operatingSystem) {
    String extension =
        operatingSystem == OperatingSystem.WINDOWS ? windowsArchiveType.getExtension() : unixArchiveType.getExtension();

    String artifactPlatform = downloadPlatformName.get(operatingSystem);

    String url = String.format(URL_PATTERN, version, artifactPlatform, version, extension);
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Download URL is invalid: " + url, e);
    }
  }

  @Override
  public String getExtractionSubFolder() {
    return String.format(EXTRACTION_PATH, version);
  }
}
