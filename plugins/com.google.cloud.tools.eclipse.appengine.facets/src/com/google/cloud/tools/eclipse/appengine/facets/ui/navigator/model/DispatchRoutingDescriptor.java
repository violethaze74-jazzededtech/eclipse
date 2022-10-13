/*
 * Copyright 2018 Google LLC
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

package com.google.cloud.tools.eclipse.appengine.facets.ui.navigator.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;

/**
 * Represents a {@code dispatch.xml} element.
 */
public class DispatchRoutingDescriptor extends AppEngineResourceElement {
  public DispatchRoutingDescriptor(IFacetedProject project, IFile file) {
    super(project, file);
  }

  @Override
  public StyledString getStyledLabel() {
    return new StyledString("Dispatch Routing Rules");
  }

}
