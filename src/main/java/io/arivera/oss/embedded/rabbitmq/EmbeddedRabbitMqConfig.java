package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.SystemUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class EmbeddedRabbitMqConfig {


  private final URL downloadSource;
  private final File downloadTarget;
  private final File extractionFolder;

  private final long downloadReadTimeoutInMillis;
  private final long downloadConnectionTimeoutInMillis;
  private final long defaultRabbitMqCtlTimeoutInMillis;
  private final long rabbitMqServerInitializationTimeoutInMillis;

  protected EmbeddedRabbitMqConfig(URL downloadSource,
                                   File downloadTarget,
                                   File extractionFolder,
                                   long downloadReadTimeoutInMillis,
                                   long downloadConnectionTimeoutInMillis,
                                   long defaultRabbitMqCtlTimeoutInMillis,
                                   long rabbitMqServerInitializationTimeoutInMillis) {
    this.downloadSource = downloadSource;
    this.downloadTarget = downloadTarget;
    this.extractionFolder = extractionFolder;
    this.downloadReadTimeoutInMillis = downloadReadTimeoutInMillis;
    this.downloadConnectionTimeoutInMillis = downloadConnectionTimeoutInMillis;
    this.defaultRabbitMqCtlTimeoutInMillis = defaultRabbitMqCtlTimeoutInMillis;
    this.rabbitMqServerInitializationTimeoutInMillis = rabbitMqServerInitializationTimeoutInMillis;
  }

  public long getDownloadReadTimeoutInMillis() {
    return downloadReadTimeoutInMillis;
  }

  public long getDownloadConnectionTimeoutInMillis() {
    return downloadConnectionTimeoutInMillis;
  }

  public long getDefaultRabbitMqCtlTimeoutInMillis() {
    return defaultRabbitMqCtlTimeoutInMillis;
  }

  public long getRabbitMqServerInitializationTimeoutInMillis() {
    return rabbitMqServerInitializationTimeoutInMillis;
  }

  public URL getDownloadSource() {
    return downloadSource;
  }

  public File getDownloadTarget() {
    return downloadTarget;
  }

  public File getExtractionFolder() {
    return extractionFolder;
  }


  public static class Builder {

    public static final String GENERIC_UNIX_V3_6_4_DOWNLOAD =
        "http://www.rabbitmq.com/releases/rabbitmq-server/v3.6.4/rabbitmq-server-generic-unix-3.6.4.tar.xz";
    public static final String WINDOWS_V3_6_4_DOWNLOAD =
        "http://www.rabbitmq.com/releases/rabbitmq-server/v3.6.4/rabbitmq-server-windows-3.6.4.zip";

    private long downloadReadTimeoutInMillis;
    private long downloadConnectionTimeoutInMillis;
    private long defaultRabbitMqCtlTimeoutInMillis;
    private long rabbitMqServerInitializationTimeoutInMillis;
    private URL downloadSource;
    private File downloadTarget;
    private File extractionFolder;

    public Builder() {
      try {
        this.downloadSource = new URL(SystemUtils.IS_OS_WINDOWS ? WINDOWS_V3_6_4_DOWNLOAD : GENERIC_UNIX_V3_6_4_DOWNLOAD);
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
      this.downloadConnectionTimeoutInMillis = TimeUnit.SECONDS.toMillis(2);
      this.downloadReadTimeoutInMillis = TimeUnit.SECONDS.toMillis(3);
      this.defaultRabbitMqCtlTimeoutInMillis = TimeUnit.SECONDS.toMillis(2);
      this.rabbitMqServerInitializationTimeoutInMillis = TimeUnit.SECONDS.toMillis(3);
    }

    public Builder downloadReadTimeoutInMillis(long downloadReadTimeoutInMillis) {
      this.downloadReadTimeoutInMillis = downloadReadTimeoutInMillis;
      return this;
    }

    public Builder downloadConnectionTimeoutInMillis(long downloadConnectionTimeoutInMillis) {
      this.downloadConnectionTimeoutInMillis = downloadConnectionTimeoutInMillis;
      return this;
    }

    public Builder defaultRabbitMqCtlTimeoutInMillis(long defaultRabbitMqCtlTimeoutInMillis) {
      this.defaultRabbitMqCtlTimeoutInMillis = defaultRabbitMqCtlTimeoutInMillis;
      return this;
    }

    public Builder rabbitMqServerInitializationTimeoutInMillis(long rabbitMqServerInitializationTimeoutInMillis) {
      this.rabbitMqServerInitializationTimeoutInMillis = rabbitMqServerInitializationTimeoutInMillis;
      return this;
    }

    public Builder downloadSource(URL downloadSource) {
      this.downloadSource = downloadSource;
      return this;
    }

    public Builder downloadTarget(File downloadTarget) {
      this.downloadTarget = downloadTarget;
      return this;
    }

    public Builder extractionFolder(File extractionFolder) {
      this.extractionFolder = extractionFolder;
      return this;
    }

    public EmbeddedRabbitMqConfig build() {
      if (downloadTarget == null) {
        String filename = downloadSource.getPath().substring(downloadSource.getPath().lastIndexOf("/"));
        this.downloadTarget = new File(SystemUtils.JAVA_IO_TMPDIR + File.separator + filename);
      }
      if (extractionFolder == null) {
        this.extractionFolder = new File(SystemUtils.JAVA_IO_TMPDIR);
      }

      return new EmbeddedRabbitMqConfig(
          downloadSource, downloadTarget, extractionFolder,
          downloadConnectionTimeoutInMillis, downloadReadTimeoutInMillis,
          defaultRabbitMqCtlTimeoutInMillis,
          rabbitMqServerInitializationTimeoutInMillis);
    }
  }
}
