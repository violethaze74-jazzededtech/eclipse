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

package com.google.cloud.tools.eclipse.ui.util;

import com.google.cloud.tools.eclipse.util.AdapterUtil;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class ProjectFromSelectionHelper {

  public static List<IProject> getProjects(ExecutionEvent event) throws ExecutionException {
    List<IProject> projects = new ArrayList<>();

    ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      for (Object selected : structuredSelection.toList()) {
        IProject project = AdapterUtil.adapt(selected, IProject.class);
        if (project != null) {
          projects.add(project);
        }
      }
    }

    return projects;
  }

  public static IProject getFirstProject(ExecutionEvent event) throws ExecutionException {
    List<IProject> projects = getProjects(event);
    if (projects.isEmpty()) {
      return null;
    } else {
      return projects.get(0);
    }
  }

}
