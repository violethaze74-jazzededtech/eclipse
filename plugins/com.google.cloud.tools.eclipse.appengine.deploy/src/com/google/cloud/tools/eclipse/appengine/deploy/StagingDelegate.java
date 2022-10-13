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

package com.google.cloud.tools.eclipse.appengine.deploy;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Delegate that takes care of App Engine environment-specific deploy behaviors for {@link
 * DeployJob}, mostly staging for {@code gcloud app deploy}.
 */
public interface StagingDelegate {

  /**
   * @param stagingDirectory directory where files ready for {@code gcloud app deploy} execution
   *     will be placed
   * @param safeWorkDirectory directory path that may be created safely to use as a temporary work
   *     directory during staging
   * @param stdoutOutputStream where staging may stream stdout
   * @param stderrOutputStream where staging may stream stderr
   */
  IStatus stage(IPath stagingDirectory, IPath safeWorkDirectory,
      MessageConsoleStream stdoutOutputStream, MessageConsoleStream stderrOutputStream,
      IProgressMonitor monitor);

  /**
   * Returns an {@link ISchedulingRule} for this stager to work safely. For example, if this stager
   * needs to lock down an {@link org.eclipse.core.resources.IProject IProject} during staging, it
   * could return the {@link org.eclipse.core.resources.IProject IProject}. Must be reentrant,
   * returning the same object all the time. May return {@code null}.
   */
  ISchedulingRule getSchedulingRule();

  /**
   * Returns a directory where optional YAML configuration files such as {@code cron.yaml}
   * that may be deployed together reside.
   *
   * Conventionally, for App Engine standard, this directory is found inside the staging directory.
   * ({@code appcfg.sh} converts XML configuration files to YAML and puts them under {@code
   * <staging-directory>/WEB-INF/appengine-generated}). For App Engine flexible, this is usually the
   * directory where {@code app.yaml} is located, which may or may not be inside project source.
   *
   * Must be called after successful {@link #stage} (for standard deploy).
   */
  IPath getOptionalConfigurationFilesDirectory();

  /**
   * Does not guarantee cancellation/termination. This can be called concurrently from a different
   * thread than the thread running {@link #stage}.
   */
  void interrupt();

}
