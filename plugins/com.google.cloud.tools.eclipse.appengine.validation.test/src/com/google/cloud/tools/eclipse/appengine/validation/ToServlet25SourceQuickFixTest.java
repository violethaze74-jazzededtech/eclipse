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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.cloud.tools.eclipse.test.util.project.TestProjectCreator;
import com.google.cloud.tools.eclipse.ui.util.WorkbenchUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.junit.Rule;
import org.junit.Test;

public class ToServlet25SourceQuickFixTest {

  @Rule
  public TestProjectCreator projectCreator = new TestProjectCreator();

  @Test
  public void testConvertServlet() throws CoreException {
    IProject project = projectCreator.getProject();
    IFile file = project.getFile("web.xml");
    String webXml = "<web-app xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" version='3.1'/>";
    file.create(ValidationTestUtils.stringToInputStream(webXml), IFile.FORCE, null);

    IWorkbench workbench = PlatformUI.getWorkbench();
    IEditorPart editorPart = WorkbenchUtil.openInEditor(workbench, file);
    ITextViewer viewer = ValidationTestUtils.getViewer(file);
    String preContents = viewer.getDocument().get();

    assertTrue(preContents.contains("version='3.1'"));

    XsltSourceQuickFix quickFix = new ToServlet25SourceQuickFix();
    quickFix.apply(viewer, 'a', 0, 0);

    IDocument document = viewer.getDocument();
    String contents = document.get();
    assertFalse(contents.contains("version='3.1'"));
    assertTrue(contents.contains("version=\"2.5\""));

    // https://github.com/GoogleCloudPlatform/google-cloud-eclipse/issues/1527
    editorPart.doSave(new NullProgressMonitor());
  }

}
