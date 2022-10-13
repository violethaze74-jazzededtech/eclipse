/*
 * Copyright 2017 Google Inc. All Rights Reserved.
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

package com.google.cloud.tools.eclipse.integration.appengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.tools.eclipse.appengine.facets.AppEngineStandardFacet;
import com.google.cloud.tools.eclipse.test.util.ArrayAssertions;
import com.google.cloud.tools.eclipse.test.util.ThreadDumpingWatchdog;
import com.google.cloud.tools.eclipse.test.util.ZipUtil;
import com.google.cloud.tools.eclipse.test.util.project.ProjectUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
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
 * Import snapshot of <a href=
 * "https://github.com/GoogleCloudPlatform/getting-started-java/tree/master/appengine-standard-java8/springboot-appengine-standard">Hello
 * Spring Boot on App Engine standard environment</a>.
 * 
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ImportMavenAppEngineStandardProjectTest extends BaseProjectTest {
  @Rule
  public ThreadDumpingWatchdog timer = new ThreadDumpingWatchdog(2, TimeUnit.MINUTES);

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
  
  @Test
  public void runImport() throws Exception {
    Assume.assumeTrue("No JavaSE 8 JRE found", hasJavaSE8());
    assertFalse(projectExists("springboot-appengine-standard"));
    extractZip(new URL(
        "platform:/plugin/com.google.cloud.tools.eclipse.integration.appengine/test-projects/springboot-appengine-standard.zip"),
        tempFolder.getRoot());
    project = SwtBotAppEngineActions.importMavenProject(bot, "springboot-appengine-standard",
        tempFolder.getRoot());
    assertTrue(project.exists());

    ProjectUtils.failIfBuildErrors("Imported Maven project has errors", project);

    IFacetedProject facetedProject = ProjectFacetsManager.create(project);
    assertNotNull("m2e-wtp should create a faceted project", facetedProject);

    IProjectFacetVersion appEngineFacetVersion =
        facetedProject.getProjectFacetVersion(AppEngineStandardFacet.FACET);
    assertNotNull("Project does not have AES facet", appEngineFacetVersion);
    assertEquals("Project should have AES Java 8", "JRE8",
        appEngineFacetVersion.getVersionString());
    assertEquals(JavaFacet.VERSION_1_8, facetedProject.getProjectFacetVersion(JavaFacet.FACET));
    assertEquals(WebFacetUtils.WEB_31,
        facetedProject.getProjectFacetVersion(WebFacetUtils.WEB_FACET));

    ArrayAssertions.assertIsEmpty("runtime classpath should be empty for Maven projects",
        NewMavenBasedAppEngineProjectWizardTest.getAppEngineServerRuntimeClasspathEntries(project));
  }

  /**
   * Check that we have a Java 8 compatible VM available.
   */
  private boolean hasJavaSE8() {
    IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
    IExecutionEnvironment java8 = manager.getEnvironment("JavaSE-1.8");
    return java8 != null && java8.getCompatibleVMs().length > 0;
  }

  private static File extractZip(URL zipProjectLocation, File destination) throws IOException {
    URL zipLocation = FileLocator.toFileURL(zipProjectLocation);
    if (!zipLocation.getProtocol().equals("file")) {
      throw new IOException("could not resolve location to a file");
    }
    File zippedFile = new File(zipLocation.getPath());
    assertTrue(zippedFile.exists());
    IStatus status = ZipUtil.unzip(zippedFile, destination, new NullProgressMonitor());
    assertTrue("failed to extract: " + status, status.isOK());
    return destination;
  }
}
