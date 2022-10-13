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
import com.google.cloud.tools.eclipse.login.IGoogleLoginService;
import com.google.cloud.tools.eclipse.login.ui.AccountSelector;
import com.google.cloud.tools.eclipse.login.ui.AccountSelectorObservableValue;
import com.google.cloud.tools.eclipse.ui.util.FontUtil;
import com.google.cloud.tools.eclipse.ui.util.databinding.BucketNameValidator;
import com.google.cloud.tools.eclipse.ui.util.databinding.ProjectIdInputValidator;
import com.google.cloud.tools.eclipse.ui.util.databinding.ProjectVersionValidator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ObservablesManager;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.osgi.service.prefs.BackingStoreException;

public class StandardDeployPreferencesPanel extends DeployPreferencesPanel {

  private static final String APPENGINE_VERSIONS_URL =
      "https://console.cloud.google.com/appengine/versions";

  private static final Logger logger = Logger.getLogger(
      StandardDeployPreferencesPanel.class.getName());

  private AccountSelector accountSelector;

  private Text projectId;

  private Text version;

  private Button autoPromoteButton;

  private Button stopPreviousVersionButton;

  private Text bucket;

  private ExpandableComposite expandableComposite;

  @VisibleForTesting
  DeployPreferencesModel model;
  private ObservablesManager observables;
  private DataBindingContext bindingContext;

  private Runnable layoutChangedHandler;
  private boolean requireValues = true;

  public StandardDeployPreferencesPanel(Composite parent, IProject project,
      IGoogleLoginService loginService, Runnable layoutChangedHandler, boolean requireValues) {
    super(parent, SWT.NONE);

    this.layoutChangedHandler = layoutChangedHandler;
    this.requireValues = requireValues;

    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;

    createCredentialSection(loginService);

    createProjectIdSection();

    createProjectVersionSection();

    createPromoteSection();

    createAdvancedSection();

    Dialog.applyDialogFont(this);

    setLayout(gridLayout);

    loadPreferences(project);

    setupDataBinding();
  }

  private void setupDataBinding() {
    bindingContext = new DataBindingContext();

    setupAccountEmailDataBinding(bindingContext);
    setupProjectIdDataBinding(bindingContext);
    setupProjectVersionDataBinding(bindingContext);
    setupAutoPromoteDataBinding(bindingContext);
    setupBucketDataBinding(bindingContext);

    observables = new ObservablesManager();
    observables.addObservablesFromContext(bindingContext, true, true);
  }

  private void setupAccountEmailDataBinding(DataBindingContext context) {
    AccountSelectorObservableValue accountSelectorObservableValue =
        new AccountSelectorObservableValue(accountSelector);
    UpdateValueStrategy modelToTarget =
        new UpdateValueStrategy().setConverter(new Converter(String.class, String.class) {
          @Override
          public Object convert(Object expectedEmail) {
            // Expected to be an email address, but must also ensure is a currently logged-in
            // account
            if (expectedEmail instanceof String
                && accountSelector.isEmailAvailable((String) expectedEmail)) {
              return expectedEmail;
            } else {
              return null;
            }
          }
        });

    final IObservableValue accountEmailModel = PojoProperties.value("accountEmail").observe(model);

    Binding binding = context.bindValue(accountSelectorObservableValue, accountEmailModel,
        new UpdateValueStrategy(), modelToTarget);
    /*
     * Trigger an explicit target -> model update for the auto-select-single-account case. When the
     * model has a null account but there is exactly 1 login account, then the AccountSelector
     * automatically selects that account. That change means the AccountSelector is at odds with the
     * model.
     */
    binding.updateTargetToModel();
    context.addValidationStatusProvider(new AccountSelectorValidator(requireValues, accountSelector,
        accountSelectorObservableValue));
  }

  private void setupProjectIdDataBinding(DataBindingContext context) {
    ISWTObservableValue projectIdField = WidgetProperties.text(SWT.Modify).observe(projectId);

    IObservableValue projectIdModel = PojoProperties.value("projectId").observe(model);

    context.bindValue(projectIdField, projectIdModel,
        new UpdateValueStrategy().setAfterGetValidator(new ProjectIdInputValidator(requireValues)),
        new UpdateValueStrategy().setAfterGetValidator(new ProjectIdInputValidator(requireValues)));
  }

  private void setupProjectVersionDataBinding(DataBindingContext context) {
    ISWTObservableValue versionField = WidgetProperties.text(SWT.Modify).observe(version);

    IObservableValue versionModel = PojoProperties.value("version").observe(model);

    context.bindValue(versionField, versionModel,
        new UpdateValueStrategy().setAfterGetValidator(new ProjectVersionValidator()),
        new UpdateValueStrategy().setAfterGetValidator(new ProjectVersionValidator()));
  }

  private void setupAutoPromoteDataBinding(DataBindingContext context) {
    ISWTObservableValue promoteButton = WidgetProperties.selection().observe(autoPromoteButton);
    ISWTObservableValue stopPreviousVersion =
        WidgetProperties.selection().observe(stopPreviousVersionButton);
    ISWTObservableValue stopPreviousVersionEnablement =
        WidgetProperties.enabled().observe(stopPreviousVersionButton);

    // use an intermediary value to control the enabled state of stopPreviousVersionButton
    // based on the promote checkbox's state
    WritableValue enablement = new WritableValue();
    context.bindValue(promoteButton, enablement);
    context.bindValue(stopPreviousVersionEnablement, enablement);

    IObservableValue promoteModel = PojoProperties.value("autoPromote").observe(model);
    IObservableValue stopPreviousVersionModel =
        PojoProperties.value("stopPreviousVersion").observe(model);

    context.bindValue(promoteButton, promoteModel);
    context.bindValue(stopPreviousVersion, stopPreviousVersionModel);
  }

  private void setupBucketDataBinding(DataBindingContext context) {
    ISWTObservableValue bucketField = WidgetProperties.text(SWT.Modify).observe(bucket);

    IObservableValue bucketModel = PojoProperties.value("bucket").observe(model);

    context.bindValue(bucketField, bucketModel,
        new UpdateValueStrategy().setAfterGetValidator(new BucketNameValidator()),
        new UpdateValueStrategy().setAfterGetValidator(new BucketNameValidator()));
  }

  @Override
  public boolean savePreferences() {
    try {
      model.savePreferences();
      return true;
    } catch (BackingStoreException exception) {
      logger.log(Level.SEVERE, "Could not save deploy preferences", exception);
      MessageDialog.openError(getShell(),
                              Messages.getString("deploy.preferences.save.error.title"),
                              Messages.getString("deploy.preferences.save.error.message",
                                                 exception.getLocalizedMessage()));
      return false;
    }
  }

  private void loadPreferences(IProject project) {
    model = new DeployPreferencesModel(project);
  }

  public Credential getSelectedCredential() {
    return accountSelector.getSelectedCredential();
  }

  private void createCredentialSection(IGoogleLoginService loginService) {

    Label accountLabel = new Label(this, SWT.LEAD);
    accountLabel.setText(Messages.getString("deploy.preferences.dialog.label.selectAccount"));
    accountLabel.setToolTipText(Messages.getString("tooltip.account"));

    // If we don't require values, then don't auto-select accounts
    accountSelector = new AccountSelector(this, loginService,
        Messages.getString("deploy.preferences.dialog.accountSelector.login"), requireValues);
    accountSelector.setToolTipText(Messages.getString("tooltip.account"));
    GridData accountSelectorGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    accountSelector.setLayoutData(accountSelectorGridData);
  }

  private void createProjectIdSection() {
    Label projectIdLabel = new Label(this, SWT.LEAD);
    projectIdLabel.setText(Messages.getString("project.id"));
    projectIdLabel.setToolTipText(Messages.getString("tooltip.project.id"));

    projectId = new Text(this, SWT.LEAD | SWT.SINGLE | SWT.BORDER);
    projectId.setToolTipText(Messages.getString("tooltip.project.id"));
    GridData projectIdTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    projectId.setLayoutData(projectIdTextGridData);
  }

  private void createProjectVersionSection() {
    Label versionLabel = new Label(this, SWT.LEAD);
    versionLabel.setText(Messages.getString("custom.versioning"));
    versionLabel.setToolTipText(Messages.getString("tooltip.version"));

    version = new Text(this, SWT.LEAD | SWT.SINGLE | SWT.BORDER);
    version.setMessage(Messages.getString("custom.versioning.hint"));
    version.setToolTipText(Messages.getString("tooltip.version"));
    GridData versionGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    version.setLayoutData(versionGridData);
  }

  private void createPromoteSection() {
    autoPromoteButton = new Button(this, SWT.CHECK);
    autoPromoteButton.setText(Messages.getString("auto.promote"));
    String manualPromoteMessage = Messages.getString(
        "tooltip.manual.promote.link", APPENGINE_VERSIONS_URL);
    autoPromoteButton.setToolTipText(manualPromoteMessage);
    GridData autoPromoteButtonGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
    autoPromoteButtonGridData.horizontalSpan = 2;
    autoPromoteButton.setLayoutData(autoPromoteButtonGridData);

    stopPreviousVersionButton = new Button(this, SWT.CHECK);
    stopPreviousVersionButton.setText(Messages.getString("stop.previous.version"));
    stopPreviousVersionButton.setToolTipText(Messages.getString("tooltip.stop.previous.version"));
    GridData stopPreviousVersionButtonGridData =
        new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
    stopPreviousVersionButtonGridData.horizontalSpan = 2;
    stopPreviousVersionButton.setLayoutData(stopPreviousVersionButtonGridData);
  }

  private void createAdvancedSection() {
    createExpandableComposite();
    final Composite bucketComposite = createBucketSection(expandableComposite);

    expandableComposite.setClient(bucketComposite);
    expandableComposite.addExpansionListener(new ExpansionAdapter() {
      @Override
      public void expansionStateChanged(ExpansionEvent e) {
        handleExpansionStateChanged();
      }
    });
  }

  private void createExpandableComposite() {
    expandableComposite = new ExpandableComposite(this, SWT.NONE, ExpandableComposite.TWISTIE);
    FontUtil.convertFontToBold(expandableComposite);
    expandableComposite.setText(Messages.getString("settings.advanced"));
    expandableComposite.setExpanded(false);
    GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    gridData.horizontalSpan = 2;
    expandableComposite.setLayoutData(gridData);

    getFormToolkit().adapt(expandableComposite, true, true);
  }

  private Composite createBucketSection(Composite parent) {
    Composite bucketComposite = new Composite(parent, SWT.NONE);

    Label bucketLabel = new Label(bucketComposite, SWT.LEAD);
    bucketLabel.setText(Messages.getString("custom.bucket"));
    bucketLabel.setToolTipText(Messages.getString("tooltip.staging.bucket"));

    bucket = new Text(bucketComposite, SWT.LEAD | SWT.SINGLE | SWT.BORDER);
    bucket.setMessage(Messages.getString("custom.bucket.hint"));
    GridData bucketData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    bucket.setLayoutData(bucketData);

    bucket.setToolTipText(Messages.getString("tooltip.staging.bucket"));

    GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(bucketComposite);
    return bucketComposite;
  }

  /**
   * Validates the {@link AccountSelector account selector} state against the panel settings.
   * Reports an error if the panel requires all values to be set, but the account selector does not
   * have a valid account.
   */
  private static class AccountSelectorValidator extends FixedMultiValidator {
    final private boolean requireValues;
    final private AccountSelectorObservableValue accountSelectorObservableValue;
    final private AccountSelector accountSelector;

    private AccountSelectorValidator(boolean requireValues, AccountSelector accountSelector,
        AccountSelectorObservableValue accountSelectorObservableValue) {
      this.requireValues = requireValues;
      this.accountSelector = accountSelector;
      this.accountSelectorObservableValue = accountSelectorObservableValue;
      // trigger the validator, as defaults to OK otherwise
      getValidationStatus();
    }

    @Override
    protected IStatus validate() {
      // access accountSelectorObservableValue so MultiValidator records the access
      String selectedEmail = (String) accountSelectorObservableValue.getValue();
      if (requireValues && Strings.isNullOrEmpty(selectedEmail)) {
        if (accountSelector.isSignedIn()) {
          return ValidationStatus.error(Messages.getString("error.account.missing.signedin"));
        } else {
          return ValidationStatus.error(Messages.getString("error.account.missing.signedout"));
        }
      }
      return ValidationStatus.ok();
    }
  }

  // BUGFIX: https://bugs.eclipse.org/bugs/show_bug.cgi?id=312785
  private abstract static class FixedMultiValidator extends MultiValidator {
    @Override
    public IObservableList getTargets() {
      if (isDisposed()) {
        return Observables.emptyObservableList();
      }
      return super.getTargets();
    }
  };

  @Override
  public DataBindingContext getDataBindingContext() {
    return bindingContext;
  }

  @Override
  public void resetToDefaults() {
    model.resetToDefaults();
    bindingContext.updateTargets();
  }

  @Override
  public void dispose() {
    if (bindingContext != null) {
      bindingContext.dispose();
    }
    if (observables != null) {
      observables.dispose();
    }
    super.dispose();
  }

  private void handleExpansionStateChanged() {
    if (layoutChangedHandler != null) {
      layoutChangedHandler.run();
    }
  }

  @Override
  public void setFont(Font font) {
    super.setFont(font);
    expandableComposite.setFont(font);
    FontUtil.convertFontToBold(expandableComposite);
  }
}
