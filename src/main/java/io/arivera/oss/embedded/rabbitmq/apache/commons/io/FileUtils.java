/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.arivera.oss.embedded.rabbitmq.apache.commons.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * General file manipulation utilities.
 * <p>
 * Facilities are provided in the following areas:
 * <ul>
 * <li>writing to a file
 * <li>reading from a file
 * <li>make a directory including parent directories
 * <li>copying files and directories
 * <li>deleting files and directories
 * <li>converting to and from a URL
 * <li>listing files and directories by filter and extension
 * <li>comparing file content
 * <li>file last changed date
 * <li>calculating a checksum
 * </ul>
 * <p>
 * Note that a specific charset should be specified whenever possible.
 * Relying on the platform default means that the code is Locale-dependent.
 * Only use the default if the files are known to always use the platform default.
 * <p>
 * Origin of code: Excalibur, Alexandria, Commons-Utils
 *
 * @version $Id$
 */
@SuppressWarnings("all")
public class FileUtils {

  /**
   * Copies bytes from the URL <code>source</code> to a file
   * <code>destination</code>. The directories up to <code>destination</code>
   * will be created if they don't already exist. <code>destination</code>
   * will be overwritten if it already exists.
   *
   * @param source            the <code>URL</code> to copy bytes from, must not be {@code null}
   * @param destination       the non-directory <code>File</code> to write bytes to
   *                          (possibly overwriting), must not be {@code null}
   * @param connectionTimeout the number of milliseconds until this method
   *                          will timeout if no connection could be established to the <code>source</code>
   * @param readTimeout       the number of milliseconds until this method will
   *                          timeout if no data could be read from the <code>source</code>
   * @throws IOException if <code>source</code> URL cannot be opened
   * @throws IOException if <code>destination</code> is a directory
   * @throws IOException if <code>destination</code> cannot be written
   * @throws IOException if <code>destination</code> needs creating but can't be
   * @throws IOException if an IO error occurs during copying
   * @since 2.0
   */
  public static void copyURLToFile(URL source, File destination,
                                   int connectionTimeout, int readTimeout) throws IOException {
    URLConnection connection = source.openConnection();
    connection.setConnectTimeout(connectionTimeout);
    connection.setReadTimeout(readTimeout);
    InputStream input = connection.getInputStream();
    copyInputStreamToFile(input, destination);
  }

  /**
   * Copies bytes from an {@link InputStream} <code>source</code> to a file
   * <code>destination</code>. The directories up to <code>destination</code>
   * will be created if they don't already exist. <code>destination</code>
   * will be overwritten if it already exists.
   *
   * @param source      the <code>InputStream</code> to copy bytes from, must not be {@code null}
   * @param destination the non-directory <code>File</code> to write bytes to
   *                    (possibly overwriting), must not be {@code null}
   * @throws IOException if <code>destination</code> is a directory
   * @throws IOException if <code>destination</code> cannot be written
   * @throws IOException if <code>destination</code> needs creating but can't be
   * @throws IOException if an IO error occurs during copying
   * @since 2.0
   */
  public static void copyInputStreamToFile(InputStream source, File destination) throws IOException {
    try {
      FileOutputStream output = openOutputStream(destination);
      try {
        copy(source, output);
        output.close(); // don't swallow close Exception if copy completes normally
      } finally {
        closeQuietly(output);
      }
    } finally {
      closeQuietly(source);
    }
  }


  public static FileOutputStream openOutputStream(File file) throws IOException {
    if (file.exists()) {
      if (file.isDirectory()) {
        throw new IOException("File \'" + file + "\' exists but is a directory");
      }

      if (!file.canWrite()) {
        throw new IOException("File \'" + file + "\' cannot be written to");
      }
    } else {
      File parent = file.getParentFile();
      if (parent != null && !parent.exists() && !parent.mkdirs()) {
        throw new IOException("File \'" + file + "\' could not be created");
      }
    }

    return new FileOutputStream(file);
  }

  public static int copy(InputStream input, OutputStream output) throws IOException {
    long count = copyLarge(input, output);
    return count > 2147483647L ? -1 : (int) count;
  }

  public static long copyLarge(InputStream input, OutputStream output) throws IOException {
    byte[] buffer = new byte[4096];
    long count = 0L;

    int n1;
    for (boolean n = false; -1 != (n1 = input.read(buffer)); count += (long) n1) {
      output.write(buffer, 0, n1);
    }

    return count;
  }


  public static void closeQuietly(OutputStream output) {
    try {
      if (output != null) {
        output.close();
      }
    } catch (IOException var2) {
      ;
    }

  }

  public static void closeQuietly(InputStream input) {
    try {
      if (input != null) {
        input.close();
      }
    } catch (IOException var2) {
      ;
    }

  }
}
