package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.apache.commons.lang3.SystemUtils;
import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqCommand;
import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqCtl;
import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqPlugins;
import io.arivera.oss.embedded.rabbitmq.bin.RabbitMqServer;
import io.arivera.oss.embedded.rabbitmq.util.OperatingSystem;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Immutable configuration used to control all interactions with RabbitMQ broker and respective commands.
 * <p>
 * Use {@link EmbeddedRabbitMqConfig.Builder} to construct a new instance.
 *
 * Once you have created an instance, you can use it to create a new instance of {@link EmbeddedRabbitMq}
 * or {@link RabbitMqCommand} or any of the helpers, like {@link RabbitMqPlugins}.
 *
 * @see EmbeddedRabbitMqConfig.Builder
 * @see EmbeddedRabbitMq
 * @see RabbitMqCommand
 * @see RabbitMqCtl
 * @see RabbitMqPlugins
 * @see RabbitMqServer
 */
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

  private final Map<String, String> envVars;
  private final RabbitMqCommand.ProcessExecutorFactory processExecutorFactory;

  protected EmbeddedRabbitMqConfig(URL downloadSource,
                                   File downloadTarget,
                                   File extractionFolder,
                                   File appFolder,
                                   long downloadReadTimeoutInMillis,
                                   long downloadConnectionTimeoutInMillis,
                                   long defaultRabbitMqCtlTimeoutInMillis,
                                   long rabbitMqServerInitializationTimeoutInMillis,
                                   boolean cacheDownload, boolean deleteCachedFile,
                                   Map<String, String> envVars,
                                   RabbitMqCommand.ProcessExecutorFactory processExecutorFactory) {
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
    this.envVars = envVars;
    this.processExecutorFactory = processExecutorFactory;
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

  public Map<String, String> getEnvVars() {
    return envVars;
  }

  public RabbitMqCommand.ProcessExecutorFactory getProcessExecutorFactory() {
    return processExecutorFactory;
  }

  /**
   * A user-friendly way to create a new {@link EmbeddedRabbitMqConfig} instance.
   * <p>
   * Example use:
   * <pre>
   * {@code
   * EmbeddedRabbitMqConfig config = new EmbeddedRabbitMqConfig.Builder()
   *      .version(...);
   *      .downloadFrom(...)
   *      ...
   *      .build();
   * }
   * </pre>
   *
   * @see EmbeddedRabbitMqConfig
   */
  public static class Builder {

    public static final String DOWNLOAD_FOLDER = ".embeddedrabbitmq";
    private long downloadReadTimeoutInMillis;
    private long downloadConnectionTimeoutInMillis;
    private long defaultRabbitMqCtlTimeoutInMillis;
    private long rabbitMqServerInitializationTimeoutInMillis;
    private File downloadFolder;
    private File downloadTarget;
    private File extractionFolder;
    private boolean cacheDownload;
    private boolean deleteCachedFile;
    private Version version;
    private Map<String, String> envVars;
    private ArtifactRepository artifactRepository;
    private RabbitMqCommand.ProcessExecutorFactory processExecutorFactory;

    /**
     * Creates a new instance of the Configuration Builder.
     * <p>
     * This class uses the Builder pattern where you can chain several methods and invoke {@link #build()} at the end.
     */
    public Builder() {
      this.downloadConnectionTimeoutInMillis = TimeUnit.SECONDS.toMillis(2);
      this.downloadReadTimeoutInMillis = TimeUnit.SECONDS.toMillis(3);
      this.defaultRabbitMqCtlTimeoutInMillis = TimeUnit.SECONDS.toMillis(2);
      this.rabbitMqServerInitializationTimeoutInMillis = TimeUnit.SECONDS.toMillis(3);
      this.cacheDownload = true;
      this.deleteCachedFile = true;
      this.downloadFolder = new File(System.getProperty("user.home"), DOWNLOAD_FOLDER);
      this.artifactRepository = OfficialArtifactRepository.RABBITMQ;
      this.envVars = new HashMap<>();
      this.processExecutorFactory = new RabbitMqCommand.ProcessExecutorFactory();
    }

    @Beta
    public Builder downloadReadTimeoutInMillis(long downloadReadTimeoutInMillis) {
      this.downloadReadTimeoutInMillis = downloadReadTimeoutInMillis;
      return this;
    }

    @Beta
    public Builder downloadConnectionTimeoutInMillis(long downloadConnectionTimeoutInMillis) {
      this.downloadConnectionTimeoutInMillis = downloadConnectionTimeoutInMillis;
      return this;
    }

    @Beta
    public Builder defaultRabbitMqCtlTimeoutInMillis(long defaultRabbitMqCtlTimeoutInMillis) {
      this.defaultRabbitMqCtlTimeoutInMillis = defaultRabbitMqCtlTimeoutInMillis;
      return this;
    }

    @Beta
    public Builder rabbitMqServerInitializationTimeoutInMillis(long rabbitMqServerInitializationTimeoutInMillis) {
      this.rabbitMqServerInitializationTimeoutInMillis = rabbitMqServerInitializationTimeoutInMillis;
      return this;
    }

    /**
     * Defines where the artifact should be downloaded from and characteristics of the downloaded artifact.
     * <p>
     * Default is {@link OfficialArtifactRepository#RABBITMQ}
     *
     * @see OfficialArtifactRepository
     */
    public Builder downloadFrom(ArtifactRepository repository) {
      this.artifactRepository = repository;
      return this;
    }

    /**
     * @param downloadSource the URL from which to download the OS-specific artifact. For example: {@code
     *                       https://github.com/rabbitmq/rabbitmq-server/releases/download/rabbitmq_v3_6_5/rabbitmq-server-generic-unix-3.6.5.tar.xz}
     * @param appFolderName  this is the name of the folder under which all application files are found. For example,
     *                       {@code rabbitmq-server-generic-unix-3.6.5.tar.xz} extracts everything into to {@code
     *                       rabbitmq_server-3.6.5/*}. Therefore, the value here should be {@code
     *                       "rabbitmq_server-3.6.5"}
     *
     * @see #version(Version)
     */
    public Builder downloadFrom(final URL downloadSource, final String appFolderName) {
      this.artifactRepository = new CustomArtifactRepository(downloadSource);

      this.version = new CustomVersion(appFolderName);
      return this;
    }

    /**
     * Use a predefined version of RabbitMQ.
     * <p>
     * {@link PredefinedVersion} establishes the download URL using the official RabbitMQ artifact repository,
     * the appropriate artifact type depending on the current Operating System, and information about the extraction
     * folder.
     * <p>
     * Default value if none is specified: {@link PredefinedVersion#LATEST}
     *
     * @see #downloadFrom(URL, String)
     */
    public Builder version(Version version) {
      this.version = version;
      return this;
    }

    /**
     * Use this method to define a location where the compressed artifact will be downloaded to.
     * <p>
     * If the folder doesn't already exist, it will be created.
     * <p>
     * By default, the download folder will be: ~/{@value DOWNLOAD_FOLDER}, while the file name will match the
     * remote artifact's name (as defined by the download URL).
     * <p>
     * Use the {@link #downloadTarget(File)} method to specify a file as destination instead of a folder, but not both.
     */
    public Builder downloadFolder(File downloadFolder) {
      if (this.downloadTarget != null) {
        throw new IllegalStateException("Download Target has already been set.");
      }
      this.downloadFolder = downloadFolder;
      return this;
    }

    /**
     * Use this method to specify a file which should be used to store the downloaded artifact.
     * <p>
     * If the file already exists, it will be overwritten.
     * <p>
     * Use {@link #downloadFolder(File)} to define a folder instead of a file if necessary, but not both.
     * <p>
     * By default, the download folder will be: ~/{@value DOWNLOAD_FOLDER}, while the file name will match the
     * remote artifact's name (as defined by the download URL).
     */
    public Builder downloadTarget(File downloadTarget) {
      if (this.downloadFolder != null) {
        throw new IllegalStateException("Download Folder has already been set.");
      }
      this.downloadTarget = downloadTarget;
      return this;
    }

    /**
     * Define a folder where the artifact should be extracted to.
     * <p>
     * By default, the artifact will be extracted to Java's Temp folder.
     *
     * @see SystemUtils#JAVA_IO_TMPDIR
     */
    public Builder extractionFolder(File extractionFolder) {
      this.extractionFolder = extractionFolder;
      return this;
    }

    /**
     * If the artifact is already present in the filesystem, setting this as {code true} will prevent re-downloading it.
     * <p>
     * Default value is {@code true}
     */
    public Builder useCachedDownload(boolean cacheDownload) {
      this.cacheDownload = cacheDownload;
      return this;
    }

    /**
     * If there's an issue downloading or extracting the artifact, automatically delete from downloaded file to prevent
     * future re-use.
     * <p>
     * Default value is {@code true}
     */
    public Builder deleteDownloadedFileOnErrors(boolean deleteCachedFile) {
      this.deleteCachedFile = deleteCachedFile;
      return this;
    }

    /**
     * Defines an environment variable value to use for the execution of all RabbitMQ commands.
     *
     * @see <a href="https://www.rabbitmq.com/configure.html#define-environment-variables">RabbitMQ Environment
     * Variables</a>
     * @see #envVar(RabbitMqEnvVar, String)
     */
    public Builder envVar(String key, String value) {
      this.envVars.put(key, value);
      return this;
    }

    /**
     * Defines an environment variable value to use for the execution of all RabbitMQ commands.
     * <p>
     * Use {@link #envVar(String, String)} to define a variable that's not predefined in the {@link RabbitMqEnvVar} enum
     */
    public Builder envVar(RabbitMqEnvVar key, String value) {
      this.envVar(key.getEnvVarName(), value);
      return this;
    }

    /**
     * A helper method to define several variables at once.
     *
     * @see #envVar(String, String)
     * @see #envVar(RabbitMqEnvVar, String)
     */
    public Builder envVars(Map<String, String> map) {
      this.envVars.putAll(map);
      return this;
    }

    public Builder processExecutorFactory(RabbitMqCommand.ProcessExecutorFactory factory) {
      this.processExecutorFactory = factory;
      return this;
    }

    /**
     * Builds an immutable instance of {@link EmbeddedRabbitMqConfig} using smart defaults.
     */
    public EmbeddedRabbitMqConfig build() {
      if (artifactRepository == null) {
        artifactRepository = OfficialArtifactRepository.RABBITMQ;
      }

      OperatingSystem os = OperatingSystem.detect();

      if (version == null) {
        version = PredefinedVersion.LATEST;
      }

      URL downloadSource = artifactRepository.getUrl(version, os);

      if (downloadTarget == null) {
        String filename = downloadSource.getPath().substring(downloadSource.getPath().lastIndexOf("/"));
        this.downloadTarget = new File(downloadFolder, filename);
      }

      if (extractionFolder == null) {
        this.extractionFolder = new File(SystemUtils.JAVA_IO_TMPDIR);
      }

      File appAbsPath = new File(extractionFolder.toString(), version.getExtractionFolder());

      return new EmbeddedRabbitMqConfig(
          downloadSource, downloadTarget, extractionFolder, appAbsPath,
          downloadConnectionTimeoutInMillis, downloadReadTimeoutInMillis,
          defaultRabbitMqCtlTimeoutInMillis,
          rabbitMqServerInitializationTimeoutInMillis,
          cacheDownload, deleteCachedFile,
          envVars,
          processExecutorFactory);
    }

  }

}
