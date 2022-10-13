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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.api.client.auth.oauth2.Credential;
import com.google.cloud.tools.eclipse.appengine.deploy.DeployPreferences;
import com.google.cloud.tools.eclipse.appengine.deploy.ui.AppEngineDeployPreferencesPanel.ProjectSelectionValidator;
import com.google.cloud.tools.eclipse.login.IGoogleLoginService;
import com.google.cloud.tools.eclipse.login.ui.AccountSelector;
import com.google.cloud.tools.eclipse.login.ui.AccountSelectorObservableValue;
import com.google.cloud.tools.eclipse.projectselector.ProjectRepository;
import com.google.cloud.tools.eclipse.projectselector.ProjectRepositoryException;
import com.google.cloud.tools.eclipse.projectselector.ProjectSelector;
import com.google.cloud.tools.eclipse.projectselector.model.GcpProject;
import com.google.cloud.tools.eclipse.test.util.ui.CompositeUtil;
import com.google.cloud.tools.eclipse.test.util.ui.ShellTestResource;
import com.google.cloud.tools.login.Account;
import java.util.Arrays;
import java.util.HashSet;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.prefs.BackingStoreException;

@RunWith(MockitoJUnitRunner.class)
public class AppEngineDeployPreferencesPanelTest {

  private static final String EMAIL_2 = "some-email-2@example.com";
  private static final String EMAIL_1 = "some-email-1@example.com";

  private Composite parent;
  private DeployPreferences model;
  private AppEngineDeployPreferencesPanel deployPanel;
  private HashSet<Account> oneAccountSet;
  private HashSet<Account> twoAccountSet;
  @Mock private IProject project;
  @Mock private IGoogleLoginService loginService;
  @Mock private Runnable layoutChangedHandler;
  @Mock private Account account1;
  @Mock private Account account2;
  @Mock private Credential credential;
  @Mock private ProjectRepository projectRepository;
  @Rule public ShellTestResource shellTestResource = new ShellTestResource();

  @Before
  public void setUp() {
    parent = new Composite(shellTestResource.getShell(), SWT.NONE);
    when(project.getName()).thenReturn("testProject");
    when(account1.getEmail()).thenReturn(EMAIL_1);
    when(account2.getEmail()).thenReturn(EMAIL_2);
    when(account1.getOAuth2Credential()).thenReturn(credential);
    when(account2.getOAuth2Credential()).thenReturn(mock(Credential.class));
    oneAccountSet = new HashSet<>(Arrays.asList(account1));
    twoAccountSet = new HashSet<>(Arrays.asList(account1, account2));
    model = new DeployPreferences(project);
  }

  @Test
  public void testCreateGcpProjectUrl() {
    assertEquals("https://console.cloud.google.com/projectcreate?previousPage=%2Fprojectselector%2Fappengine%2Fcreate%3Flang%3Djava",
        AppEngineDeployPreferencesPanel.CREATE_GCP_PROJECT_URL);
  }

  @Test
  public void testAutoSelectSingleAccount() {
    when(loginService.getAccounts()).thenReturn(oneAccountSet);
    deployPanel = createPanel(true /* requireValues */);
    assertThat(deployPanel.getSelectedCredential(), is(credential));

    // verify not in error
    IStatus status = getAccountSelectorValidationStatus();
    assertTrue("account selector is in error: " + status.getMessage(), status.isOK());

    assertThat("auto-selected value should be propagated back to model",
        model.getAccountEmail(), is(account1.getEmail()));
  }

  @Test
  public void testAutoSelectSingleAccount_loadGcpProjects()
      throws ProjectRepositoryException, InterruptedException {
    when(loginService.getAccounts()).thenReturn(oneAccountSet);
    initializeProjectRepository();
    deployPanel = createPanel(true /* requireValues */);
    assertNotNull(deployPanel.latestGcpProjectQueryJob);
    deployPanel.latestGcpProjectQueryJob.join();

    assertThat(getProjectSelector().getProjectCount(), is(2));
  }

  @Test
  public void testValidationMessageWhenNotSignedIn() {
    deployPanel = createPanel(true /* requireValues */);
    IStatus status = getAccountSelectorValidationStatus();
    assertThat(status.getMessage(), is("Sign in to Google."));
  }

  @Test
  public void testValidationMessageWhenSignedIn() {
    // Return two accounts because the account selector will auto-select if there exists only one.
    when(loginService.getAccounts()).thenReturn(twoAccountSet);

    deployPanel = createPanel(true /* requireValues */);
    IStatus status = getAccountSelectorValidationStatus();
    assertThat(status.getMessage(), is("Select an account."));
  }

  // https://github.com/GoogleCloudPlatform/google-cloud-eclipse/issues/1229
  @Test
  public void testUncheckStopPreviousVersionButtonWhenDisabled() {
    deployPanel = createPanel(true /* requireValues */);

    Button promoteButton = CompositeUtil.findButton(deployPanel,
        "Promote the deployed version to receive all traffic");
    Button stopButton = CompositeUtil.findButton(deployPanel, "Stop previous version");
    SWTBotCheckBox promote = new SWTBotCheckBox(promoteButton);
    SWTBotCheckBox stop = new SWTBotCheckBox(stopButton);

    // Initially, everything is checked and enabled.
    assertTrue(promoteButton.getSelection());
    assertTrue(stopButton.getSelection());
    assertTrue(stopButton.getEnabled());

    promote.click();
    assertFalse(promoteButton.getSelection());
    assertFalse(stopButton.getSelection());
    assertFalse(stopButton.getEnabled());

    promote.click();
    assertTrue(promoteButton.getSelection());
    assertTrue(stopButton.getSelection());
    assertTrue(stopButton.getEnabled());

    stop.click();
    assertTrue(promoteButton.getSelection());
    assertFalse(stopButton.getSelection());
    assertTrue(stopButton.getEnabled());

    promote.click();
    assertFalse(promoteButton.getSelection());
    assertFalse(stopButton.getSelection());
    assertFalse(stopButton.getEnabled());

    promote.click();
    assertTrue(promoteButton.getSelection());
    assertFalse(stopButton.getSelection());
    assertTrue(stopButton.getEnabled());
  }

  @Test
  public void testProjectSavedInPreferencesSelected()
      throws ProjectRepositoryException, InterruptedException, BackingStoreException {
    IEclipsePreferences node =
        new ProjectScope(project).getNode(DeployPreferences.PREFERENCE_STORE_QUALIFIER);
    try {
      node.put("project.id", "projectId1");
      node.put("account.email", EMAIL_1);
      model = new DeployPreferences(project);

      initializeProjectRepository();
      when(loginService.getAccounts()).thenReturn(twoAccountSet);
      deployPanel = createPanel(true /* requireValues */);
      deployPanel.latestGcpProjectQueryJob.join();

      ProjectSelector projectSelector = getProjectSelector();
      IStructuredSelection selection = projectSelector.getViewer().getStructuredSelection();
      assertThat(selection.size(), is(1));
      assertThat(((GcpProject) selection.getFirstElement()).getId(), is("projectId1"));
    } finally {
      node.clear();
    }
  }

  private ProjectSelector getProjectSelector() {
    return CompositeUtil.findControl(deployPanel, ProjectSelector.class);
  }

  @Test
  public void testProjectNotSelectedIsAnErrorWhenRequireValuesIsTrue() {
    deployPanel = createPanel(true /* requireValues */);
    assertThat(getProjectSelectionValidator().getSeverity(), is(IStatus.ERROR));
  }

  @Test
  public void testProjectNotSelectedIsNotAnErrorWhenRequireValuesIsFalse() {
    deployPanel = createPanel(false /* requireValues */);
    assertThat(getProjectSelectionValidator().getSeverity(), is(IStatus.INFO));
  }

  @Test
  public void testProjectsExistThenNoProjectNotFoundError()
      throws ProjectRepositoryException, InterruptedException {
    when(loginService.getAccounts()).thenReturn(oneAccountSet);
    initializeProjectRepository();
    deployPanel = createPanel(false /* requireValues */);
    selectAccount(account1);
    deployPanel.latestGcpProjectQueryJob.join();
    assertThat(getProjectSelectionValidator().getSeverity(), is(IStatus.OK));
  }

  @Test
  public void testRefreshProjectsForSelectedCredential()
      throws ProjectRepositoryException, InterruptedException {
    when(loginService.getAccounts()).thenReturn(twoAccountSet);
    initializeProjectRepository();

    deployPanel = createPanel(false /* requireValues */);
    assertNull(deployPanel.latestGcpProjectQueryJob);
    assertThat(getProjectSelector().getProjectCount(), is(0));

    selectAccount(account1);
    assertNotNull(deployPanel.latestGcpProjectQueryJob);
    deployPanel.latestGcpProjectQueryJob.join();
    assertThat(getProjectSelector().getProjectCount(), is(2));
    assertThat(getProjectSelector().getProjects().get(0).getId(), is("projectId1"));
    assertThat(getProjectSelector().getProjects().get(1).getId(), is("projectId2"));
  }

  @Test
  public void testRefreshProjectsForSelectedCredential_switchAccounts()
      throws ProjectRepositoryException, InterruptedException {
    when(loginService.getAccounts()).thenReturn(twoAccountSet);
    initializeProjectRepository();

    deployPanel = createPanel(false /* requireValues */);
    assertNull(deployPanel.latestGcpProjectQueryJob);
    assertThat(getProjectSelector().getProjectCount(), is(0));

    selectAccount(account1);
    Job jobForAccount1 = deployPanel.latestGcpProjectQueryJob;
    jobForAccount1.join();
    assertThat(getProjectSelector().getProjectCount(), is(2));

    selectAccount(account2);
    assertNotEquals(jobForAccount1, deployPanel.latestGcpProjectQueryJob);
    deployPanel.latestGcpProjectQueryJob.join();
    assertThat(getProjectSelector().getProjectCount(), is(1));
    assertThat(getProjectSelector().getProjects().get(0).getId(), is("projectId2"));
  }

  // "AppEngineApplicationQueryJob" assumes no project gets selected when switching accounts.
  @Test
  public void testNoProjectSelectedWhenSwitchingAccounts()
      throws ProjectRepositoryException, InterruptedException {
    when(loginService.getAccounts()).thenReturn(twoAccountSet);
    initializeProjectRepository();

    deployPanel = createPanel(false /* requireValues */);
    selectAccount(account1);
    deployPanel.latestGcpProjectQueryJob.join();

    assertThat(getProjectSelector().getProjectCount(), is(2));
    getProjectSelector()
        .setSelection(new StructuredSelection(getProjectSelector().getProjects().get(0)));
    assertThat(getProjectSelector().getSelection().size(), is(1));

    selectAccount(account2);
    deployPanel.latestGcpProjectQueryJob.join();

    assertThat(getProjectSelector().getProjectCount(), is(1));
    assertThat(getProjectSelector().getSelection().size(), is(0));
  }

  private void selectAccount(Account account) {
    AccountSelector selector = CompositeUtil.findControl(deployPanel, AccountSelector.class);
    selector.selectAccount(account.getEmail());
  }

  private AppEngineDeployPreferencesPanel createPanel(boolean requireValues) {
    return new AppEngineDeployPreferencesPanel(parent, project, loginService, layoutChangedHandler,
        requireValues, projectRepository, model) {
          @Override
          protected String getHelpContextId() {
            return null;
          }};
  }

  private IStatus getProjectSelectionValidator() {
    for (Object object : deployPanel.getDataBindingContext().getValidationStatusProviders()) {
      if (object instanceof ProjectSelectionValidator) {
        ProjectSelectionValidator projectSelectionValidator = (ProjectSelectionValidator) object;
        return (IStatus) projectSelectionValidator.getValidationStatus().getValue();
      }
    }
    fail("Could not find ProjectSelectionValidator.");
    return null;
  }

  private void initializeProjectRepository() throws ProjectRepositoryException {
    GcpProject project1 = new GcpProject("Project1", "projectId1");
    GcpProject project2 = new GcpProject("Project2", "projectId2");
    when(projectRepository.getProjects(any(Credential.class)))
      .thenReturn(Arrays.asList(project1, project2))
      .thenReturn(Arrays.asList(project2));
    when(projectRepository.getProject(any(Credential.class), eq("projectId1")))
        .thenReturn(project1);
    when(projectRepository.getProject(any(Credential.class), eq("projectId2")))
        .thenReturn(project2);
  }

  private IStatus getAccountSelectorValidationStatus() {
    IStatus status = null;
    for (Object object : deployPanel.getDataBindingContext().getValidationStatusProviders()) {
      ValidationStatusProvider statusProvider = (ValidationStatusProvider) object;
      if (!statusProvider.getTargets().isEmpty()) {
        if (statusProvider.getTargets().get(0) instanceof AccountSelectorObservableValue) {
          status = (IStatus) statusProvider.getValidationStatus().getValue();
          if (!status.isOK()) {
            return status;
          }
        }
      }
    }
    if (status == null) {
      fail("Could not find AccountSelector databinding to verify validation");
    }
    return status;
  }
}
