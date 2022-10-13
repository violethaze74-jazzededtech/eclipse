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

package com.google.cloud.tools.eclipse.appengine.deploy;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AppEngineProjectDeployerTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  private IPath deployablesDirectory;

  @Before
  public void setUp() {
    deployablesDirectory = new Path(tempFolder.getRoot().toString());
  }

  @Test
  public void testPresetDeployables() {
    assertEquals(6, AppEngineProjectDeployer.APP_ENGINE_DEPLOYABLES.size());
    assertDeployableDefined("app.yaml", "appengine-web.xml", false /* optional */);
    assertDeployableDefined("cron.yaml", "cron.xml", true);
    assertDeployableDefined("dispatch.yaml", "dispatch.xml", true);
    assertDeployableDefined("dos.yaml", "dos.xml", true);
    assertDeployableDefined("index.yaml", "datastore-indexes.xml", true);
    assertDeployableDefined("queue.yaml", "queue.xml", true);
  }

  private static void assertDeployableDefined(String yaml, String xml, boolean optional) {
    assertTrue(AppEngineProjectDeployer.APP_ENGINE_DEPLOYABLES.stream().anyMatch(
        item -> item.yaml.equals(yaml) && item.xml.equals(xml) && item.optional == optional));
  }

  @Test
  public void testComputeDeployables_xmlFiles() throws IOException {
    createAllXmlDeployables();

    List<File> result = AppEngineProjectDeployer.computeDeployables(deployablesDirectory, true);
    assertEquals(6, result.size());
    assertHasDeployable(result, "appengine-web.xml");
    assertHasDeployable(result, "cron.xml");
    assertHasDeployable(result, "datastore-indexes.xml");
    assertHasDeployable(result, "dispatch.xml");
    assertHasDeployable(result, "dos.xml");
    assertHasDeployable(result, "queue.xml");
  }

  @Test
  public void testComputeDeployables_yamlFiles() throws IOException {
    createAllYamlDeployables();

    List<File> result = AppEngineProjectDeployer.computeDeployables(deployablesDirectory, true);
    assertEquals(6, result.size());
    assertHasDeployable(result, "app.yaml");
    assertHasDeployable(result, "cron.yaml");
    assertHasDeployable(result, "index.yaml");
    assertHasDeployable(result, "dispatch.yaml");
    assertHasDeployable(result, "dos.yaml");
    assertHasDeployable(result, "queue.yaml");
  }

  @Test
  public void testComputeDeployables_yamlFilesTakePrecedence() throws IOException {
    createAllYamlDeployables();
    createAllXmlDeployables();

    List<File> result = AppEngineProjectDeployer.computeDeployables(deployablesDirectory, true);
    assertEquals(6, result.size());
    assertHasDeployable(result, "app.yaml");
    assertHasDeployable(result, "cron.yaml");
    assertHasDeployable(result, "index.yaml");
    assertHasDeployable(result, "dispatch.yaml");
    assertHasDeployable(result, "dos.yaml");
    assertHasDeployable(result, "queue.yaml");
  }

  @Test
  public void testComputeDeployables_doNotAddOptionals() throws IOException {
    createAllYamlDeployables();
    createAllXmlDeployables();

    List<File> result = AppEngineProjectDeployer.computeDeployables(deployablesDirectory,
        false /* addOptionals */);
    assertEquals(1, result.size());
    assertHasDeployable(result, "app.yaml");
  }

  private void assertHasDeployable(List<File> result, String filename) {
    assertThat(result, hasItem(deployablesDirectory.append(filename).toFile()));
  }

  private void createAllYamlDeployables() throws IOException {
    tempFolder.newFile("app.yaml");
    tempFolder.newFile("cron.yaml");
    tempFolder.newFile("index.yaml");
    tempFolder.newFile("dispatch.yaml");
    tempFolder.newFile("dos.yaml");
    tempFolder.newFile("queue.yaml");
  }

  private void createAllXmlDeployables() throws IOException {
    tempFolder.newFile("appengine-web.xml");
    tempFolder.newFile("cron.xml");
    tempFolder.newFile("datastore-indexes.xml");
    tempFolder.newFile("dispatch.xml");
    tempFolder.newFile("dos.xml");
    tempFolder.newFile("queue.xml");
  }
}
