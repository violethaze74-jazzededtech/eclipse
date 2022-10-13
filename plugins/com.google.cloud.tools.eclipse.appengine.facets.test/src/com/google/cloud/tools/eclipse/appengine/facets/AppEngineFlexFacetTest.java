package com.google.cloud.tools.eclipse.appengine.facets;

import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.junit.Assert;
import org.junit.Test;

public class AppEngineFlexFacetTest {
  @Test
  public void testFlexFacetExists() {
    Assert.assertTrue(
        ProjectFacetsManager.isProjectFacetDefined("com.google.cloud.tools.eclipse.appengine.facets.flex"));
  }
}
