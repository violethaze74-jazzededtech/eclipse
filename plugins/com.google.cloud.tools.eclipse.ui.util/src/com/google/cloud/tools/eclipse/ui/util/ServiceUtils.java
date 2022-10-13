/*******************************************************************************
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/

package com.google.cloud.tools.eclipse.ui.util;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Utility class for using OSGi services, such as locating OSGi services from a desired context.
 */
public class ServiceUtils {

  /**
   * Returns an OSGi service from {@link ExecutionEvent}. It looks up a service in the following
   * locations (if exist) in the given order:
   *
   * {@code HandlerUtil.getActiveSite(event)}
   * {@code HandlerUtil.getActiveEditor(event).getEditorSite()}
   * {@code HandlerUtil.getActiveEditor(event).getSite()}
   * {@code HandlerUtil.getActiveWorkbenchWindow(event)}
   * {@code PlatformUI.getWorkbench()}
   */
  public static <T> T getService(ExecutionEvent event, Class<T> api) {
    IWorkbenchSite activeSite = HandlerUtil.getActiveSite(event);
    if (activeSite != null) {
      return activeSite.getService(api);
    }

    IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
    if (activeEditor != null) {
      IEditorSite editorSite = activeEditor.getEditorSite();
      if (editorSite != null) {
        return editorSite.getService(api);
      }
      IWorkbenchPartSite site = activeEditor.getSite();
      if (site != null) {
        return site.getService(api);
      }
    }

    IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
    if (workbenchWindow != null) {
      return workbenchWindow.getService(api);
    }

    return PlatformUI.getWorkbench().getService(api);
  }

}
