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

package com.google.cloud.tools.eclipse.appengine.libraries.ui;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.cloud.tools.eclipse.appengine.libraries.model.CloudLibraries;
import com.google.cloud.tools.eclipse.appengine.libraries.model.Library;
import com.google.cloud.tools.eclipse.test.util.ui.ShellTestResource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ClientApisLibrariesSelectorGroupTest {

  @Rule public ShellTestResource shellTestResource = new ShellTestResource();

  private Shell shell;
  private LibrarySelectorGroup librariesSelector;
  private SWTBotCheckBox cloudStorageButton;
  private SWTBotCheckBox cloudTranslateButton;

  @Before
  public void setUp() {
    shell = shellTestResource.getShell();
    shell.setLayout(new FillLayout());
    librariesSelector =
        new LibrarySelectorGroup(shell, CloudLibraries.CLIENT_APIS_GROUP, "label", false);
    shell.open();
    cloudStorageButton = getButton("googlecloudstorage");
    cloudTranslateButton = getButton("cloudtranslation");
  }

  @Test
  public void testButtonSetup() {
    Control container = shell.getChildren()[0];
    assertThat(container, instanceOf(Composite.class));
    Control groupAsControl = ((Composite) container).getChildren()[0];
    assertThat(groupAsControl, instanceOf(Group.class));
    Control[] buttonsAsControls = ((Group) groupAsControl).getChildren();
    for (int i = 0; i < buttonsAsControls.length; i++) {
      Control control = buttonsAsControls[i];
      assertThat(control, instanceOf(Button.class));
      Button button = (Button) control;
      assertNotNull(button.getData());
      assertThat(button.getData(), instanceOf(Library.class));
    }
  }

  @Test
  public void testToolTips() {
    assertTrue(cloudStorageButton.getToolTipText().length() > 0);
    assertTrue(cloudTranslateButton.getToolTipText().length() > 0);
  }

  @Test
  public void testInitiallyNoLibrariesSelected() {
    assertTrue(getSelectedLibrariesSorted().isEmpty());
  }
  
  @Test
  public void testSelectCloudStorage() {
    cloudStorageButton.click();
    List<Library> selectedLibraries = getSelectedLibrariesSorted();
    assertNotNull(selectedLibraries);
    assertEquals(1, selectedLibraries.size());
    assertEquals("googlecloudstorage", selectedLibraries.get(0).getId());
  }

  @Test
  public void testUnselectCloudStorage() {
    cloudStorageButton.click(); // select
    cloudStorageButton.click(); // unselect
    List<Library> selectedLibraries = getSelectedLibrariesSorted();
    assertNotNull(selectedLibraries);
    assertEquals(0, selectedLibraries.size());
  }

  private SWTBotCheckBox getButton(String libraryId) {
    for (Button button : librariesSelector.getLibraryButtons()) {
      if (libraryId.equals(((Library) button.getData()).getId())) {
        return new SWTBotCheckBox(button);
      }
    }
    fail("Could not find button for " + libraryId);
    return null; // won't be reached
  }
  
  private List<Library> getSelectedLibrariesSorted() {
    List<Library> selectedLibraries = new ArrayList<>(librariesSelector.getSelectedLibraries());
    Collections.sort(selectedLibraries, new LibraryComparator());
    return selectedLibraries;
  }
  
}