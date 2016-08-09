package io.arivera.oss.embedded.rabbitmq;

import io.arivera.oss.embedded.rabbitmq.util.StopWatch;
import io.arivera.oss.embedded.rabbitmq.util.SystemUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
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
import java.util.zip.GZIPInputStream;

class Extractor implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(Extractor.class);

  private final EmbeddedRabbitMqConfig config;

  Extractor(EmbeddedRabbitMqConfig config) {
    this.config = config;
  }

  public void run() {
    TarArchiveInputStream archive;
    try {
      BufferedInputStream bufferedFileInput = new BufferedInputStream(new FileInputStream(config.getDownloadTarget()));

      InputStream compressedInputStream;
      if (config.getDownloadTarget().toString().endsWith(".xz")) {
        compressedInputStream = new XZInputStream(bufferedFileInput);
      } else if (config.getDownloadTarget().toString().endsWith(".gz")) {
        compressedInputStream = new GZIPInputStream(bufferedFileInput);
      } else {
        throw new IllegalArgumentException("Cannot determine compression type for: " + config.getDownloadTarget());
      }
      archive = new TarArchiveInputStream(compressedInputStream);
    } catch (IOException e) {
      throw new DownloadException("Download file '" + config.getDownloadTarget() + "' was not found or is not accessible.", e);
    }

    TarArchiveEntry fileToExtract;
    try {
      fileToExtract = archive.getNextTarEntry();
    } catch (IOException e) {
      throw new DownloadException(
          "Could not extract files from file '" + config.getDownloadTarget() + "' due to: " + e.getLocalizedMessage(), e);
    }

    LOGGER.info("Extracting '{}' to '{}'", config.getDownloadTarget(), config.getExtractionFolder());

    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    int fileCounter = 0;
    while (fileToExtract != null) {
      fileCounter++;
      File destPath = new File(config.getExtractionFolder(), fileToExtract.getName());

      if (fileToExtract.isDirectory()) {
        boolean mkdirs = destPath.mkdirs();
        if (!mkdirs) {
          LOGGER.warn("Directory '{}' could not be created. Will attempt to continue...", destPath);
        }
      } else {
        try {
          boolean newFile = destPath.createNewFile();
          if (!newFile) {
            LOGGER.warn("File '{}' already exists. Will attempt to continue...", destPath);
          }
        } catch (IOException e) {
          LOGGER.warn("Could not extract file '" + destPath + "'. Will attempt to continue...", e);
        }

        if (!SystemUtils.IS_OS_WINDOWS) {
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
        }

        BufferedOutputStream output = null;
        try {
          LOGGER.debug("Extracting '" + fileToExtract.getName() + "'...");
          output = new BufferedOutputStream(new FileOutputStream(destPath));
          IOUtils.copy(archive, output);
        } catch (IOException e) {
          throw new DownloadException("Error extracting file '" + fileToExtract.getName() + "' " +
              "from downloaded file: " + config.getDownloadTarget(), e);
        } finally {
          IOUtils.closeQuietly(output);
        }
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

    stopWatch.stop();
    IOUtils.closeQuietly(archive);
    LOGGER.info("Finished extracting {} files in {}ms", fileCounter, stopWatch.getTime());
  }
}
