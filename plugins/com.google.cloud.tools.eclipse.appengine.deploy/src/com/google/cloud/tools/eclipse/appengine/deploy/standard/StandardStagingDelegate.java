/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.eclipse.appengine.deploy.standard;

import com.google.cloud.tools.eclipse.appengine.deploy.Messages;
import com.google.cloud.tools.eclipse.appengine.deploy.StagingDelegate;
import com.google.cloud.tools.eclipse.appengine.deploy.WarPublisher;
import com.google.cloud.tools.eclipse.util.status.StatusUtil;
import com.google.common.base.Preconditions;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.console.MessageConsoleStream;

public class StandardStagingDelegate implements StagingDelegate {

  private final IProject project;

  private IPath optionalConfigurationFilesDirectory;

  public StandardStagingDelegate(IProject project) {
    this.project = Preconditions.checkNotNull(project);
  }

  @Override
  public IStatus stage(IPath stagingDirectory, IPath safeWorkDirectory,
      MessageConsoleStream stdoutOutputStream, MessageConsoleStream stderrOutputStream,
      IProgressMonitor monitor) {
    try {
      WarPublisher.publishExploded(
          project, stagingDirectory, safeWorkDirectory, monitor);

      optionalConfigurationFilesDirectory = stagingDirectory.append("WEB-INF");
      return Status.OK_STATUS;
    } catch (CoreException ex) {
      return StatusUtil.error(this, Messages.getString("war.publishing.failed"), ex);
    }
  }

  @Override
  public IPath getDeployablesDirectory() {
    return optionalConfigurationFilesDirectory;
  }

  @Override
  public ISchedulingRule getSchedulingRule() {
    return project;
  }

}
