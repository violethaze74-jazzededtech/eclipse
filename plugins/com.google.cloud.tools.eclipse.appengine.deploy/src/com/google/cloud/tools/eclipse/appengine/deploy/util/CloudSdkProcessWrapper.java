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

package com.google.cloud.tools.eclipse.appengine.deploy.util;

import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.cloudsdk.Gcloud;
import com.google.cloud.tools.appengine.cloudsdk.process.LegacyProcessHandler;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandler;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessOutputLineListener;
import com.google.cloud.tools.appengine.cloudsdk.process.StringBuilderProcessOutputLineListener;
import com.google.cloud.tools.eclipse.appengine.deploy.AppEngineProjectDeployer;
import com.google.cloud.tools.eclipse.appengine.deploy.Messages;
import com.google.cloud.tools.eclipse.sdk.GcloudStructuredLogErrorMessageCollector;
import com.google.cloud.tools.eclipse.sdk.MessageConsoleWriterListener;
import com.google.cloud.tools.eclipse.util.CloudToolsInfo;
import com.google.cloud.tools.eclipse.util.status.StatusUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Helper class wrapping a process induced by {@link Gcloud} to hide the bulk of low-level work
 * dealing with process cancellation, process exit monitoring, error output line collection,
 * standard output collection, etc. Intended to be used exclusively by {@link
 * AppEngineProjectDeployer} for its convenience.
 */
public class CloudSdkProcessWrapper {

  private Process process;
  private boolean interrupted;
  private IStatus exitStatus = Status.OK_STATUS;
  private ProcessOutputLineListener stdOutCaptor;
  private boolean initialized;

  /** Collects messages of any gcloud structure log lines whose severity is ERROR. */
  private GcloudStructuredLogErrorMessageCollector gcloudErrorMessageCollector;

  /**
   * Sets up an {@link AppEngineDeployment} to be used for App Engine deploy.
   */
  public AppEngineDeployment getAppEngineDeployment(Path credentialFile,
      MessageConsoleStream normalOutputStream) throws CloudSdkNotFoundException {
    Preconditions.checkNotNull(credentialFile, "credential required for deploying");
    Preconditions.checkArgument(Files.exists(credentialFile), "non-existing credential file");
    Preconditions.checkState(!initialized, "process wrapper already set up");
    initialized = true;

    CloudSdk cloudSdk = new CloudSdk.Builder().build();
    Gcloud gcloud = Gcloud.builder(cloudSdk)
        .setCredentialFile(credentialFile.toFile())
        .setMetricsEnvironment(CloudToolsInfo.METRICS_NAME, CloudToolsInfo.getToolsVersion())
        .setShowStructuredLogs("always")  // turns on gcloud structured log
        .setOutputFormat("json")  // Deploy result will be in JSON.
        .build();

    // Gcloud sends structured deploy result (in JSON format) to stdout, so prepare to capture that.
    stdOutCaptor = new StringBuilderProcessOutputLineListener();
    // Gcloud sends structured gcloud logs (in JSON format) to stderr, so prepare to capture them.
    gcloudErrorMessageCollector = new GcloudStructuredLogErrorMessageCollector();

    ProcessHandler processHandler = LegacyProcessHandler.builder()
        .setStartListener(this::storeProcessObject)
        .setExitListener(this::recordProcessExitCode)
        // Gcloud sends normal operation output to stderr.
        .addStdErrLineListener(new MessageConsoleWriterListener(normalOutputStream))
        .addStdErrLineListener(gcloudErrorMessageCollector)
        .addStdOutLineListener(stdOutCaptor)
        .build();

    return gcloud.newDeployment(processHandler);
  }

  public void interrupt() {
    synchronized (this) {
      interrupted = true;  // not to miss destruction due to race condition
      if (process != null) {
        process.destroy();
      }
    }
  }

  public IStatus getExitStatus() {
    return exitStatus;
  }

  public String getStdOutAsString() {
    Preconditions.checkNotNull(stdOutCaptor);
    return stdOutCaptor.toString();
  }

  private void storeProcessObject(Process process) {
    synchronized (this) {
      this.process = process;
      if (interrupted) {
        process.destroy();
      }
    }
  }

  @VisibleForTesting
  void recordProcessExitCode(int exitCode) {
    if (exitCode != 0) {
      exitStatus = StatusUtil.error(this, getErrorMessage(exitCode), exitCode);
    } else {
      exitStatus = Status.OK_STATUS;
    }
  }

  private String getErrorMessage(int exitCode) {
    if (gcloudErrorMessageCollector != null) {
      List<String> lines = gcloudErrorMessageCollector.getErrorMessages();
      if (!lines.isEmpty()) {
        return Joiner.on('\n').join(lines);
      }
    }
    return Messages.getString("cloudsdk.process.failed", exitCode);
  }
}
