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

package com.google.cloud.tools.eclipse.appengine.facets;

import com.google.common.base.Preconditions;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

public class AppEngineFlexWarFacet {
  public static final String ID = "com.google.cloud.tools.eclipse.appengine.facets.flex";
  public static final String VERSION = "1";

  public static final IProjectFacet FACET = ProjectFacetsManager.getProjectFacet(ID);
  public static final IProjectFacetVersion FACET_VERSION = FACET.getVersion(VERSION);

  /**
   * Returns true if project has the App Engine Flex WAR facet and false otherwise.
   *
   * @param project should not be null
   * @return true if project has the App Engine Flex WAR facet and false otherwise
   */
  public static boolean hasFacet(IFacetedProject project) {
    Preconditions.checkNotNull(project);
    return project.hasProjectFacet(FACET);
  }

  /**
   * Installs the App Engine Flexible WAR facet if {@code facetedProject} does not have it already.
   *
   * @param facetedProject the faceted project receiving the App Engine facet
   * @param installDependentFacets true if the facets required by the App Engine facet should be
   *        installed, false otherwise
   * @param monitor the progress monitor
   * @throws CoreException if anything goes wrong during install
   */
  public static void installAppEngineFacet(IFacetedProject facetedProject,
      boolean installDependentFacets, IProgressMonitor monitor) throws CoreException {
    if (facetedProject.hasProjectFacet(FACET)) {
      return;
    }

    FacetUtil facetUtil = new FacetUtil(facetedProject);
    if (installDependentFacets) {
      facetUtil.addJavaFacetToBatch(JavaFacet.VERSION_1_8);
      facetUtil.addWebFacetToBatch(WebFacetUtils.WEB_31);
    }

    facetUtil.addFacetToBatch(FACET_VERSION, null /* config */);
    facetUtil.install(monitor);
  }
}
