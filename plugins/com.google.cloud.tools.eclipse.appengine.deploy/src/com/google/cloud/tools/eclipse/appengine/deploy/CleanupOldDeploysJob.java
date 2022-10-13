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

package com.google.cloud.tools.eclipse.appengine.deploy;

import com.google.cloud.tools.eclipse.util.status.StatusUtil;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class CleanupOldDeploysJob extends Job {

  private static final int RECENT_DIRECTORIES_TO_KEEP = 2;
  private final IPath parentTempDir;

  public CleanupOldDeploysJob(IPath parentTempDir) {
    super(Messages.getString("cleanup.deploy.job.name")); //$NON-NLS-1$
    this.parentTempDir = parentTempDir;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {
      List<File> directories = collectDirectories();
      deleteDirectories(directories);
      return Status.OK_STATUS;
    } catch (IOException ex) {
      return StatusUtil.error(this, Messages.getString("cleanup.deploy.job.error"), ex); //$NON-NLS-1$
    }
  }

  private List<File> collectDirectories() {
    List<File> directories = new ArrayList<>();
    File[] files = parentTempDir.toFile().listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        directories.add(file);
      }
    }
    Collections.sort(directories, new ReverseLastModifiedComparator());
    return directories;
  }

  private void deleteDirectories(List<File> directories) throws IOException {
    for (int i = RECENT_DIRECTORIES_TO_KEEP; i < directories.size(); i++) {
      MoreFiles.deleteRecursively(directories.get(i).toPath(),
          RecursiveDeleteOption.ALLOW_INSECURE);
    }
  }

  /**
   * Comparator that sorts files on reversed order of last modification, i.e. the file that was
   * modified more recently will be "smaller".
   */
  private final class ReverseLastModifiedComparator implements Comparator<File> {
    @Override
    public int compare(File o1, File o2) {
      return - Long.compare(o1.lastModified(), o2.lastModified());
    }
  }

}
