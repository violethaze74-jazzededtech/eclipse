package com.google.cloud.tools.eclipse.appengine.newproject;

import com.google.cloud.tools.eclipse.sdk.ui.preferences.CloudSdkPrompter;
import com.google.cloud.tools.eclipse.usagetracker.AnalyticsEvents;
import com.google.cloud.tools.eclipse.usagetracker.AnalyticsPingManager;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.statushandlers.StatusManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class StandardProjectWizard extends Wizard implements INewWizard {

  private AppEngineStandardWizardPage page;
  private AppEngineStandardProjectConfig config = new AppEngineStandardProjectConfig();

  public StandardProjectWizard() {
    this.setWindowTitle("New App Engine Standard Project");
    page = new AppEngineStandardWizardPage();
    setNeedsProgressMonitor(true);
  }

  @Override
  public void addPages() {
    this.addPage(page);
  }

  @Override
  public boolean performFinish() {
    AnalyticsPingManager.getInstance().sendPing(
        AnalyticsEvents.APP_ENGINE_NEW_PROJECT_WIZARD_COMPLETE,
        AnalyticsEvents.APP_ENGINE_NEW_PROJECT_WIZARD_TYPE,
        AnalyticsEvents.APP_ENGINE_NEW_PROJECT_WIZARD_TYPE_NATIVE);

    if (config.getCloudSdkLocation() == null) {
      File location = CloudSdkPrompter.getCloudSdkLocation(getShell());
      if (location == null) {
        return false;
      }
      config.setCloudSdkLocation(location);
    }
    // todo is this the right time/place to grab these?
    config.setAppEngineProjectId(page.getAppEngineProjectId());
    config.setPackageName(page.getPackageName());

    config.setProject(page.getProjectHandle());
    if (!page.useDefaults()) {
      config.setEclipseProjectLocationUri(page.getLocationURI());
    }

    config.setAppEngineLibraries(page.getSelectedLibraries());

    // todo set up
    final IAdaptable uiInfoAdapter = WorkspaceUndoUtil.getUIInfoAdapter(getShell());
    IRunnableWithProgress runnable = new CreateAppEngineStandardWtpProject(config, uiInfoAdapter);

    IStatus status = Status.OK_STATUS;
    try {
      boolean fork = true;
      boolean cancelable = true;
      getContainer().run(fork, cancelable, runnable);
    } catch (InterruptedException ex) {
      status = Status.CANCEL_STATUS;
    } catch (InvocationTargetException ex) {
      status = setErrorStatus(ex.getCause());
    }

    return status.isOK();
  }

  // visible for testing
  static IStatus setErrorStatus(Throwable ex) {
    int errorCode = 1;
    String message = "Failed to create project";
    if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
      message += ": " + ex.getMessage();
    }
    IStatus status = new Status(Status.ERROR, "todo plugin ID", errorCode, message, ex);
    StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
    return status;
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
  }

}
