/*
 * Copyright 2017 Google LLC
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

package com.google.cloud.tools.eclipse.integration.appengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.tools.eclipse.appengine.compat.cte13.CloudToolsEclipseProjectUpdater;
import com.google.cloud.tools.eclipse.appengine.facets.AppEngineStandardFacet;
import com.google.cloud.tools.eclipse.test.util.ThreadDumpingWatchdog;
import com.google.cloud.tools.eclipse.test.util.ZipUtil;
import com.google.cloud.tools.eclipse.test.util.project.JavaRuntimeUtils;
import com.google.cloud.tools.eclipse.test.util.project.ProjectUtils;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

/**
 * Test that we can import packaged native applications from previous versions of Cloud Tools for
 * Eclipse.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ImportNativeAppEngineStandardProjectTest extends BaseProjectTest {
  @Rule
  public ThreadDumpingWatchdog timer = new ThreadDumpingWatchdog(2, TimeUnit.MINUTES);

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void importAppEngineStandardJava7_from1_3_1() throws IOException, CoreException {
    assertFalse(projectExists("AESv7"));
    ZipUtil.extractZip(new URL(
        "platform:/plugin/com.google.cloud.tools.eclipse.integration.appengine/test-projects/cte-1_3_1-appengine-standard-java7.zip"),
        tempFolder.getRoot());
    project = SwtBotAppEngineActions.importNativeProject(bot, "AESv7", tempFolder.getRoot());
    assertTrue(project.exists());

    updateOldContainers();

    ProjectUtils.waitForProjects(project);
    ProjectUtils.waitUntilNoBuildErrors(project);

    IFacetedProject facetedProject = ProjectFacetsManager.create(project);
    assertNotNull("should be a faceted project", facetedProject);

    IProjectFacetVersion appEngineFacetVersion =
        facetedProject.getProjectFacetVersion(AppEngineStandardFacet.FACET);
    assertNotNull("Project does not have AES facet", appEngineFacetVersion);
    assertEquals("Project should have AES Java 7", "JRE7",
        appEngineFacetVersion.getVersionString());
    assertEquals(JavaFacet.VERSION_1_7, facetedProject.getProjectFacetVersion(JavaFacet.FACET));
    assertEquals(WebFacetUtils.WEB_25,
        facetedProject.getProjectFacetVersion(WebFacetUtils.WEB_FACET));
  }

  @Test
  public void importAppEngineStandardJava8_from1_3_1() throws IOException, CoreException {
    Assume.assumeTrue(JavaRuntimeUtils.hasJavaSE8());
    assertFalse(projectExists("AESv8"));
    ZipUtil.extractZip(new URL(
        "platform:/plugin/com.google.cloud.tools.eclipse.integration.appengine/test-projects/cte-1_3_1-appengine-standard-java8.zip"),
        tempFolder.getRoot());
    project = SwtBotAppEngineActions.importNativeProject(bot, "AESv8", tempFolder.getRoot());
    assertTrue(project.exists());

    updateOldContainers();

    ProjectUtils.waitForProjects(project);
    ProjectUtils.waitUntilNoBuildErrors(project);

    IFacetedProject facetedProject = ProjectFacetsManager.create(project);
    assertNotNull("should be a faceted project", facetedProject);

    IProjectFacetVersion appEngineFacetVersion =
        facetedProject.getProjectFacetVersion(AppEngineStandardFacet.FACET);
    assertNotNull("Project does not have AES facet", appEngineFacetVersion);
    assertEquals("Project should have AES Java 8", "JRE8",
        appEngineFacetVersion.getVersionString());
    assertEquals(JavaFacet.VERSION_1_8, facetedProject.getProjectFacetVersion(JavaFacet.FACET));
    assertEquals(WebFacetUtils.WEB_31,
        facetedProject.getProjectFacetVersion(WebFacetUtils.WEB_FACET));
  }

  private void updateOldContainers() {
    assertTrue(CloudToolsEclipseProjectUpdater.hasOldContainers(project));
    IStatus updateStatus =
        CloudToolsEclipseProjectUpdater.updateProject(project, SubMonitor.convert(null));
    assertTrue("Update failed: " + updateStatus.getMessage(), updateStatus.isOK());
    assertFalse(CloudToolsEclipseProjectUpdater.hasOldContainers(project));
  }

}
