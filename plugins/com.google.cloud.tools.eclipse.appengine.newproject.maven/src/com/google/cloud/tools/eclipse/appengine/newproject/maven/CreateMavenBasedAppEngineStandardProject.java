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

package com.google.cloud.tools.eclipse.appengine.newproject.maven;

import com.google.cloud.tools.eclipse.appengine.facets.AppEngineStandardFacet;
import com.google.cloud.tools.eclipse.appengine.libraries.model.Library;
import com.google.cloud.tools.eclipse.util.MavenUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import org.apache.maven.archetype.catalog.Archetype;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.m2e.core.ui.internal.wizards.MappingDiscoveryJob;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

public class CreateMavenBasedAppEngineStandardProject extends WorkspaceModifyOperation {
  IProjectConfigurationManager projectConfigurationManager =
      MavenPlugin.getProjectConfigurationManager();

  private String packageName;
  private String artifactId;
  private String groupId;
  private String version;
  private IPath location;
  private Archetype archetype;
  private HashSet<String> appEngineLibraryIds = new HashSet<String>();

  @Override
  protected void execute(IProgressMonitor monitor)
      throws CoreException, InvocationTargetException, InterruptedException {
    SubMonitor progress = SubMonitor.convert(monitor);
    monitor.beginTask("Creating Maven AppEngine archetype", 100);

    String appengineArtifactVersion = MavenUtils.resolveLatestReleasedArtifactVersion(
        progress.newChild(20), "com.google.appengine", "appengine-api-1.0-sdk", "jar",
        AppEngineStandardFacet.DEFAULT_APPENGINE_SDK_VERSION);
    String gcloudArtifactVersion = MavenUtils.resolveLatestReleasedArtifactVersion(
        progress.newChild(20), "com.google.appengine", "gcloud-maven-plugin", "maven-plugin",
        AppEngineStandardFacet.DEFAULT_GCLOUD_PLUGIN_VERSION);

    Properties properties = new Properties();
    properties.put("appengine-version", appengineArtifactVersion);
    properties.put("gcloud-version", gcloudArtifactVersion);
    properties.put("useJstl", "true");
    // The project ID is currently necessary due to tool bugs.
    properties.put("application-id", artifactId);
    properties.put("useObjectify", Boolean.toString(appEngineLibraryIds.contains("objectify")));
    properties.put("useEndpoints1", Boolean.toString(appEngineLibraryIds.contains("appengine-endpoints")));
    properties.put("useEndpoints2", "false");
    properties.put("useAppEngineApi", Boolean.toString(appEngineLibraryIds.contains("appengine-api")));

    ProjectImportConfiguration importConfiguration = new ProjectImportConfiguration();
    String packageName = this.packageName == null || this.packageName.isEmpty()
        ? groupId : this.packageName;
    List<IProject> archetypeProjects = projectConfigurationManager.createArchetypeProjects(location,
        archetype, groupId, artifactId, version, packageName, properties,
        importConfiguration, progress.newChild(40));

    SubMonitor loopMonitor = progress.newChild(30).setWorkRemaining(3 * archetypeProjects.size());
    for (IProject project : archetypeProjects) {
      IFacetedProject facetedProject = ProjectFacetsManager.create(
          project, true, loopMonitor.newChild(1));
      AppEngineStandardFacet.installAppEngineFacet(facetedProject, true /* installDependentFacets */, loopMonitor.newChild(1));
      AppEngineStandardFacet.installAllAppEngineRuntimes(facetedProject, true /* force */, loopMonitor.newChild(1));
    }

    /*
     * invoke the Maven lifecycle mapping discovery job
     *
     * todo: is this step necessary? we know the archetype contents and we handle the
     * lifecycle-mappings and rules
     */
    Job job = new MappingDiscoveryJob(archetypeProjects);
    job.schedule();
  }

  /** Set the package for any generated code; may be {@code null} */
  void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  /** Set the Maven artifact identifier for the generated project */
  void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  /** Set the Maven group identifier for the generated project */
  void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Set the location where the project is to be generated; may be {@code null} to indicate the
   * workspace
   */
  void setLocation(IPath location) {
    this.location = location;
  }

  /**
   * Set the version of the project to be created.
   */
  void setVersion(String version) {
    this.version = version;
  }

  void setArchetype(Archetype archetype) {
    this.archetype = archetype;
  }

  void setAppEngineLibraryIds(List<Library> libraries) {
    appEngineLibraryIds = new HashSet<String>();
    for (int i = 0; i < libraries.size(); i++) {
      appEngineLibraryIds.add(libraries.get(i).getId());
    }
  }

}
