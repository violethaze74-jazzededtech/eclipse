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

import com.google.common.base.Preconditions;
import java.text.MessageFormat;

/**
 * Describes a Maven artifact.
 */
public class MavenCoordinates {

  public static final String LATEST_VERSION = "LATEST";
  public static final String JAR_TYPE = "jar";
  public static final String MAVEN_CENTRAL_REPO = "central";

  private String repository = MAVEN_CENTRAL_REPO;
  private String groupId;
  private String artifactId;
  private String version = LATEST_VERSION;
  private String type = JAR_TYPE;
  private String classifier;

  /**
   * @param groupId the Maven group ID, cannot be <code>null</code>
   * @param artifactId the Maven artifact ID, cannot be <code>null</code>
   */
  private MavenCoordinates(String groupId, String artifactId) {
    Preconditions.checkNotNull(groupId, "groupId null");
    Preconditions.checkNotNull(artifactId, "artifactId null");
    Preconditions.checkArgument(!groupId.isEmpty(), "groupId empty");
    Preconditions.checkArgument(!artifactId.isEmpty(), "artifactId empty");

    this.groupId = groupId;
    this.artifactId = artifactId;
  }

  /**
   * @return the Maven version of the artifact, defaults to special value
   *         {@link MavenCoordinates#LATEST_VERSION}, never <code>null</code>
   */
  public String getVersion() {
    return version;
  }

  /**
   * @return the Maven packaging type, defaults to <code>jar</code>, never <code>null</code>
   */
  public String getType() {
    return type;
  }

  /**
   * @return the Maven classifier or <code>null</code> if it was not set
   */
  public String getClassifier() {
    return classifier;
  }

  /**
   * @return the URI or the identifier of the repository used to download the artifact from, never
   *         <code>null</code>
   */
  public String getRepository() {
    return repository;
  }

  /**
   * @return the Maven group ID, never <code>null</code>
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * @return the Maven artifact ID, never <code>null</code>
   */
  public String getArtifactId() {
    return artifactId;
  }

  @Override
  public String toString() {
    return MessageFormat.format("MavenCoordinates [repository={0}, {1}:{2}:{3}:{4}:{5}]", repository,
        groupId, artifactId, type, classifier, version);
  }
  
  String toStringCoordinates() {
    return MessageFormat.format("{0}:{1}:{2}", groupId, artifactId, version);
  }
  
  public Builder toBuilder() {
    Builder builder = new Builder();
    builder.groupId = groupId;
    builder.artifactId = artifactId;
    builder.repository = repository;
    builder.version = version;
    builder.type = type;
    builder.classifier = classifier;
    return builder;
  }
  
  public static class Builder {
    private String repository = MAVEN_CENTRAL_REPO;
    private String groupId;
    private String artifactId;
    private String version = LATEST_VERSION;
    private String type = JAR_TYPE;
    private String classifier;

    public MavenCoordinates build() {
      MavenCoordinates coordinates = new MavenCoordinates(groupId, artifactId); 
      coordinates.repository = repository;
      coordinates.version = version;
      coordinates.type = type;
      coordinates.classifier = classifier;
      return coordinates;
    }
    
    /**
     * @param groupId the Maven group ID
     */
    public Builder setGroupId(String groupId) {
      Preconditions.checkNotNull(groupId, "groupId null");
      Preconditions.checkArgument(!groupId.isEmpty(), "groupId is empty");
      this.groupId = groupId;
      return this;
    }

    /**
     * @param artifactId the Maven artifact ID
     */
    public Builder setArtifactId(String artifactId) {
      Preconditions.checkNotNull(artifactId, "artifactId null");
      Preconditions.checkArgument(!artifactId.isEmpty(), "artifactId is empty");
      this.artifactId = artifactId;
      return this;
    }    
    
    /**
     * @param repository the URI or the identifier of the repository used to download the artifact
     *        from. Cannot be <code>null</code> or empty string.
     */
    public Builder setRepository(String repository) {
      Preconditions.checkNotNull(repository, "repository null");
      Preconditions.checkArgument(!repository.isEmpty(), "repository is empty");
      this.repository = repository;
      return this;
    }
    
    /**
     * @param type the Maven packaging type, defaults to <code>jar</code>, cannot be <code>null</code>
     *     or empty string.
     */
    public Builder setType(String type) {
      Preconditions.checkNotNull(type, "type is null");
      Preconditions.checkArgument(!type.isEmpty(), "type is empty");
      this.type = type;
      return this;
    }

    /**
     * @param version the Maven version of the artifact. Defaults to special value
     *     {@link MavenCoordinates#LATEST_VERSION}. Cannot be <code>null</code> or empty string.
     */
    public Builder setVersion(String version) {
      Preconditions.checkNotNull(version, "version is null");
      Preconditions.checkArgument(!version.isEmpty(), "version is empty");
      this.version = version;
      return this;
    }

    /**
     * @param classifier the Maven classifier, defaults to null.
     */
    public Builder setClassifier(String classifier) {
      this.classifier = classifier;
      return this;
    }
    
  }

}
