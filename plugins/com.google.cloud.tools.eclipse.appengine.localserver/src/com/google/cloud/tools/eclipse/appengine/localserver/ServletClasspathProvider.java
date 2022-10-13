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

package com.google.cloud.tools.eclipse.appengine.localserver;

import com.google.cloud.tools.eclipse.appengine.libraries.ILibraryClasspathContainerResolverService;
import com.google.cloud.tools.eclipse.appengine.libraries.repository.ILibraryRepositoryService;
import com.google.cloud.tools.eclipse.util.MavenUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
import org.eclipse.wst.server.core.IRuntime;

/**
 * Supply Java standard classes, specifically servlet-api.jar and jsp-api.jar,
 * to non-Maven projects.
 * <p>
 * The jars are resolved using {@link ILibraryRepositoryService}.
 */
public class ServletClasspathProvider extends RuntimeClasspathProviderDelegate {

  private static final Logger logger = Logger.getLogger(ServletClasspathProvider.class.getName());
  
  @Inject
  private ILibraryClasspathContainerResolverService resolverService;

  public ServletClasspathProvider() {
  }

  @Override
  public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime) {
    if (project != null && MavenUtils.hasMavenNature(project)) { // Maven handles its own classpath
      return null;
    } else {
      return doResolveClasspathContainer(runtime);
    }
  }

  // TODO this method is called often as the result of user initiated UI actions, e.g. when the user
  // clicks through the project in the Project Explorer view to drill down into the libraries
  // attached to the project. This method resolves the servlet and jsp jars each time (instead of
  // persisting the resolved version the first time and use that later). So we must ensure that if
  // the supported versions change in the future (e.g. a new GAE runtime starts
  // supporting Servlet API 3.1), then resolved versions of these jars remain the same for a project
  // as long as it is associated with the originally selected runtime (i.e. the one that supports
  // Servlet 2.5)
  // https://github.com/GoogleCloudPlatform/google-cloud-eclipse/issues/953
  @Override
  public IClasspathEntry[] resolveClasspathContainer(IRuntime runtime) {
    return doResolveClasspathContainer(runtime);
  }

  private IClasspathEntry[] doResolveClasspathContainer(IRuntime runtime) {
    try {
        IClasspathEntry[] servletApiEntries =
            resolverService.resolveLibraryAttachSourcesSync("servlet-api");
        IClasspathEntry[] jspApiEntries =
            resolverService.resolveLibraryAttachSourcesSync("jsp-api");
        
        IClasspathEntry[] allEntries =
            new IClasspathEntry[servletApiEntries.length + jspApiEntries.length];
        System.arraycopy(servletApiEntries, 0, allEntries, 0, servletApiEntries.length);
        System.arraycopy(jspApiEntries, 0, 
                         allEntries, servletApiEntries.length,
                         jspApiEntries.length);
        return allEntries;
    } catch (CoreException ex) {
      logger.log(Level.WARNING, "Failed to initialize libraries", ex);
    }
    return null;
  }
}
