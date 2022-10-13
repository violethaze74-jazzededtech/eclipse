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

package com.google.cloud.tools.eclipse.appengine.facets.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.eclipse.appengine.facets.ui.AppEngineStandardProjectConvertCommandHandler.MessageDialogWrapper;
import com.google.cloud.tools.eclipse.test.util.project.TestProjectCreator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AppEngineStandardProjectConvertCommandHandlerTest {

  @Rule public final TestProjectCreator projectCreator = new TestProjectCreator();

  @Mock private MessageDialogWrapper mockDialogWrapper;

  private IFacetedProject facetedProject;

  private AppEngineStandardProjectConvertCommandHandler commandHandler =
      new AppEngineStandardProjectConvertCommandHandler();

  @Before
  public void setUp() throws CoreException {
    facetedProject = ProjectFacetsManager.create(projectCreator.getProject());
  }

  @Test
  public void testCheckFacetCompatibility_noFacetsInstalled() throws CoreException {
    assertFalse(facetedProject.hasProjectFacet(JavaFacet.FACET));
    assertFalse(facetedProject.hasProjectFacet(WebFacetUtils.WEB_FACET));

    assertTrue(commandHandler.checkFacetCompatibility(facetedProject, mockDialogWrapper));
    verify(mockDialogWrapper, never()).openInformation(anyString(), anyString());
  }

  @Test
  public void testCheckFacetCompatibility_java1_7FacetIsCompatible() throws CoreException {
    facetedProject.installProjectFacet(JavaFacet.VERSION_1_7, null, null);
    assertTrue(facetedProject.hasProjectFacet(JavaFacet.VERSION_1_7));

    assertTrue(commandHandler.checkFacetCompatibility(facetedProject, mockDialogWrapper));
    verify(mockDialogWrapper, never()).openInformation(anyString(), anyString());
  }

  @Test
  public void testCheckFacetCompatibility_web2_5FacetIsCompatible() throws CoreException {
    facetedProject.installProjectFacet(JavaFacet.VERSION_1_7, null, null);
    facetedProject.installProjectFacet(WebFacetUtils.WEB_25, null, null);
    assertTrue(facetedProject.hasProjectFacet(WebFacetUtils.WEB_25));

    assertTrue(commandHandler.checkFacetCompatibility(facetedProject, mockDialogWrapper));
    verify(mockDialogWrapper, never()).openInformation(anyString(), anyString());
  }

  @Test
  public void testCheckFacetCompatibility_java1_6FacetIsIncompatible() throws CoreException {
    facetedProject.installProjectFacet(JavaFacet.VERSION_1_6, null, null);
    assertTrue(facetedProject.hasProjectFacet(JavaFacet.VERSION_1_6));

    assertFalse(commandHandler.checkFacetCompatibility(facetedProject, mockDialogWrapper));
    verify(mockDialogWrapper, times(1)).openInformation(anyString(), anyString());
  }

  @Test
  public void testCheckFacetCompatibility_java1_8FacetIsIncompatible() throws CoreException {
    facetedProject.installProjectFacet(JavaFacet.VERSION_1_8, null, null);
    assertTrue(facetedProject.hasProjectFacet(JavaFacet.VERSION_1_8));

    assertFalse(commandHandler.checkFacetCompatibility(facetedProject, mockDialogWrapper));
    verify(mockDialogWrapper, times(1)).openInformation(anyString(), anyString());
  }

  @Test
  public void testCheckFacetCompatibility_web2_4FacetIsIncompatible() throws CoreException {
    facetedProject.installProjectFacet(JavaFacet.VERSION_1_7, null, null);
    facetedProject.installProjectFacet(WebFacetUtils.WEB_24, null, null);
    assertTrue(facetedProject.hasProjectFacet(WebFacetUtils.WEB_24));

    assertFalse(commandHandler.checkFacetCompatibility(facetedProject, mockDialogWrapper));
    verify(mockDialogWrapper, times(1)).openInformation(anyString(), anyString());
  }

  @Test
  public void testCheckFacetCompatibility_web3_0FacetIsIncompatible() throws CoreException {
    facetedProject.installProjectFacet(JavaFacet.VERSION_1_7, null, null);
    facetedProject.installProjectFacet(WebFacetUtils.WEB_30, null, null);
    assertTrue(facetedProject.hasProjectFacet(WebFacetUtils.WEB_30));

    assertFalse(commandHandler.checkFacetCompatibility(facetedProject, mockDialogWrapper));
    verify(mockDialogWrapper, times(1)).openInformation(anyString(), anyString());
  }
}
