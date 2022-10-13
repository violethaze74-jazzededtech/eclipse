/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.eclipse.appengine.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.tools.eclipse.test.util.project.TestProjectCreator;
import com.google.cloud.tools.eclipse.ui.util.WorkbenchUtil;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Must be run as a plugin test.
 */
public class XsltQuickFixPluginTest {

  private static final String APPLICATION_XML =
      "<appengine-web-app xmlns='http://appengine.google.com/ns/1.0'>"
      + "<application>"
      + "</application>"
      + "</appengine-web-app>";

  private IFile file;
  private DocumentBuilder builder;

  @Rule public TestProjectCreator projectCreator = new TestProjectCreator();

  @Before
  public void setup() throws ParserConfigurationException {
    IProject project = projectCreator.getProject();
    file = project.getFile("testdata.xml");
    
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);
    builder = builderFactory.newDocumentBuilder();
  }

  @Test
  public void testRun_removeApplicationElement() throws IOException, CoreException, SAXException {
    file.create(ValidationTestUtils.stringToInputStream(APPLICATION_XML), IFile.FORCE, null);

    IMarker marker =
        file.createMarker("com.google.cloud.tools.eclipse.appengine.validation.runtimeMarker");
    Assert.assertTrue(marker.exists());
    
    XsltQuickFix fix = new XsltQuickFix("/xslt/removeApplication.xsl",
        Messages.getString("remove.application.element"));
    fix.run(marker);

    Assert.assertFalse(marker.exists());
    
    Document transformed = builder.parse(file.getContents());
    assertEquals(0, transformed.getDocumentElement().getChildNodes().getLength());
  }

  @Test
  public void testRun_removeVersionElement() throws IOException, SAXException, CoreException {

    String versionXml =
        "<appengine-web-app xmlns='http://appengine.google.com/ns/1.0'>"
        + "<version>"
        + "</version>"
        + "</appengine-web-app>";
    
    file.create(ValidationTestUtils.stringToInputStream(
        versionXml), IFile.FORCE, null);
    IMarker marker =
        file.createMarker("com.google.cloud.tools.eclipse.appengine.validation.runtimeMarker");
    Assert.assertTrue(marker.exists());

    XsltQuickFix fix = new XsltQuickFix("/xslt/removeVersion.xsl",
        Messages.getString("remove.version.element"));
    fix.run(marker);
    Assert.assertFalse(marker.exists());

    Document transformed = builder.parse(file.getContents());
    assertEquals(0, transformed.getDocumentElement().getChildNodes().getLength());

    assertTrue(file.isSynchronized(0));
    assertEquals(1, file.getHistory(null).length);
  }

  @Test
  public void testRun_UpgradeRuntimeElement() throws IOException, SAXException, CoreException {

    String xml = "<appengine-web-app xmlns='http://appengine.google.com/ns/1.0'/>";
    
    file.create(ValidationTestUtils.stringToInputStream(xml), IFile.FORCE, null);
    IMarker marker =
        file.createMarker("com.google.cloud.tools.eclipse.appengine.validation.runtimeMarker");
    Assert.assertTrue(marker.exists());

    XsltQuickFix fix = new XsltQuickFix("/xslt/upgradeRuntime.xsl", "");
    fix.run(marker);
    Assert.assertFalse(marker.exists());

    Document transformed = builder.parse(file.getContents());
    NodeList children = transformed.getDocumentElement().getChildNodes();
    Element runtime = (Element) children.item(0);

    assertEquals("runtime", runtime.getLocalName());
    assertEquals("java8", runtime.getTextContent());
    
    assertTrue(file.isSynchronized(0));
    assertEquals(1, file.getHistory(null).length);
  }  
  
  @Test
  public void testRun_existingEditor() throws CoreException {
    file.create(ValidationTestUtils.stringToInputStream(APPLICATION_XML), IFile.FORCE, null);

    IWorkbench workbench = PlatformUI.getWorkbench();
    IEditorPart editor = WorkbenchUtil.openInEditor(workbench, file);

    IDocument preDocument = XsltQuickFix.getCurrentDocument(file);
    String preContents = preDocument.get();
    assertTrue(preContents.contains("application"));

    IMarker marker =
        file.createMarker("com.google.cloud.tools.eclipse.appengine.validation.runtimeMarker");
    Assert.assertTrue(marker.exists());

    XsltQuickFix fix = new XsltQuickFix("/xslt/removeApplication.xsl",
        Messages.getString("remove.application.element"));
    fix.run(marker);
    Assert.assertFalse(marker.exists());

    IDocument document = XsltQuickFix.getCurrentDocument(file);
    String contents = document.get();
    assertFalse(contents.contains("application"));
    // https://github.com/GoogleCloudPlatform/google-cloud-eclipse/issues/1527
    editor.doSave(new NullProgressMonitor());
  }

  @Test
  public void testGetCurrentDocument_existingEditor() throws CoreException {
    file.create(ValidationTestUtils.stringToInputStream(APPLICATION_XML), IFile.FORCE, null);

    IWorkbench workbench = PlatformUI.getWorkbench();
    IEditorPart editor = WorkbenchUtil.openInEditor(workbench, file);

    assertNotNull(XsltQuickFix.getCurrentDocument(file));

    // https://github.com/GoogleCloudPlatform/google-cloud-eclipse/issues/1527
    editor.doSave(new NullProgressMonitor());
  }

  @Test
  public void testGetCurrentDocument_noEditor() throws CoreException {
    file.create(ValidationTestUtils.stringToInputStream(APPLICATION_XML), IFile.FORCE, null);
    assertNull(XsltQuickFix.getCurrentDocument(file));
  }

}
