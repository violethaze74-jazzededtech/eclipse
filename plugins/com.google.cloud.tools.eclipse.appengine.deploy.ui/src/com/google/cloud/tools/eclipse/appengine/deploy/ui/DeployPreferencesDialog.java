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

package com.google.cloud.tools.eclipse.appengine.deploy.ui;

import com.google.api.client.auth.oauth2.Credential;
import com.google.cloud.tools.appengine.cloudsdk.AppEngineJavaComponentsNotInstalledException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdk;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkNotFoundException;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkOutOfDateException;
import com.google.cloud.tools.eclipse.appengine.login.IGoogleLoginService;
import com.google.cloud.tools.eclipse.appengine.ui.AppEngineImages;
import com.google.common.base.Preconditions;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.dialog.TitleAreaDialogSupport;
import org.eclipse.jface.databinding.dialog.ValidationMessageProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class DeployPreferencesDialog extends TitleAreaDialog {

  // if the image is smaller (e.g. 32x32, it will break the layout of the TitleAreaDialog)
  // seems like an Eclipse/JFace bug
  private Image titleImage = AppEngineImages.appEngine(64).createImage();

  private StandardDeployPreferencesPanel content;
  private IProject project;
  private IGoogleLoginService loginService;

  public DeployPreferencesDialog(Shell parentShell, IProject project,
                                 IGoogleLoginService loginService) {
    super(parentShell);

    Preconditions.checkNotNull(project, "project is null");
    Preconditions.checkNotNull(loginService, "loginService is null");
    this.project = project;
    this.loginService = loginService;
  }

  @Override
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);

    getShell().setText(Messages.getString("deploy.preferences.dialog.title"));
    setTitle(Messages.getString("deploy.preferences.dialog.title.withProject", project.getName()));

    if (titleImage != null) {
      setTitleImage(titleImage);
    }

    getButton(IDialogConstants.OK_ID).setText(Messages.getString("deploy"));

    // TitleAreaDialogSupport does not validate initially, let's trigger validation this way
    content.getDataBindingContext().updateTargets();

    return contents;
  }

  @Override
  protected Control createDialogArea(final Composite parent) {
    Composite dialogArea = (Composite) super.createDialogArea(parent);

    Composite container = new Composite(dialogArea, SWT.NONE);
    content = new StandardDeployPreferencesPanel(container, project, loginService,
        getLayoutChangedHandler(), true /* requireValues */);
    GridDataFactory.fillDefaults().grab(true, false).applyTo(content);

    // we pull in Dialog's content margins which are zeroed out by TitleAreaDialog
    GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
    GridLayoutFactory.fillDefaults()
        .margins(convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN),
            convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN))
        .spacing(convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING),
            convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING))
        .generateLayout(container);

    TitleAreaDialogSupport.create(this, content.getDataBindingContext())
        .setValidationMessageProvider(new ValidationMessageProvider() {
          @Override
          public int getMessageType(ValidationStatusProvider statusProvider) {
            int type = super.getMessageType(statusProvider);
            setValid(type != IMessageProvider.ERROR);
            return type;
          }
        });
    return dialogArea;
  }

  private Runnable getLayoutChangedHandler() {
    return new Runnable() {
      @Override
      public void run() {
        Shell shell = getShell();
        shell.setMinimumSize(shell.getSize().x, 0);
        shell.pack();
        shell.setMinimumSize(shell.getSize());
      }
    };
  }

  @Override
  protected void okPressed() {
    content.savePreferences();
    IStatus status = validateAppEngineJavaComponents();
    if (status.equals(Status.OK_STATUS)) {
      super.okPressed();
    } else {
      setReturnCode(Dialog.CANCEL);
      close();
      String cloudSdkNotConfigured = Messages.getString("cloudsdk.not.configured");
      ErrorDialog.openError(this.getShell(), cloudSdkNotConfigured, cloudSdkNotConfigured, status);
    }
  }

  private IStatus validateAppEngineJavaComponents()  {
    try {
      CloudSdk cloudSdk = new CloudSdk.Builder().build();
      cloudSdk.validateCloudSdk();
      cloudSdk.validateAppEngineJavaComponents();
      return Status.OK_STATUS;
    } catch (CloudSdkNotFoundException ex) {
      String detailMessage = Messages.getString("cloudsdk.not.configured.detail");
      Status status = new Status(IStatus.ERROR,
          "com.google.cloud.tools.eclipse.appengine.deploy.ui", detailMessage);
      return status;
    } catch (AppEngineJavaComponentsNotInstalledException ex) {
      String detailMessage = Messages.getString("appengine.java.component.missing");
      Status status = new Status(IStatus.ERROR,
          "com.google.cloud.tools.eclipse.appengine.deploy.ui", detailMessage);
      return status;
    } catch (CloudSdkOutOfDateException ex) {
        String detailMessage = Messages.getString("cloudsdk.out.of.date");
        Status status = new Status(IStatus.ERROR,
            "com.google.cloud.tools.eclipse.appengine.deploy.ui", detailMessage);
        return status;
    }
  }

  @Override
  public boolean close() {
    titleImage.dispose();
    return super.close();
  }

  @Override
  public boolean isHelpAvailable() {
    return false;
  }

  private void setValid(boolean isValid) {
    Button okButton = getButton(IDialogConstants.OK_ID);
    if (okButton != null) {
      okButton.setEnabled(isValid);
    }
  }

  public Credential getCredential() {
    return content.getSelectedCredential();
  }
}
