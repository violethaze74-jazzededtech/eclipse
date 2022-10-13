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

package com.google.cloud.tools.eclipse.appengine.facets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.eclipse.test.util.project.TestProjectCreator;
import com.google.cloud.tools.eclipse.util.io.ResourceUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@code FacetUtil}
 */
@RunWith(MockitoJUnitRunner.class)
public class FacetUtilTest {
  @Mock private IFacetedProject mockFacetedProject;
  @Rule public TestProjectCreator projectCreator = new TestProjectCreator().withFacets();
  @Rule public TestProjectCreator javaProjectCreator = new TestProjectCreator().withFacets(
      JavaFacet.VERSION_1_7);

  @Test
  public void testConstructor_nullFacetedProject() {
    try {
      new FacetUtil(null);
      fail();
    } catch (NullPointerException ex) {
      assertEquals("facetedProject is null", ex.getMessage());
    }
  }

  @Test
  public void testConstructor_nonNullFacetedProject() {
    new FacetUtil(mockFacetedProject);
  }

  @Test
  public void testAddJavaFacetToBatch_nullFacet() {
    FacetUtil facetUtil = new FacetUtil(mockFacetedProject);
    try {
      facetUtil.addJavaFacetToBatch(null);
      fail();
    } catch (NullPointerException ex) {
      assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testAddJavaFacetToBatch_facetDoesNotExitsInProject() {
    when(mockFacetedProject.hasProjectFacet(JavaFacet.VERSION_1_7)).thenReturn(false);
    when(mockFacetedProject.getProject()).thenReturn(projectCreator.getProject());

    FacetUtil facetUtil = new FacetUtil(mockFacetedProject).addJavaFacetToBatch(JavaFacet.VERSION_1_7);
    Assert.assertEquals(1, facetUtil.facetInstallSet.size());
    Assert.assertEquals(JavaFacet.VERSION_1_7,
        facetUtil.facetInstallSet.iterator().next().getProjectFacetVersion());
  }

  @Test
  public void testAddJavaFacetToBatch_facetExitsInProject() {
    when(mockFacetedProject.hasProjectFacet(JavaFacet.FACET)).thenReturn(true);
    when(mockFacetedProject.hasProjectFacet(JavaFacet.VERSION_1_7)).thenReturn(true);
    when(mockFacetedProject.getProjectFacetVersion(JavaFacet.FACET))
        .thenReturn(JavaFacet.VERSION_1_7);

    FacetUtil facetUtil = new FacetUtil(mockFacetedProject).addJavaFacetToBatch(JavaFacet.VERSION_1_7);
    Assert.assertEquals(0, facetUtil.facetInstallSet.size());
  }

  @Test
  public void testAddJavaFacetToBatch_nonJavaFacet() {
    FacetUtil facetUtil = new FacetUtil(mockFacetedProject);
    try {
      facetUtil.addJavaFacetToBatch(WebFacetUtils.WEB_25);
      fail();
    } catch (IllegalArgumentException ex) {
      assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testAddWebFacetToBatch_nullFacet() {
    FacetUtil facetUtil = new FacetUtil(mockFacetedProject);
    try {
      facetUtil.addWebFacetToBatch(null);
      fail();
    } catch (NullPointerException ex) {
      assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testAddWebFacetToBatch_facetDoesNotExitsInProject() {
    when(mockFacetedProject.hasProjectFacet(WebFacetUtils.WEB_25)).thenReturn(false);
    when(mockFacetedProject.getProject()).thenReturn(projectCreator.getProject());

    FacetUtil facetUtil = new FacetUtil(mockFacetedProject).addWebFacetToBatch(WebFacetUtils.WEB_25);
    Assert.assertEquals(1, facetUtil.facetInstallSet.size());
    Assert.assertEquals(WebFacetUtils.WEB_25,
        facetUtil.facetInstallSet.iterator().next().getProjectFacetVersion());
  }

  @Test
  public void testAddWebFacetToBatch_facetExitsInProject() {
    when(mockFacetedProject.hasProjectFacet(WebFacetUtils.WEB_FACET)).thenReturn(true);
    when(mockFacetedProject.hasProjectFacet(WebFacetUtils.WEB_25)).thenReturn(true);
    when(mockFacetedProject.getProjectFacetVersion(WebFacetUtils.WEB_FACET))
        .thenReturn(WebFacetUtils.WEB_25);

    FacetUtil facetUtil = new FacetUtil(mockFacetedProject).addWebFacetToBatch(WebFacetUtils.WEB_25);
    Assert.assertEquals(0, facetUtil.facetInstallSet.size());
  }

  @Test
  public void testAddWebFacetToBatch_nonWebFacet() {
    FacetUtil facetUtil = new FacetUtil(mockFacetedProject);
    try {
      facetUtil.addWebFacetToBatch(JavaFacet.VERSION_1_7);
      fail();
    } catch (IllegalArgumentException ex) {
      assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testAddFacetToBatch_nullFacet() {
    FacetUtil facetUtil = new FacetUtil(mockFacetedProject);
    try {
      facetUtil.addFacetToBatch(null, null);
      fail();
    } catch (NullPointerException ex) {
      assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testAddFacetToBatch_facetDoesNotExistInProject() {
    FacetUtil facetUtil = new FacetUtil(mockFacetedProject)
        .addFacetToBatch(AppEngineStandardFacet.JRE7, null);

    Assert.assertEquals(1, facetUtil.facetInstallSet.size());
    Assert.assertEquals(AppEngineStandardFacet.JRE7,
        facetUtil.facetInstallSet.iterator().next().getProjectFacetVersion());
  }

  @Test
  public void testAddFacetToBatch_facetExistsInProject() {
    when(mockFacetedProject.hasProjectFacet(AppEngineStandardFacet.JRE7)).thenReturn(true);
    FacetUtil facetUtil = new FacetUtil(mockFacetedProject)
        .addFacetToBatch(AppEngineStandardFacet.JRE7, null);

    Assert.assertEquals(0, facetUtil.facetInstallSet.size());
  }

  @Test
  public void testInstall() throws CoreException {
    IFacetedProject facetedProject = projectCreator.getFacetedProject();
    new FacetUtil(facetedProject).addJavaFacetToBatch(JavaFacet.VERSION_1_7).install(null);

    Set<IProjectFacetVersion> facets = facetedProject.getProjectFacets();
    Assert.assertNotNull(facets);
    Assert.assertEquals(1, facets.size());
    Assert.assertEquals(JavaFacet.VERSION_1_7, facets.iterator().next());
  }

  @Test
  public void testInstallWebFacet_hasWtpClasspathContainers() throws CoreException {
    IFacetedProject facetedProject = javaProjectCreator.getFacetedProject();
    assertFalse(hasWtpClasspathContainers(facetedProject.getProject()));

    new FacetUtil(facetedProject).addWebFacetToBatch(WebFacetUtils.WEB_25).install(null);
    assertTrue(hasWtpClasspathContainers(facetedProject.getProject()));
  }

  private static boolean hasWtpClasspathContainers(IProject project) throws JavaModelException {
    boolean seenWebContainer = false;
    IJavaProject javaProject = JavaCore.create(project);
    for (IClasspathEntry entry : javaProject.getRawClasspath()) {
      if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
        if (entry.getPath().equals(new Path("org.eclipse.jst.j2ee.internal.web.container"))) {
          seenWebContainer = true;
        }
      }
    }
    return seenWebContainer;
  }

  @Test
  public void testFindAllWebInfFolders_noWebInfFolders() {
    List<IFolder> webInfFolders = FacetUtil.findAllWebInfFolders(projectCreator.getProject());
    Assert.assertTrue(webInfFolders.isEmpty());
  }

  @Test
  public void testFindAllWebInfFolders() throws CoreException {
    IProject project = projectCreator.getProject();
    ResourceUtils.createFolders(project.getFolder("src/my-webapp/WEB-INF"), null);

    List<IFolder> webInfFolders =
        FacetUtil.findAllWebInfFolders(project);
    Assert.assertEquals(1, webInfFolders.size());
    Assert.assertEquals(project.getFolder("src/my-webapp/WEB-INF"), webInfFolders.get(0));
  }

  @Test
  public void testFindAllWebInfFolders_multipleFolders() throws CoreException {
    IProject project = projectCreator.getProject();
    ResourceUtils.createFolders(project.getFolder("webapps/first-webapp/WEB-INF"), null);
    ResourceUtils.createFolders(project.getFolder("webapps/second-webapp/WEB-INF"), null);
    ResourceUtils.createFolders(project.getFolder("third-webapp/WEB-INF"), null);
    ResourceUtils.createFolders(project.getFolder("WEB-INF"), null);

    List<IFolder> webInfFolders = FacetUtil.findAllWebInfFolders(project);
    Assert.assertEquals(4, webInfFolders.size());
    Assert.assertTrue(webInfFolders.contains(project.getFolder("webapps/first-webapp/WEB-INF")));
    Assert.assertTrue(webInfFolders.contains(project.getFolder("webapps/second-webapp/WEB-INF")));
    Assert.assertTrue(webInfFolders.contains(project.getFolder("third-webapp/WEB-INF")));
    Assert.assertTrue(webInfFolders.contains(project.getFolder("WEB-INF")));
  }

  @Test
  public void testFindMainWebAppDirectory_noWebInfFolders() {
    IPath mainWebApp = FacetUtil.findMainWebAppDirectory(projectCreator.getProject());
    Assert.assertNull(mainWebApp);
  }

  @Test
  public void testFindMainWebAppDirectory() throws CoreException {
    IProject project = projectCreator.getProject();
    ResourceUtils.createFolders(project.getFolder("webapps/first-webapp/WEB-INF"), null);
    IPath mainWebApp = FacetUtil.findMainWebAppDirectory(project);
    Assert.assertEquals(new Path("webapps/first-webapp"), mainWebApp);
  }

  @Test
  public void testFindMainWebAppDirectory_returnsFolderWithWebXml() throws CoreException {
    IProject project = projectCreator.getProject();
    ResourceUtils.createFolders(project.getFolder("webapps/first-webapp/WEB-INF"), null);
    createEmptyFile(project, new Path("webapps/second-webapp/WEB-INF/web.xml"));
    ResourceUtils.createFolders(project.getFolder("third-webapp/WEB-INF"), null);
    ResourceUtils.createFolders(project.getFolder("WEB-INF"), null);

    IPath mainWebApp = FacetUtil.findMainWebAppDirectory(project);
    Assert.assertEquals(new Path("webapps/second-webapp"), mainWebApp);
  }

  @Test
  public void testFindMainWebAppDirectory_multipleFoldersWithWebXmls() throws CoreException {
    IProject project = projectCreator.getProject();
    createEmptyFile(project, new Path("webapps/first-webapp/WEB-INF/web.xml"));
    createEmptyFile(project, new Path("webapps/second-webapp/WEB-INF/web.xml"));
    ResourceUtils.createFolders(project.getFolder("WEB-INF"), null);

    IPath mainWebApp = FacetUtil.findMainWebAppDirectory(project);
    Assert.assertEquals(new Path("webapps/first-webapp"), mainWebApp);
  }

  @Test
  public void testHighestSatisfyingFacet() {
    IFacetedProjectWorkingCopy testProject = projectCreator.getFacetedProject().createWorkingCopy();
    testProject.addProjectFacet(AppEngineStandardFacet.JRE7);
    IProjectFacetVersion highestSatisfyingVersion =
        FacetUtil.getHighestSatisfyingVersion(testProject, WebFacetUtils.WEB_FACET, null);
    assertEquals(WebFacetUtils.WEB_25, highestSatisfyingVersion);
  }

  @Test
  public void testConflictsWith() {
    IFacetedProjectWorkingCopy testProject = projectCreator.getFacetedProject().createWorkingCopy();
    testProject.addProjectFacet(AppEngineStandardFacet.JRE7);
    assertFalse(FacetUtil.conflictsWith(testProject, WebFacetUtils.WEB_25));
    assertTrue(FacetUtil.conflictsWith(testProject, WebFacetUtils.WEB_31));

    // verify ignores the ignore list
    assertTrue(FacetUtil.conflictsWith(testProject, WebFacetUtils.WEB_31, null));
    assertFalse(FacetUtil.conflictsWith(testProject, WebFacetUtils.WEB_31,
        Arrays.asList(AppEngineStandardFacet.FACET)));
  }

  private static void createEmptyFile(IProject project, IPath relativePath) throws CoreException {
    ResourceUtils.createFolders(project.getFolder(relativePath.removeLastSegments(1)), null);

    InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
    project.getFile(relativePath).create(emptyStream, false /* force */, null /* monitor */);
    Assert.assertTrue(project.getFile(relativePath).exists());
  }
}
