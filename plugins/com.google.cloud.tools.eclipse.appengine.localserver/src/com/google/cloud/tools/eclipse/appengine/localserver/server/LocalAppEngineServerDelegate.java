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

package com.google.cloud.tools.eclipse.appengine.localserver.server;

import com.google.cloud.tools.eclipse.appengine.facets.AppEngineStandardFacet;
import com.google.cloud.tools.eclipse.appengine.localserver.Messages;
import com.google.cloud.tools.eclipse.util.status.StatusUtil;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.facets.FacetUtil;
import org.eclipse.wst.server.core.model.ServerDelegate;

@SuppressWarnings("restriction") // For FacetUtil
public class LocalAppEngineServerDelegate extends ServerDelegate {
  private static final IModule[] EMPTY_MODULES = new IModule[0];
  private static final String SERVLET_MODULE_FACET = "jst.web"; //$NON-NLS-1$
  private static final String ATTR_APP_ENGINE_SERVER_MODULES = "app-engine-server-modules-list"; //$NON-NLS-1$

  /**
   * Returns a {@link LocalAppEngineServerDelegate} instance associated with the
   * {@code server} or a new {@link LocalAppEngineServerDelegate} instance if a
   * {@link LocalAppEngineServerDelegate} instance cannot be found for {@code server}.
   *
   * @param server the App Engine server
   * @return a new {@link LocalAppEngineServerDelegate} instance or the one associated with
   *         {@code server}
   */
  public static LocalAppEngineServerDelegate getAppEngineServer(IServer server) {
    LocalAppEngineServerDelegate serverDelegate = server.getAdapter(LocalAppEngineServerDelegate.class);
    if (serverDelegate == null) {
      serverDelegate = (LocalAppEngineServerDelegate) server.loadAdapter(LocalAppEngineServerDelegate.class, null);
    }
    return serverDelegate;
  }

  /**
   * Returns OK status if the projects associated with modules to be added support the App Engine
   * runtime, otherwise returns an ERROR status.
   */
  @Override
  public IStatus canModifyModules(IModule[] add, IModule[] remove) {
    if (add != null) {
      for (IModule module : add) {
        if (module.getProject() != null) {
          IStatus supportedFacets = FacetUtil.verifyFacets(module.getProject(), getServer());
          if (supportedFacets != null && !supportedFacets.isOK()) {
            return supportedFacets;
          }
          IStatus appEngineStandardFacetPresent = hasAppEngineStandardFacet(module);
          if (appEngineStandardFacetPresent != null && !appEngineStandardFacetPresent.isOK()) {
            return appEngineStandardFacetPresent;
          }
        }
      }
    }
    return Status.OK_STATUS;
  }

  private static IStatus hasAppEngineStandardFacet(IModule module) {
    try {
      if (AppEngineStandardFacet.hasAppEngineFacet(ProjectFacetsManager.create(module.getProject()))) {
        return Status.OK_STATUS;
      } else {
        return StatusUtil.error(LocalAppEngineServerDelegate.class, NLS.bind(Messages.GAE_STANDARD_FACET_MISSING,
                                                                             module.getName(),
                                                                             module.getProject().getName()));
      }
    } catch (CoreException ex) {
      return StatusUtil.error(LocalAppEngineServerDelegate.class,
                              NLS.bind(Messages.NOT_FACETED_PROJECT, module.getProject().getName()),
                              ex);
    }
  }

  /**
   * If the module is a web module returns the utility modules contained within its WAR, otherwise
   * returns an empty list.
   * 
   * @param module the module path traversed to this point
   */
  @Override
  public IModule[] getChildModules(IModule[] module) {
    if (module == null || module.length == 0) {
      return EMPTY_MODULES;
    }
    IModule thisModule = module[module.length - 1];
    if (thisModule != null && thisModule.getModuleType() != null) {
      IModuleType moduleType = thisModule.getModuleType();
      if (moduleType != null && SERVLET_MODULE_FACET.equals(moduleType.getId())) { //$NON-NLS-1$
        IWebModule webModule = (IWebModule) thisModule.loadAdapter(IWebModule.class, null);
        if (webModule != null) {
          IModule[] modules = webModule.getModules();
          return modules;
        }
      }
    }
    return EMPTY_MODULES;
  }

  @Override
  public IModule[] getRootModules(IModule module) throws CoreException {
    return new IModule[] { module };
  }

  @SuppressWarnings("unchecked")
  @Override
  public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor)
      throws CoreException {
    List<String> modules = this.getAttribute(ATTR_APP_ENGINE_SERVER_MODULES, (List<String>) null);

    if (add != null && add.length > 0) {
      // TODO: ensure modules have same Project ID
      // throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
      // "This server instance cannot run more than one application", null));
      if (modules == null) {
        modules = new ArrayList<>();
      }

      for (int i = 0; i < add.length; i++) {
        if (!modules.contains(add[i].getId())) {
          modules.add(add[i].getId());
        }
      }
    }

    if (remove != null && remove.length > 0 && modules != null) {
      for (int i = 0; i < remove.length; i++) {
        modules.remove(remove[i].getId());
      }
      // schedule server stop as App Engine server cannot run without modules.
      if (modules.isEmpty()) {
        getServer().stop(true);
      }
    }
    if (modules != null) {
      setAttribute(ATTR_APP_ENGINE_SERVER_MODULES, modules);
    }
  }
}
