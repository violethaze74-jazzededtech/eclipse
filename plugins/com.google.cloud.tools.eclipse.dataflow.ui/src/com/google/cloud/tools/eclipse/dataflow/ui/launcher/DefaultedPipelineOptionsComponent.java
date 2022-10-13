/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.eclipse.dataflow.ui.launcher;

import com.google.cloud.tools.eclipse.dataflow.core.preferences.DataflowPreferences;
import com.google.cloud.tools.eclipse.dataflow.ui.Messages;
import com.google.cloud.tools.eclipse.dataflow.ui.page.MessageTarget;
import com.google.cloud.tools.eclipse.dataflow.ui.preferences.RunOptionsDefaultsComponent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * A Component that contains a group of pipeline options that can be defaulted with
 * {@link DataflowPreferences}. Contains a Button to re-load from defaults and a
 * {@link RunOptionsDefaultsComponent} to input said defaults.
 */
public class DefaultedPipelineOptionsComponent {
  private Group defaultsGroup;

  @VisibleForTesting
  Button loadDefaultsButton;

  private DataflowPreferences preferences;
  private Map<String, String> customValues;

  private RunOptionsDefaultsComponent defaultOptions;

  public DefaultedPipelineOptionsComponent(Composite parent, Object layoutData,
      MessageTarget messageTarget, DataflowPreferences preferences) {
    this(parent, layoutData, messageTarget, preferences, null);
  }

  @VisibleForTesting
  DefaultedPipelineOptionsComponent(Composite parent, Object layoutData,
      MessageTarget messageTarget, DataflowPreferences preferences,
      RunOptionsDefaultsComponent defaultOptions) {
    this.preferences = preferences;
    customValues = new HashMap<>();

    defaultsGroup = new Group(parent, SWT.NULL);
    int numColumns = 3;
    defaultsGroup.setLayout(new GridLayout(numColumns, false));
    defaultsGroup.setLayoutData(layoutData);

    this.defaultOptions = defaultOptions == null
        ? new RunOptionsDefaultsComponent(defaultsGroup, numColumns, messageTarget, preferences)
        : defaultOptions;

    loadDefaultsButton = new Button(defaultsGroup, SWT.PUSH);
    loadDefaultsButton.setText(Messages.getString("restore.defaults")); //$NON-NLS-1$

    loadDefaultsButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        loadPreferences();
      }
    });

    loadDefaultsButton
        .setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false, numColumns, 1));
  }

  public void setCustomValues(Map<String, String> newValues) {
    customValues.put(DataflowPreferences.ACCOUNT_EMAIL_PROPERTY,
        newValues.get(DataflowPreferences.ACCOUNT_EMAIL_PROPERTY));
    customValues.put(DataflowPreferences.PROJECT_PROPERTY,
        newValues.get(DataflowPreferences.PROJECT_PROPERTY));
    customValues.put(DataflowPreferences.STAGING_LOCATION_PROPERTY,
        newValues.get(DataflowPreferences.STAGING_LOCATION_PROPERTY));
    // TODO: Select appropriate defaults based on major version
    customValues.put(DataflowPreferences.GCP_TEMP_LOCATION_PROPERTY,
        newValues.get(DataflowPreferences.STAGING_LOCATION_PROPERTY));
    loadCustomValues();
  }

  public void setPreferences(DataflowPreferences preferences) {
    this.preferences = preferences;
    loadPreferences();
  }

  public Map<String, String> getValues() {
    Map<String, String> values = new HashMap<>();
    values.put(DataflowPreferences.ACCOUNT_EMAIL_PROPERTY, defaultOptions.getAccountEmail());
    values.put(DataflowPreferences.PROJECT_PROPERTY, defaultOptions.getProjectId());
    values.put(DataflowPreferences.STAGING_LOCATION_PROPERTY, defaultOptions.getStagingLocation());
    // TODO: Give this a separate input
    values.put(DataflowPreferences.GCP_TEMP_LOCATION_PROPERTY, defaultOptions.getStagingLocation());
    return values;
  }

  @VisibleForTesting
  void loadPreferences() {
    String defaultAccountEmail = preferences.getDefaultAccountEmail();
    defaultOptions.selectAccount(Strings.nullToEmpty(defaultAccountEmail));
    String defaultProject = preferences.getDefaultProject();
    defaultOptions.setCloudProjectText(Strings.nullToEmpty(defaultProject));
    String defaultStagingLocation = preferences.getDefaultStagingLocation();
    defaultOptions.setStagingLocationText(Strings.nullToEmpty(defaultStagingLocation));
  }

  private void loadCustomValues() {
    String accountEmail = customValues.get(DataflowPreferences.ACCOUNT_EMAIL_PROPERTY);
    defaultOptions.selectAccount(Strings.nullToEmpty(accountEmail));
    String project = customValues.get(DataflowPreferences.PROJECT_PROPERTY);
    defaultOptions.setCloudProjectText(Strings.nullToEmpty(project));
    String stagingLocation = customValues.get(DataflowPreferences.STAGING_LOCATION_PROPERTY);
    defaultOptions.setStagingLocationText(Strings.nullToEmpty(stagingLocation));
  }

  public void addAccountSelectionListener(Runnable listener) {
    defaultOptions.addAccountSelectionListener(listener);
  }

  public void addButtonSelectionListener(SelectionListener listener) {
    loadDefaultsButton.addSelectionListener(listener);
  }

  public void addModifyListener(ModifyListener listener) {
    defaultOptions.addModifyListener(listener);
  }
}
