package com.google.cloud.tools.eclipse.appengine.deploy;

import java.util.Collections;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;

import com.google.cloud.tools.appengine.api.deploy.DefaultDeployConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineDeployment;
import com.google.cloud.tools.eclipse.appengine.deploy.standard.StandardProjectStaging;

/**
 * Deploys a staged App Engine project.
 * The project must be staged first (e.g. in case of App Engine Standard project using {@link StandardProjectStaging})
 * This class will take the staged project and deploy it to App Engine using {@link CloudSdk}
 *
 */
public class AppEngineProjectDeployer {

  public void deploy(IPath stagingDirectory, CloudSdk cloudSdk,
                     DefaultDeployConfiguration configuration,
                     IProgressMonitor monitor) {
    if (monitor.isCanceled()) {
      throw new OperationCanceledException();
    }

    SubMonitor progress = SubMonitor.convert(monitor, 1);
    progress.setTaskName(Messages.getString("task.name.deploy.project")); //$NON-NLS-1$
    try  {
      configuration.setDeployables(Collections.singletonList(stagingDirectory.append("app.yaml").toFile())); //$NON-NLS-1$
      CloudSdkAppEngineDeployment deployment = new CloudSdkAppEngineDeployment(cloudSdk);
      deployment.deploy(configuration);
    } finally {
      progress.worked(1);
    }
  }
}
