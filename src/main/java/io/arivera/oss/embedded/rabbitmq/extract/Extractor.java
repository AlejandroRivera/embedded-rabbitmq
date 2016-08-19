package io.arivera.oss.embedded.rabbitmq.extract;

import io.arivera.oss.embedded.rabbitmq.EmbeddedRabbitMqConfig;
import io.arivera.oss.embedded.rabbitmq.util.ArchiveType;
import io.arivera.oss.embedded.rabbitmq.util.StopWatch;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tukaani.xz.XZInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

class Extractor implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(Extractor.class);

  private final EmbeddedRabbitMqConfig config;

  Extractor(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  public void run() throws ExtractException {
    Runnable extractor = getExtractor(config);
    extractor.run();
  }

  CompressedExtractor getExtractor(EmbeddedRabbitMqConfig config) {
    String downloadedFilename = config.getDownloadTarget().toString();
    if (ArchiveType.TAR_GZ.matches(downloadedFilename)) {
      return new TarGzExtractor(config);
    } else if (ArchiveType.TAR_XZ.matches(downloadedFilename)) {
      return new TarXzExtractor(config);
    } else if (ArchiveType.ZIP.matches(downloadedFilename)) {
      return new ZipExtractor(config);
    } else {
      throw new IllegalStateException("Could not determine compression format for file: " + downloadedFilename);
    }
  }

  abstract static class CompressedExtractor implements Runnable {

    protected final EmbeddedRabbitMqConfig config;

    CompressedExtractor(EmbeddedRabbitMqConfig config) {
      this.config = config;
    }

    protected static void createNewFile(File destPath) {
      try {
        boolean newFile = destPath.createNewFile();
        if (!newFile) {
          LOGGER.warn("File '{}' already exists. Will attempt to continue...", destPath);
        }
      } catch (IOException e) {
        LOGGER.warn("Could not extract file '" + destPath + "'. Will attempt to continue...", e);
      }
    }

    protected static void makeDirectory(File destPath) {
      boolean mkdirs = destPath.mkdirs();
      if (!mkdirs) {
        LOGGER.warn("Directory '{}' could not be created. Will attempt to continue...", destPath);
      }
    }

    protected static void extractFile(InputStream archive, File destPath, String fileName) {
      BufferedOutputStream output = null;
      try {
        LOGGER.debug("Extracting '{}'...", fileName);
        output = new BufferedOutputStream(new FileOutputStream(destPath));
        IOUtils.copy(archive, output);
      } catch (IOException e) {
        throw new ExtractException("Error extracting file '" + fileName + "' ", e);
      } finally {
        IOUtils.closeQuietly(output);
      }
    }

  }

  abstract static class AbstractTarExtractor extends CompressedExtractor {

    AbstractTarExtractor(EmbeddedRabbitMqConfig config) {
      super(config);
    }

    @Override
    public void run() throws ExtractException {
      String downloadedFile = config.getDownloadTarget().toString();
      TarArchiveInputStream archive;
      try {
        BufferedInputStream bufferedFileInput = new BufferedInputStream(new FileInputStream(config.getDownloadTarget()));
        InputStream compressedInputStream = getCompressedInputStream(downloadedFile, bufferedFileInput);
        archive = new TarArchiveInputStream(compressedInputStream);
      } catch (IOException e) {
        throw new ExtractException("Download file '" + config.getDownloadTarget() + "' was not found or is not accessible.", e);
      }

      try {
        LOGGER.info("Extracting '{}' to '{}'", config.getDownloadTarget(), config.getExtractionFolder());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        extractTar(archive);
        stopWatch.stop();
        LOGGER.info("Finished extracting files in {}ms", stopWatch.getTime());
      } finally {
        IOUtils.closeQuietly(archive);
      }
    }

    protected abstract InputStream getCompressedInputStream(String downloadedFile,
                                                            BufferedInputStream bufferedFileInput) throws IOException;

    private void extractTar(TarArchiveInputStream archive) {
      TarArchiveEntry fileToExtract;
      try {
        fileToExtract = archive.getNextTarEntry();
      } catch (IOException e) {
        throw new ExtractException("Could not extract files from file '" + config.getDownloadTarget()
            + "' due to: " + e.getLocalizedMessage(), e);
      }

      while (fileToExtract != null) {
        File destPath = new File(config.getExtractionFolder(), fileToExtract.getName());

        if (fileToExtract.isDirectory()) {
          makeDirectory(destPath);
        } else {
          createNewFile(destPath);

          int mode = fileToExtract.getMode();     // example: 764
          int ownerBits = mode >> 2;              // owner bits: 7
          int isExecutable = ownerBits & 1;       // bits: RWX, where X = executable bit
          boolean madeExecutable = destPath.setExecutable(isExecutable == 1);
          if (!madeExecutable) {
            LOGGER.warn("File '{}' (original mode {}) could not be made executable probably due to permission issues.",
                fileToExtract.getName(), mode);
          }

          boolean madeReadable = destPath.setReadable(true);
          if (!madeReadable) {
            LOGGER.warn("File '{}' (original mode {}) could not be made readable probably due to permission issues.",
                fileToExtract.getName(), mode);
          }

          extractFile(archive, destPath, fileToExtract.getName());
        }

        try {
          fileToExtract = archive.getNextTarEntry();
        } catch (IOException e) {
          LOGGER.error("Could not find next file to extract.", e);
          break;
        }
      }

      File extractionFolder = config.getExtractionFolder();
      boolean madeReadable = extractionFolder.setReadable(true);
      if (!madeReadable) {
        LOGGER.warn("File '{}' could not be made readable probably due to permission issues.",
            extractionFolder);
      }

    }

  }

  private static class TarGzExtractor extends AbstractTarExtractor {

    public TarGzExtractor(EmbeddedRabbitMqConfig config) {
      super(config);
    }

    @Override
    protected InputStream getCompressedInputStream(String downloadedFile,
                                                   BufferedInputStream bufferedFileInput)
        throws IOException {
      return new GZIPInputStream(bufferedFileInput);
    }
  }

  private static class TarXzExtractor extends AbstractTarExtractor {

    public TarXzExtractor(EmbeddedRabbitMqConfig config) {
      super(config);
    }

    protected InputStream getCompressedInputStream(String downloadedFile,
                                                   BufferedInputStream bufferedFileInput) throws IOException {
      return new XZInputStream(bufferedFileInput);
    }
  }

  private static class ZipExtractor extends CompressedExtractor {

    public ZipExtractor(EmbeddedRabbitMqConfig config) {
      super(config);
    }

    @Override
    public void run() throws ExtractException {
      ZipFile zipFile;
      try {
        zipFile = new ZipFile(config.getDownloadTarget());
      } catch (IOException e) {
        throw new ExtractException("Download file '" + config.getDownloadTarget() + "' was not found or is not accessible.", e);
      }

      try {
        LOGGER.info("Extracting '{}' to '{}'", config.getDownloadTarget(), config.getExtractionFolder());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        extractZip(zipFile);
        stopWatch.stop();
        LOGGER.info("Finished extracting files in {}ms", stopWatch.getTime());
      } finally {
        IOUtils.closeQuietly(zipFile);
      }
    }

    private void extractZip(ZipFile zipFile) {
      Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
      while (entries.hasMoreElements()) {

        ZipArchiveEntry entry = entries.nextElement();
        String fileName = entry.getName();
        File outputFile = new File(config.getExtractionFolder(), fileName);

        if (entry.isDirectory()) {
          makeDirectory(outputFile);
        } else {
          createNewFile(outputFile);
          try {
            InputStream inputStream = zipFile.getInputStream(entry);
            extractFile(inputStream, outputFile, fileName);
          } catch (IOException e) {
            throw new ExtractException("Error extracting file '" + fileName + "' "
                + "from downloaded file: " + config.getDownloadTarget(), e);
          }
        }
      }
    }
  }

}
