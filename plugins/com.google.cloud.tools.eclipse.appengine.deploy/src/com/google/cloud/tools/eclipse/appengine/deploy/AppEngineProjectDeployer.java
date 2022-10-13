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

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.DefaultDeployConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.eclipse.appengine.deploy.util.CloudSdkProcessWrapper;
import com.google.cloud.tools.eclipse.util.status.StatusUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Deploys a staged App Engine project. The project must be staged first (e.g. using {@link
 * CloudSdkStagingHelper}). This class will take the staged project and deploy it to App Engine
 * using {@link CloudSdk}.
 */
public class AppEngineProjectDeployer {

  @VisibleForTesting
  static class Deployable {
    String yaml;
    String xml;
    boolean optional;

    Deployable(String yaml, String xml, boolean optional) {
      Preconditions.checkArgument(yaml.endsWith(".yaml")); //$NON-NLS-1$
      Preconditions.checkArgument(xml.endsWith(".xml")); //$NON-NLS-1$
      this.yaml = yaml;
      this.xml = xml;
      this.optional = optional;
    }
  }

  @VisibleForTesting
  static final List<Deployable> APP_ENGINE_DEPLOYABLES = Collections.unmodifiableList(
      Arrays.asList(
          new Deployable("app.yaml", "appengine-web.xml", //$NON-NLS-1$ //$NON-NLS-2$
              false /* optional */),
          new Deployable("cron.yaml", "cron.xml", true), //$NON-NLS-1$ //$NON-NLS-2$
          new Deployable("dispatch.yaml", "dispatch.xml", true), //$NON-NLS-1$ //$NON-NLS-2$
          new Deployable("dos.yaml", "dos.xml", true), //$NON-NLS-1$ //$NON-NLS-2$
          new Deployable("index.yaml", "datastore-indexes.xml", true), //$NON-NLS-1$ //$NON-NLS-2$
          new Deployable("queue.yaml", "queue.xml", true))); //$NON-NLS-1$ //$NON-NLS-2$

  private final CloudSdkProcessWrapper cloudSdkProcessWrapper = new CloudSdkProcessWrapper();

  /**
   * @param deployablesDirectory where deployable target files ({@code app.yaml, appengine-web.xml,
   *     cron.yaml, cron.xml, ...}) reside
   */
  public IStatus deploy(Path credentialFile, DeployPreferences deployPreferences,
      IPath deployablesDirectory, MessageConsoleStream stdoutOutputStream,
      IProgressMonitor monitor) {
    Preconditions.checkNotNull(deployablesDirectory);

    if (monitor.isCanceled()) {
      throw new OperationCanceledException();
    }

    SubMonitor progress = SubMonitor.convert(monitor, 1);
    progress.setTaskName(Messages.getString("task.name.deploy.project")); //$NON-NLS-1$
    try {
      boolean addOptionals = deployPreferences.isIncludeOptionalConfigurationFiles();
      List<File> deployables = computeDeployables(deployablesDirectory, addOptionals);

      DefaultDeployConfiguration configuration =
          DeployPreferencesConverter.toDeployConfiguration(deployPreferences);
      configuration.setDeployables(deployables);

      AppEngineDeployment deployment =
          cloudSdkProcessWrapper.getAppEngineDeployment(credentialFile, stdoutOutputStream);
      deployment.deploy(configuration);
      return cloudSdkProcessWrapper.getExitStatus();
    } catch (AppEngineException ex) {
      return StatusUtil.error(
          this, "Error deploying project: " + ex.getMessage(), ex); //$NON-NLS-1$
    } finally {
      progress.worked(1);
    }
  }

  @VisibleForTesting
  static List<File> computeDeployables(IPath deployablesDirectory, boolean addOptionals) {
    List<File> deployables = new ArrayList<>();

    for (Deployable deployable : APP_ENGINE_DEPLOYABLES) {
      IPath yaml = deployablesDirectory.append(deployable.yaml);
      IPath xml = deployablesDirectory.append(deployable.xml);

      if (!deployable.optional || addOptionals) {
        if (!addFileToListIfExists(yaml, deployables)) {
          addFileToListIfExists(xml, deployables);
        }
      }
    }
    return deployables;
  }

  private static boolean addFileToListIfExists(IPath filePath, List<File> list) {
    File file = filePath.toFile();
    if (file.exists()) {
      list.add(file);
      return true;
    }
    return false;
  }

  public void interrupt() {
    cloudSdkProcessWrapper.interrupt();
  }

  public String getJsonDeployResult() {
    Preconditions.checkNotNull(cloudSdkProcessWrapper);
    return cloudSdkProcessWrapper.getStdOutAsString();
  }
}
