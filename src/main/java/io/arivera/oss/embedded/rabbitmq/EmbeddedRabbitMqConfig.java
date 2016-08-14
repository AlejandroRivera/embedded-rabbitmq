package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;
import io.arivera.oss.embedded.rabbitmq.util.SystemUtils;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class EmbeddedRabbitMqConfig {


  private final URL downloadSource;
  private final File downloadTarget;
  private final File extractionFolder;
  private final File appFolder;

  private final long downloadReadTimeoutInMillis;
  private final long downloadConnectionTimeoutInMillis;
  private final long defaultRabbitMqCtlTimeoutInMillis;
  private final long rabbitMqServerInitializationTimeoutInMillis;

  private final boolean shouldCacheDownload;
  private final boolean deleteCachedFileOnErrors;

  protected EmbeddedRabbitMqConfig(URL downloadSource,
                                   File downloadTarget,
                                   File extractionFolder,
                                   File appFolder,
                                   long downloadReadTimeoutInMillis,
                                   long downloadConnectionTimeoutInMillis,
                                   long defaultRabbitMqCtlTimeoutInMillis,
                                   long rabbitMqServerInitializationTimeoutInMillis,
                                   boolean cacheDownload, boolean deleteCachedFile) {
    this.downloadSource = downloadSource;
    this.downloadTarget = downloadTarget;
    this.extractionFolder = extractionFolder;
    this.appFolder = appFolder;
    this.downloadReadTimeoutInMillis = downloadReadTimeoutInMillis;
    this.downloadConnectionTimeoutInMillis = downloadConnectionTimeoutInMillis;
    this.defaultRabbitMqCtlTimeoutInMillis = defaultRabbitMqCtlTimeoutInMillis;
    this.rabbitMqServerInitializationTimeoutInMillis = rabbitMqServerInitializationTimeoutInMillis;
    this.shouldCacheDownload = cacheDownload;
    this.deleteCachedFileOnErrors = deleteCachedFile;
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

  public boolean shouldCachedDownload() {
    return shouldCacheDownload;
  }

  public boolean shouldDeleteCachedFileOnErrors() {
    return deleteCachedFileOnErrors;
  }

  public File getAppFolder() {
    return appFolder;
  }

  public static class Builder {

    private long downloadReadTimeoutInMillis;
    private long downloadConnectionTimeoutInMillis;
    private long defaultRabbitMqCtlTimeoutInMillis;
    private long rabbitMqServerInitializationTimeoutInMillis;
    private URL downloadSource;
    private File downloadFolder;
    private File downloadTarget;
    private File extractionFolder;
    private boolean cacheDownload;
    private boolean deleteCachedFile;
    private PredefinedVersion version;
    private String appFolder;

    public Builder() {
      this.downloadConnectionTimeoutInMillis = TimeUnit.SECONDS.toMillis(2);
      this.downloadReadTimeoutInMillis = TimeUnit.SECONDS.toMillis(3);
      this.defaultRabbitMqCtlTimeoutInMillis = TimeUnit.SECONDS.toMillis(2);
      this.rabbitMqServerInitializationTimeoutInMillis = TimeUnit.SECONDS.toMillis(3);
      this.cacheDownload = true;
      this.deleteCachedFile = true;
      this.downloadFolder = new File(System.getProperty("user.home"), ".embeddedrabbitmq");
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

    public Builder downloadSource(URL downloadSource, String version) {
      if (this.version != null) {
        throw new IllegalStateException("Pre-defined Version has already been set.");
      }
      this.downloadSource = downloadSource;
      this.appFolder = "rabbitmq_server-" + version;
      return this;
    }

    public Builder version(PredefinedVersion version) {
      if (this.downloadSource != null || this.appFolder != null) {
        throw new IllegalStateException("Download source has been set manually.");
      }
      this.version = version;
      return this;
    }

    public Builder downloadFolder(File downloadFolder) {
      if (this.downloadTarget != null) {
        throw new IllegalStateException("Download Target has already been set.");
      }
      this.downloadFolder = downloadFolder;
      return this;
    }

    public Builder downloadTarget(File downloadTarget) {
      if (this.downloadFolder != null) {
        throw new IllegalStateException("Download Folder has already been set.");
      }
      this.downloadTarget = downloadTarget;
      return this;
    }

    public Builder extractionFolder(File extractionFolder) {
      this.extractionFolder = extractionFolder;
      return this;
    }

    public Builder useCachedDownload(boolean cacheDownload) {
      this.cacheDownload = cacheDownload;
      return this;
    }

    public Builder deleteDownloadedFileOnErrors(boolean deleteCachedFile) {
      this.deleteCachedFile = deleteCachedFile;
      return this;
    }

    public EmbeddedRabbitMqConfig build() {
      if (downloadSource == null) {
        if (version == null) {
          version = PredefinedVersion.LATEST;
        }
        downloadSource = version.getPathsProvider().getDownloadUrl(OperatingSystem.detect());
        appFolder = version.getPathsProvider().getExtractionSubFolder();
      }

      if (downloadTarget == null) {
        String filename = downloadSource.getPath().substring(downloadSource.getPath().lastIndexOf("/"));
        this.downloadTarget = new File(downloadFolder, filename);
      }

      if (extractionFolder == null) {
        this.extractionFolder = new File(SystemUtils.JAVA_IO_TMPDIR);
      }

      File appAbsPath = new File(extractionFolder.toString(), appFolder);

      return new EmbeddedRabbitMqConfig(
          downloadSource, downloadTarget, extractionFolder, appAbsPath,
          downloadConnectionTimeoutInMillis, downloadReadTimeoutInMillis,
          defaultRabbitMqCtlTimeoutInMillis,
          rabbitMqServerInitializationTimeoutInMillis,
          cacheDownload, deleteCachedFile);
    }
  }
}
