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

package com.google.cloud.tools.eclipse.util;

import com.google.cloud.tools.eclipse.util.status.StatusUtil;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MavenUtils {

  public static final String MAVEN2_NATURE_ID = "org.eclipse.m2e.core.maven2Nature"; //$NON-NLS-1$

  private static final Logger logger = Logger.getLogger(MavenUtils.class.getName());

  private static final String MAVEN_LATEST_VERSION = "LATEST"; //$NON-NLS-1$
  private static final String POM_XML_NAMESPACE_URI = "http://maven.apache.org/POM/4.0.0"; //$NON-NLS-1$

  /**
   * Returns {@code true} if the given project has the Maven 2 nature. This
   * checks for the Maven nature used by m2Eclipse 1.0.0.
   */
  public static boolean hasMavenNature(IProject project) {
    try {
      return NatureUtils.hasNature(project, MavenUtils.MAVEN2_NATURE_ID);
    } catch (CoreException coreException) {
      logger.log(Level.SEVERE, "Unable to examine natures on project " + project.getName(),
          coreException);
      return false;
    }
  }

  public static Artifact resolveArtifact(IProgressMonitor monitor, String groupId,
      String artifactId, String type, String version, String classifier,
      List<ArtifactRepository> repositories) throws CoreException {
    Artifact artifact = MavenPlugin.getMaven().resolve(groupId, artifactId, version, type,
        classifier, repositories, monitor);
    return artifact;
  }

  public static String getProperty(InputStream pomXml, String propertyName) throws CoreException {
    return getTopLevelValue(parse(pomXml), "properties", propertyName); //$NON-NLS-1$
  }

  private static Document parse(InputStream pomXml) throws CoreException {
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      return documentBuilderFactory.newDocumentBuilder().parse(pomXml);
    } catch (IOException | SAXException | ParserConfigurationException exception) {
      throw new CoreException(
          StatusUtil.error(MavenUtils.class, "Cannot parse pom.xml", exception));
    }
  }

  private static String getTopLevelValue(Document doc, String parentTagName, String childTagName)
      throws CoreException {
    try {
      NodeList parentElements = doc.getElementsByTagNameNS(POM_XML_NAMESPACE_URI, parentTagName);
      if (parentElements.getLength() > 0) {
        Node parent = parentElements.item(0);
        if (parent.hasChildNodes()) {
          for (int i = 0; i < parent.getChildNodes().getLength(); ++i) {
            Node child = parent.getChildNodes().item(i);
            if (child.getNodeName().equals(childTagName) && (child.getNamespaceURI() == null
                || POM_XML_NAMESPACE_URI.equals(child.getNamespaceURI()))) {
              return child.getTextContent();
            }
          }
        }
      }
      return null;
    } catch (DOMException exception) {
      throw new CoreException(StatusUtil.error(
          MavenUtils.class, "Missing pom.xml element: " + childTagName, exception));
    }
  }

  public static ArtifactRepository createRepository(String id, String url) throws CoreException {
    return MavenPlugin.getMaven().createArtifactRepository(id, url);
  }

  /**
   * Checks if an artifact is available in the local repository. The artifact <code>version</code>
   * must be a specific value, cannot be "LATEST".
   */
  public static boolean isArtifactAvailableLocally(String groupId, String artifactId,
                                                   String version, String type,
                                                   String classifier) {
    try {
      Preconditions.checkArgument(!MAVEN_LATEST_VERSION.equals(version));
      String artifactPath =
          MavenPlugin.getMaven().getLocalRepository()
              .pathOf(new DefaultArtifact(groupId, artifactId, version, null /* scope */, type,
                                          classifier, new DefaultArtifactHandler(type)));
      return new File(artifactPath).exists();
    } catch (CoreException ex) {
      logger.log(Level.SEVERE, "Could not lookup local repository", ex);
      return false;
    }
  }
}
