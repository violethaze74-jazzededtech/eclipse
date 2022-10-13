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

package com.google.cloud.tools.eclipse.appengine.deploy;

import static org.junit.Assert.assertTrue;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.eclipse.test.util.project.TestProjectCreator;
import com.google.cloud.tools.eclipse.util.io.ResourceUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CloudSdkStagingHelperTest {

  private static final String APP_YAML = "runtime: java\nenv: flex";

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();
  @Rule public TestProjectCreator projectCreator = new TestProjectCreator();

  @Mock private IProgressMonitor monitor;

  @Test
  public void testStageFlexible() throws CoreException, AppEngineException {
    IProject project = projectCreator.getProject();
    IPath stagingDirectory = new Path(tempFolder.getRoot().toString());

    createFile(project, "src/main/appengine/app.yaml", APP_YAML);

    IFolder appEngineDirectory = project.getFolder("src/main/appengine");
    IPath deployArtifact = createFile(project, "my-app.war", "fake WAR").getLocation();

    CloudSdkStagingHelper.stageFlexible(
        appEngineDirectory.getLocation(), deployArtifact, stagingDirectory, monitor);

    assertTrue(stagingDirectory.append("app.yaml").toFile().exists());
    assertTrue(stagingDirectory.append("my-app.war").toFile().exists());
  }

  private static IFile createFile(IProject project, String path, String content)
      throws CoreException {
    InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    IFile file = project.getFile(path);
    ResourceUtils.createFolders(file.getParent(), null);
    file.create(in, true, null);
    return file;
  }
}
