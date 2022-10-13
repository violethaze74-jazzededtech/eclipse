/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.eclipse.projectselector;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.cloudresourcemanager.CloudResourceManager.Projects;
import com.google.api.services.cloudresourcemanager.model.ListProjectsResponse;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.cloud.tools.eclipse.googleapis.IGoogleApiFactory;
import com.google.cloud.tools.eclipse.projectselector.model.GcpProject;
import com.google.cloud.tools.eclipse.test.util.ui.ShellTestResource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MiniSelectorTest {
  @Mock
  public IGoogleApiFactory apiFactory;
  @Rule
  public ShellTestResource shellResource = new ShellTestResource();
  
  private SWTBot bot;

  @Before
  public void setUp() {
    bot = new SWTBot(shellResource.getShell());
  }

  @Test
  public void testNoCredential() {
    MiniSelector selector = new MiniSelector(shellResource.getShell(), apiFactory);
    assertNull(selector.getCredential());
    assertNotNull(selector.getSelection());
    assertTrue(selector.getSelection().isEmpty());
    assertNull(selector.getProject());
    assertEquals("", selector.getProjectId());
  }

  @Test
  public void testWithCredential() {
    Credential credential = mock(Credential.class);
    mockProjectsList(credential, new GcpProject("foo", "foo.id"));
    MiniSelector selector = new MiniSelector(shellResource.getShell(), apiFactory, credential);
    assertEquals(credential, selector.getCredential());
    assertNotNull(selector.getSelection());
    assertTrue(selector.getSelection().isEmpty());
    assertNull(selector.getProject());
    assertEquals("", selector.getProjectId());

    selector.setProject("foo.id");
    waitUntilResolvedProject(selector); // waits for the project list to be returned

    assertFalse(selector.getSelection().isEmpty());
    assertNotNull(selector.getProject());
    assertEquals("foo", selector.getProject().getName());
    assertEquals("foo.id", selector.getProject().getId());
    assertEquals("foo.id", selector.getProjectId());
  }

  @Test
  public void testListenerFired() {
    Credential credential = mock(Credential.class);
    mockProjectsList(credential, new GcpProject("foo", "foo.id"));
    final MiniSelector selector =
        new MiniSelector(shellResource.getShell(), apiFactory, credential);

    assertNotNull(selector.getSelection());
    assertTrue(selector.getSelection().isEmpty());
    final ISelection[] lastSelection = new ISelection[1];
    selector.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        lastSelection[0] = event.getSelection();
      }
    });

    selector.setProject("foo.id");
    waitUntilResolvedProject(selector); // waits for the project list to be returned

    assertThat(lastSelection[0], instanceOf(IStructuredSelection.class));
    IStructuredSelection selection = (IStructuredSelection) lastSelection[0];
    assertEquals(1, selection.size());
    assertThat(selection.getFirstElement(), instanceOf(GcpProject.class));
    GcpProject gcpProject = (GcpProject) selection.getFirstElement();
    assertEquals("foo", gcpProject.getName());
    assertEquals("foo.id", gcpProject.getId());
  }

  private void waitUntilResolvedProject(final MiniSelector selector) {
    bot.waitUntil(new DefaultCondition() {
      @Override
      public boolean test() throws Exception {
        if (Display.getCurrent() != null) {
          // seems surprising that waitUtil() doesn't spin the event loop
          while (Display.getCurrent().readAndDispatch());
        }
        return selector.getProject() != null;
      }

      @Override
      public String getFailureMessage() {
        return "MiniSelector project was never resolved";
      }
    });
  }


  private void mockProjectsList(Credential credential,
      GcpProject... gcpProjects) {
    Projects projectsApi = mock(Projects.class);
    Projects.List listApi = mock(Projects.List.class);
    List<Project> projectsList = new ArrayList<>();
    for (GcpProject gcpProject : gcpProjects) {
      Project project = new Project();
      project.setName(gcpProject.getName());
      project.setProjectId(gcpProject.getId());
      projectsList.add(project);
    }
    ListProjectsResponse response = new ListProjectsResponse();
    response.setProjects(projectsList);
    try {
      doReturn(projectsApi).when(apiFactory).newProjectsApi(credential);
      doReturn(listApi).when(listApi).setPageSize(any(Integer.class));
      doReturn(listApi).when(projectsApi).list();
      doReturn(response).when(listApi).execute();
    } catch (IOException ex) {
      fail(ex.toString());
    }
  }
}
