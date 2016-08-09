package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import java.net.MalformedURLException;
import java.net.URL;

class DefaultPathProvider implements PathsProvider {

  private static final String URL_PATTERN = "http://www.rabbitmq.com/releases/rabbitmq-server/v%s/rabbitmq-server-%s-%s.%s";
  private static final String EXTRACTION_PATH = "rabbitmq_server-%s";

  private final ArchiveType archiveType;
  private final String version;

  public DefaultPathProvider(String version, ArchiveType archiveType) {
    this.version = version;
    this.archiveType = archiveType;
  }

  @Override
  public URL getDownloadUrl(OperatingSystem operatingSystem){
    String url = String.format(URL_PATTERN,
        version,
        operatingSystem.getDownloadNameComponent(),
        version,
        operatingSystem == OperatingSystem.WINDOWS ? "zip" : archiveType.getExtension());
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Download URL is invalid: " + url, e);
    }
  }

  @Override
  public String getExtractionSubFolder(){
    return String.format(EXTRACTION_PATH, version);
  }
}
