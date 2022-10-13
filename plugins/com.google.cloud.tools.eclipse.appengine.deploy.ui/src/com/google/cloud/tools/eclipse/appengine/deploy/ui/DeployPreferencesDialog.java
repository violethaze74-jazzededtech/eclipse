package com.google.cloud.tools.eclipse.appengine.deploy.ui;

import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.databinding.dialog.TitleAreaDialogSupport;
import org.eclipse.jface.databinding.dialog.ValidationMessageProvider;
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

import com.google.cloud.tools.eclipse.appengine.ui.AppEngineImages;
import com.google.common.base.Preconditions;

public class DeployPreferencesDialog extends TitleAreaDialog {

  // if the image is smaller (e.g. 32x32, it will break the layout of the TitleAreaDialog)
  // seems like an Eclipse/JFace bug
  private Image titleImage = AppEngineImages.appEngine(64).createImage();

  private DeployPreferencesPanel content;
  private IProject project;

  public DeployPreferencesDialog(Shell parentShell, IProject project) {
    super(parentShell);

    Preconditions.checkNotNull(project, "project is null");
    this.project = project;
  }

  @Override
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);

    getShell().setText(Messages.getString("deploy.preferences.dialog.title"));
    setTitle(Messages.getString("deploy.preferences.dialog.title.withProject", project.getName()));

    if (titleImage != null) {
      setTitleImage(titleImage);
    }

    // TitleAreaDialogSupport does not validate initially, let's trigger validation this way
    content.getDataBindingContext().updateTargets();

    return contents;
  }
  
  @Override
  protected Control createDialogArea(final Composite parent) {
    Composite dialogArea = (Composite) super.createDialogArea(parent);

    Composite container = new Composite(dialogArea, SWT.NONE);
    content = new DeployPreferencesPanel(container, project, getLayoutChangedHandler());
    GridDataFactory.fillDefaults().grab(true, false).applyTo(content);

    // we pull in Dialog's content margins which are zeroed out by TitleAreaDialog
    GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
    GridLayoutFactory.fillDefaults()
        .margins(convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN),
            convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN))
        .spacing(convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING),
            convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING))
        .generateLayout(container);

    TitleAreaDialogSupport.create(this, content.getDataBindingContext()).setValidationMessageProvider(new ValidationMessageProvider() {
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
    super.okPressed();
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
}
