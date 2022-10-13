package com.google.cloud.tools.eclipse.appengine.newproject.maven;

import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.archetype.catalog.Archetype;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import com.google.cloud.tools.eclipse.appengine.ui.AppEngineImages;
import com.google.common.annotations.VisibleForTesting;

/**
 * UI to select an archetype in creating a new Maven-based App Engine Standard Java project.
 */
public class MavenAppEngineStandardArchetypeWizardPage extends WizardPage {

  @VisibleForTesting
  static final java.util.List<ArchetypeTuple> PRESET_ARCHETYPES =
      Collections.unmodifiableList(Arrays.asList(
          new ArchetypeTuple("com.google.appengine.archetypes", //$NON-NLS-1$
              "appengine-skeleton-archetype", //$NON-NLS-1$
              Messages.getString("APPENGINE_SKELETON_ARCHETYPE_DISPLAY_NAME"), //$NON-NLS-1$
              Messages.getString("APPENGINE_SKELETON_ARCHETYPE_DESCRIPTION")), //$NON-NLS-1$
          new ArchetypeTuple("com.google.appengine.archetypes", //$NON-NLS-1$
              "guestbook-archetype", //$NON-NLS-1$
              Messages.getString("APPENGINE_GUESTBOOK_ARCHETYPE_DISPLAY_NAME"), //$NON-NLS-1$
              Messages.getString("APPENGINE_GUESTBOOK_ARCHETYPE_DESCRIPTION")))); //$NON-NLS-1$

  // UI components
  private List archetypeList;
  private StyledText descriptionBox;

  public MavenAppEngineStandardArchetypeWizardPage() {
    super("newProjectArchetypePage"); //$NON-NLS-1$
    setTitle("Maven-based App Engine Standard Project");
    setDescription("Select an Archetype");
    setImageDescriptor(AppEngineImages.googleCloudPlatform(32));
  }

  @Override
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    GridLayoutFactory.swtDefaults().numColumns(2).applyTo(container);

    // List of (selectable) archetypes on the left
    archetypeList = new List(container, SWT.SINGLE | SWT.BORDER);
    for (ArchetypeTuple tuple : PRESET_ARCHETYPES) {
      archetypeList.add(tuple.displayName);
    }
    archetypeList.setSelection(0);
    GridDataFactory.defaultsFor(archetypeList).grab(true, true).applyTo(archetypeList);
    archetypeList.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        int index = archetypeList.getSelectionIndex();
        descriptionBox.setText(PRESET_ARCHETYPES.get(index).description);
      }
    });

    // Text box describing a selected archetype on the right
    descriptionBox = new StyledText(container, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.READ_ONLY);
    descriptionBox.setText(PRESET_ARCHETYPES.get(0).description);
    descriptionBox.setEnabled(false);
    descriptionBox.setMargins(5, 5, 5, 5);
    GridDataFactory.defaultsFor(descriptionBox).applyTo(descriptionBox);

    setControl(container);

    Dialog.applyDialogFont(container);
  }

  public Archetype getArchetype() {
    return PRESET_ARCHETYPES.get(archetypeList.getSelectionIndex()).archetype;
  }

  @Override
  public boolean isPageComplete() {
    return isCurrentPage();
  }

  @VisibleForTesting
  static class ArchetypeTuple {
    Archetype archetype;
    String displayName;
    String description;

    ArchetypeTuple(String groupId, String artifactId, String displayName, String description) {
      archetype = new Archetype();
      archetype.setGroupId(groupId);
      archetype.setArtifactId(artifactId);
      archetype.setVersion("LATEST"); //$NON-NLS-1$
      this.displayName = displayName;
      this.description = description;
    }
  };
}
