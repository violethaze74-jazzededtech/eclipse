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

package com.google.cloud.tools.eclipse.appengine.libraries.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A jar file that is downloaded from the location defined by {@link MavenCoordinates}. It can have
 * associated filters to control visibility of classes and packages contained in the jar file.
 */
public class LibraryFile {

  private List<Filter> filters = Collections.emptyList();
  private MavenCoordinates mavenCoordinates;
  private URI javadocUri;
  private URI sourceUri;
  private boolean export = true;

  public LibraryFile(MavenCoordinates mavenCoordinates) {
    Preconditions.checkNotNull(mavenCoordinates, "mavenCoordinates is null");
    this.mavenCoordinates = mavenCoordinates;
  }

  // todo Danger! This exposes mutable internal state
  public MavenCoordinates getMavenCoordinates() {
    return mavenCoordinates;
  }

  public List<Filter> getFilters() {
    return new ArrayList<>(filters);
  }

  void setFilters(List<Filter> filters) {
    if (filters != null) {
      this.filters = new ArrayList<>(filters);
    }
  }

  public URI getJavadocUri() {
    return javadocUri;
  }

  void setJavadocUri(URI javadocUri) {
    this.javadocUri = javadocUri;
  }

  public URI getSourceUri() {
    return sourceUri;
  }

  @VisibleForTesting
  public void setSourceUri(URI sourceUri) {
    this.sourceUri = sourceUri;
  }

  public boolean isExport() {
    return export;
  }

  void setExport(boolean export) {
    this.export = export;
  }
}
