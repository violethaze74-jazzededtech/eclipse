/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.eclipse.projectselector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.eclipse.projectselector.model.GcpProject;
import com.google.cloud.tools.eclipse.test.util.ui.ShellTestResource;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.TableColumn;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProjectSelectorTest {

  @Rule public ShellTestResource shellResource = new ShellTestResource();

  @Test
  public void testCreatedColumns() {
    ProjectSelector projectSelector = new ProjectSelector(shellResource.getShell());

    TableColumn[] columns = projectSelector.getViewer().getTable().getColumns();
    assertThat(columns.length, is(2));
    assertThat(columns[0].getText(), is("Name"));
    assertThat(columns[1].getText(), is("ID"));
  }

  @Test
  public void testProjectsAreSortedAlphabetically() {
    ProjectSelector projectSelector = new ProjectSelector(shellResource.getShell());
    projectSelector.setProjects(getUnsortedProjectList());

    assertThat(getVisibleProjectAtIndex(projectSelector, 0).getName(), is("a"));
    assertThat(getVisibleProjectAtIndex(projectSelector, 1).getName(), is("b"));
    assertThat(getVisibleProjectAtIndex(projectSelector, 2).getName(), is("c"));
    assertThat(getVisibleProjectAtIndex(projectSelector, 3).getName(), is("d"));
  }

  private static GcpProject getVisibleProjectAtIndex(ProjectSelector projectSelector, int index) {
    return (GcpProject) projectSelector.getViewer().getTable().getItem(index).getData();
  }

  @Test
  public void testSetProjectMaintainsSelection() {
    List<GcpProject> projects = getUnsortedProjectList();
    GcpProject selectedProject = projects.get(3);

    ProjectSelector projectSelector = new ProjectSelector(shellResource.getShell());
    projectSelector.setProjects(projects);
    projectSelector.getViewer().setSelection(new StructuredSelection(selectedProject));
    projectSelector.setProjects(projects.subList(2, projects.size()));

    IStructuredSelection selection = projectSelector.getViewer().getStructuredSelection();
    assertThat(selection.size(), is(1));
    assertThat((GcpProject) selection.getFirstElement(), is(selectedProject));
  }

  @Test
  public void testMatches() {
    IValueProperty property = mock(IValueProperty.class);
    when(property.getValue(any())).thenReturn("a");
    assertTrue(ProjectSelector.matches(new String[] { "a" }, new Object(), new IValueProperty[] { property }));
    assertFalse(
        ProjectSelector.matches(new String[] {"b"}, new Object(), new IValueProperty[] {property}));
  }


  private List<GcpProject> getUnsortedProjectList() {
    return Arrays.asList(new GcpProject("b", "b"),
                         new GcpProject("a", "a"),
                         new GcpProject("d", "d"),
                         new GcpProject("c", "c"));
  }
}
