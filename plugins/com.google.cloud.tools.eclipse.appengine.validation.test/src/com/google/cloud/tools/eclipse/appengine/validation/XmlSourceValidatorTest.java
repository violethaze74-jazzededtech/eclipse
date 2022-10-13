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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.eclipse.appengine.facets.AppEngineStandardFacet;
import com.google.cloud.tools.eclipse.test.util.project.TestProjectCreator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.IncrementalHelper;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.IncrementalReporter;
import org.eclipse.wst.validation.internal.core.ValidationException;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

public class XmlSourceValidatorTest {

  private static final String APPLICATION_XML =
      "<appengine-web-app xmlns='http://appengine.google.com/ns/1.0'>"
      + "<application>"
      + "</application>"
      + "</appengine-web-app>";

  private final IncrementalReporter reporter = new IncrementalReporter(null);

  @Rule public TestProjectCreator nonFacetedProject = new TestProjectCreator();

  @Rule public TestProjectCreator dynamicWebProject =
      new TestProjectCreator().withFacets(JavaFacet.VERSION_1_7, WebFacetUtils.WEB_25);

  @Rule public TestProjectCreator appEngineStandardProject =
      new TestProjectCreator().withFacets(JavaFacet.VERSION_1_7,
          WebFacetUtils.WEB_25, AppEngineStandardFacet.JRE7);

  @Test
  public void testValidate_appEngineStandardFacet() throws CoreException, ValidationException {
    IProject project = appEngineStandardProject.getProject();
    IFile file = project.getFile("testdata.xml");
    file.create(ValidationTestUtils.stringToInputStream(APPLICATION_XML), 0, null);

    IDocument document = ValidationTestUtils.getDocument(file);

    // Adds the URI of the file to be validated to the IncrementalHelper.
    IncrementalHelper helper = new IncrementalHelper(document, project);
    IPath path = file.getFullPath();
    helper.setURI(path.toString());

    XmlSourceValidator validator = new XmlSourceValidator();
    validator.setHelper(new AppEngineWebXmlValidator());
    validator.connect(document);
    validator.validate(helper, reporter);
    assertEquals(1, reporter.getMessages().size());
  }

  @Test
  public void testValidate_dynamicWebProject() throws CoreException, ValidationException {
    IProject project = dynamicWebProject.getProject();
    IFile file = project.getFile("testdata.xml");
    file.create(ValidationTestUtils.stringToInputStream(APPLICATION_XML), 0, null);

    IDocument document = ValidationTestUtils.getDocument(file);

    // Adds the URI of the file to be validated to the IncrementalHelper.
    IncrementalHelper helper = new IncrementalHelper(document, project);
    IPath path = file.getFullPath();
    helper.setURI(path.toString());

    XmlSourceValidator validator = new XmlSourceValidator();
    validator.setHelper(new AppEngineWebXmlValidator());
    validator.connect(document);
    validator.validate(helper, reporter);
    assertTrue(reporter.getMessages().isEmpty());
  }

  @Test
  public void testValidate_noBannedElements() throws IOException {
    XmlSourceValidator validator = new XmlSourceValidator();
    validator.setHelper(new AppEngineWebXmlValidator());
    byte[] xml = "<test></test>".getBytes(StandardCharsets.UTF_8);
    validator.validate(reporter, null, xml);
    assertTrue(reporter.getMessages().isEmpty());
  }

  @Test
  public void testValidate() throws IOException {
    XmlSourceValidator validator = new XmlSourceValidator();
    validator.setHelper(new WebXmlValidator());
    String xml = "<web-app xmlns='http://xmlns.jcp.org/xml/ns/javaee' version='3.1'></web-app>";
    IFile file = Mockito.mock(IFile.class);
    when(file.getProject()).thenReturn(appEngineStandardProject.getProject());
    validator.validate(reporter, file, xml.getBytes(StandardCharsets.UTF_8));
    assertEquals(1, reporter.getMessages().size());
  }

  @Test
  public void getDocumentEncodingTest() throws CoreException {
    IProject project = dynamicWebProject.getProject();
    IFile file = project.getFile("testdata.xml");
    file.create(ValidationTestUtils.stringToInputStream(APPLICATION_XML), IFile.FORCE, null);
    IDocument document = ValidationTestUtils.getDocument(file);
    assertEquals("UTF-8", XmlSourceValidator.getDocumentEncoding(document));
  }

  @Test
  public void testCreateMessage() {
    XmlSourceValidator validator = new XmlSourceValidator();
    validator.setHelper(new AppEngineWebXmlValidator());
    BannedElement element =
        new AppEngineBlacklistElement("application", new DocumentLocation(5, 17), 0);
    validator.createMessage(reporter, element, 0);
    List<IMessage> messages = reporter.getMessages();
    assertEquals(1, messages.size());
    IMessage iMessage = messages.get(0);
    String markerId = "com.google.cloud.tools.eclipse.appengine.validation.applicationMarker";
    assertEquals(markerId, iMessage.getMarkerId());
  }

  @Test
  public void testGetFile() throws CoreException {
    IProject project = dynamicWebProject.getProject();
    IFile file = project.getFile("testdata.xml");
    file.create(ValidationTestUtils.stringToInputStream(APPLICATION_XML), 0, null);

    assertTrue(file.exists());

    IPath path = file.getFullPath();
    IFile testFile = XmlSourceValidator.getFile(path.toString());

    assertNotNull(testFile);
    assertEquals(file, testFile);
  }

  @Test
  public void testGetProject() throws CoreException {
    IProject project = dynamicWebProject.getProject();
    IFile file = project.getFile("testdata.xml");
    file.create(ValidationTestUtils.stringToInputStream(APPLICATION_XML), 0, null);

    IDocument document = ValidationTestUtils.getDocument(file);

    IncrementalHelper helper = new IncrementalHelper(document, project);
    IPath path = file.getFullPath();
    helper.setURI(path.toString());

    IProject testProject = XmlSourceValidator.getProject(helper);
    assertNotNull(testProject);
    assertEquals(project, testProject);
  }

  @Test
  public void testNoErrorOnNonFacetedProject() throws CoreException, ValidationException {
    IProject project = nonFacetedProject.getProject();
    assertNull("project should have not been faceted", ProjectFacetsManager.create(project));

    project.getFolder("folder").create(true, true, null);
    project.getFile("folder/file.ext").create(new ByteArrayInputStream(new byte[0]), true, null);
    assertTrue(project.getFile("folder/file.ext").exists());

    IValidationContext validationContext = mock(IValidationContext.class);
    when(validationContext.getURIs()).thenReturn(
        new String[] {project.getName() + "/folder/file.ext"});

    new XmlSourceValidator().validate(validationContext, reporter);
    // Should not throw NPE and exit normally.
  }
}
