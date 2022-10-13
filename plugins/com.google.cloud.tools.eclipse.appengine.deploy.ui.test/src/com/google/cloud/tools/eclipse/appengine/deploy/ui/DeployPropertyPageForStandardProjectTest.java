package com.google.cloud.tools.eclipse.appengine.deploy.ui;

import com.google.cloud.tools.eclipse.test.util.project.TestProjectCreator;
import java.util.Arrays;
import org.eclipse.core.resources.IProject;
import org.eclipse.jst.common.project.facet.core.JavaFacet;
import org.eclipse.jst.j2ee.web.project.facet.WebFacetUtils;
import org.junit.Rule;

public class DeployPropertyPageForStandardProjectTest
    extends AbstractDeployPropertyPageForProjectsTest<StandardDeployPreferencesPanel> {

  @Rule
  public TestProjectCreator standardProjectCreator =
      new TestProjectCreator().withFacetVersions(Arrays.asList(
          JavaFacet.VERSION_1_7, WebFacetUtils.WEB_25, APP_ENGINE_STANDARD_FACET_1));

  @Override
  protected IProject getProject() {
    return standardProjectCreator.getProject();
  }

  @Override
  protected Class<StandardDeployPreferencesPanel> getPanelClass() {
    return StandardDeployPreferencesPanel.class;
  }
}
