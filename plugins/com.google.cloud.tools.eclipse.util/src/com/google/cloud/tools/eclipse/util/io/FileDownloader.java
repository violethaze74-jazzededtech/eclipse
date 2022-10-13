/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.eclipse.util.io;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Utility class to download files from {@link URL}s.
 */
public class FileDownloader {
  private static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;
  private static final int DEFAULT_READ_TIMEOUT_MS = 3000;
  private IPath downloadFolderPath;

  /**
   * Creates a new instance which will download the files to the directory defined by <code>downloadFolderPath</code>.
   * <p>
   * If the directory does not exist, it will be created on-demand when the first file is downloaded.
   *
   * @param downloadFolderPath path to the directory where the files must be downloaded. Cannot be <code>null</code>,
   * but does not have to exist, it will be created on demand.
   */
  public FileDownloader(IPath downloadFolderPath) {
    Preconditions.checkNotNull(downloadFolderPath, "downloadFolderPath is null");
    File downloadFolder = downloadFolderPath.toFile();
    Preconditions.checkArgument(!downloadFolder.exists() || downloadFolder.isDirectory());
    this.downloadFolderPath = downloadFolderPath;
  }

  /**
   * Downloads the file pointed to by the <code>url</code>
   * <p>
   * The downloaded file's name will be the last segment of the path of the URL.
   *
   * @param url location of the file to download, cannot be <code>null</code>
   * @return a path pointing to the downloaded file
   * @throws IOException if the URL cannot be opened, the output file cannot be written or the transfer of the remote
   * file fails
   */
  public IPath download(URL url) throws IOException {
    Preconditions.checkNotNull(url, "url is null");
    ensureDownloadFolderExists();

    File downloadedFile = downloadFolderPath.append(new Path(url.getPath()).lastSegment()).toFile();
    URLConnection connection = url.openConnection();
    connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MS);
    connection.setReadTimeout(DEFAULT_READ_TIMEOUT_MS);
    connection.setRequestProperty(HttpHeaders.USER_AGENT, "google-cloud-eclipse");
    try (InputStream inputStream = connection.getInputStream();
         FileOutputStream outputStream = new FileOutputStream(downloadedFile)) {
      ByteStreams.copy(inputStream, outputStream);
      return new Path(downloadedFile.getAbsolutePath());
    }
  }

  private void ensureDownloadFolderExists() throws IOException {
    File downloadFolder = downloadFolderPath.toFile();
    if (!downloadFolder.exists() && !downloadFolder.mkdirs()) {
      throw new IOException("Cannot create folder " + downloadFolder.getAbsolutePath());
    }
  }
}
