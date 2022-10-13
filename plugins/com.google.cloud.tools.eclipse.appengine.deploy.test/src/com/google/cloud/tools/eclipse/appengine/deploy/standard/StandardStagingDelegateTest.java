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

package com.google.cloud.tools.eclipse.appengine.deploy.standard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.tools.eclipse.appengine.deploy.StagingDelegate;
import com.google.cloud.tools.eclipse.appengine.facets.AppEngineStandardFacet;
import com.google.cloud.tools.eclipse.test.util.project.TestProjectCreator;
import com.google.cloud.tools.eclipse.util.io.ResourceUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StandardStagingDelegateTest {

  @Rule public TestProjectCreator projectCreator = new TestProjectCreator().withFacets(
      JavaFacet.VERSION_1_7, WebFacetUtils.WEB_25, AppEngineStandardFacet.JRE7);

  private IProject project;
  private IPath safeWorkDirectory;
  private IPath stagingDirectory;

  @Before
  public void setUp() throws CoreException {
    project = projectCreator.getProject();
    safeWorkDirectory = project.getFolder("safe-work-directory").getLocation();
    stagingDirectory = project.getFolder("staging-result").getLocation();
    createConfigFile("cron.xml");
  }

  @Test
  public void testStage() {
    StagingDelegate delegate = new StandardStagingDelegate(project);
    delegate.stage(stagingDirectory, safeWorkDirectory, null, null, new NullProgressMonitor());

    assertTrue(stagingDirectory.append("WEB-INF").toFile().exists());
    assertTrue(stagingDirectory.append("WEB-INF/appengine-web.xml").toFile().exists());
    assertTrue(stagingDirectory.append("WEB-INF/cron.xml").toFile().exists());
    assertTrue(stagingDirectory.append("META-INF").toFile().exists());
  }

  @Test
  public void testGetOptionalConfigurationFilesDirectory() {
    StagingDelegate delegate = new StandardStagingDelegate(project);
    delegate.stage(stagingDirectory, safeWorkDirectory, null, null, new NullProgressMonitor());

    assertEquals(stagingDirectory.append("WEB-INF"),
        delegate.getDeployablesDirectory());
    assertTrue(stagingDirectory.append("WEB-INF/cron.xml").toFile().exists());
  }

  private void createConfigFile(String path) throws CoreException {
    InputStream in = new ByteArrayInputStream("".getBytes());
    IFile file = project.getFile("WebContent/WEB-INF/" + path);
    ResourceUtils.createFolders(file.getParent(), null);
    file.create(in, true, null);
  }
}
