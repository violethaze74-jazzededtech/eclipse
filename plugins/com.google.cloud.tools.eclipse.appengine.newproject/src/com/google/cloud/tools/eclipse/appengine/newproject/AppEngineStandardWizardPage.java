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

package com.google.cloud.tools.eclipse.appengine.newproject;

import com.google.cloud.tools.eclipse.appengine.libraries.model.Library;
import com.google.cloud.tools.eclipse.appengine.ui.AppEngineImages;
import com.google.cloud.tools.eclipse.appengine.ui.AppEngineLibrariesSelectorGroup;
import com.google.cloud.tools.eclipse.usagetracker.AnalyticsEvents;
import com.google.cloud.tools.eclipse.usagetracker.AnalyticsPingManager;
import java.io.File;
import java.util.Collection;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * UI to collect all information necessary to create a new App Engine Standard Java Eclipse project.
 */
public class AppEngineStandardWizardPage extends WizardNewProjectCreationPage {

  private Text javaPackageField;
  private AppEngineLibrariesSelectorGroup appEngineLibrariesSelectorGroup;

  public AppEngineStandardWizardPage() {
    super("basicNewProjectPage"); //$NON-NLS-1$
    setTitle(Messages.getString("app.engine.standard.project")); //$NON-NLS-1$
    setDescription(Messages.getString("create.app.engine.standard.project")); //$NON-NLS-1$

    setImageDescriptor(AppEngineImages.appEngine(64));
  }

  // todo is there a way to call this for a test?
  @Override
  public void createControl(Composite parent) {
    super.createControl(parent);
    AnalyticsPingManager.getInstance().sendPing(
        AnalyticsEvents.APP_ENGINE_NEW_PROJECT_WIZARD,
        AnalyticsEvents.APP_ENGINE_NEW_PROJECT_WIZARD_TYPE,
        AnalyticsEvents.APP_ENGINE_NEW_PROJECT_WIZARD_TYPE_NATIVE, parent.getShell());

    Composite container = (Composite) getControl();
    PlatformUI.getWorkbench().getHelpSystem().setHelp(container,
        "com.google.cloud.tools.eclipse.appengine.newproject.NewProjectContext"); //$NON-NLS-1$

    ModifyListener pageValidator = new PageValidator();

    // Java package name
    Label packageNameLabel = new Label(container, SWT.NONE);
    packageNameLabel.setText(Messages.getString("java.package")); //$NON-NLS-1$
    javaPackageField = new Text(container, SWT.BORDER);
    GridData javaPackagePosition = new GridData(GridData.FILL_HORIZONTAL);
    javaPackagePosition.horizontalSpan = 2;
    javaPackageField.setLayoutData(javaPackagePosition);
    javaPackageField.addModifyListener(pageValidator);

    // Manage APIs
    appEngineLibrariesSelectorGroup = new AppEngineLibrariesSelectorGroup(container);

    Dialog.applyDialogFont(container);
  }

  @Override
  public boolean validatePage() {
    if (!super.validatePage()) {
      return false;
    }

    return validateLocalFields();
  }

  private boolean validateLocalFields() {
    String packageName = javaPackageField.getText();
    IStatus packageStatus = JavaPackageValidator.validate(packageName);
    if (!packageStatus.isOK()) {
      String message = Messages.getString("illegal.package.name",  //$NON-NLS-1$
          packageStatus.getMessage());
      setErrorMessage(message);
      return false;
    }

    File parent = getLocationPath().toFile();
    File projectDirectory = new File(parent, getProjectName());
    if (projectDirectory.exists()) {
      setErrorMessage(Messages.getString("project.location.exists", projectDirectory)); //$NON-NLS-1$
      return false;
    }

    return true;
  }

  private final class PageValidator implements ModifyListener {
    @Override
    public void modifyText(ModifyEvent event) {
      setPageComplete(validatePage());
    }
  }

  public String getPackageName() {
    return javaPackageField.getText();
  }

  public Collection<Library> getSelectedLibraries() {
    return appEngineLibrariesSelectorGroup.getSelectedLibraries();
  }

  @Override
  public void dispose() {
    appEngineLibrariesSelectorGroup.dispose();
    super.dispose();
  }
}
