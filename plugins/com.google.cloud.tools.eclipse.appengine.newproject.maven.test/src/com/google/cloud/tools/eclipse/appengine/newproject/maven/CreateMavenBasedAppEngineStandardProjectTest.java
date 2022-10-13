package com.google.cloud.tools.eclipse.appengine.newproject.maven;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.project.IProjectConfigurationManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;;

@RunWith(MockitoJUnitRunner.class)
public class CreateMavenBasedAppEngineStandardProjectTest {

  @Mock
  private IProjectConfigurationManager manager;

  private NullProgressMonitor monitor = new NullProgressMonitor();
  
  @Test
  public void testConstructor()
      throws InvocationTargetException, CoreException, InterruptedException {
    CreateMavenBasedAppEngineStandardProject operation =
        new CreateMavenBasedAppEngineStandardProject();
    operation.setGroupId("group");
    operation.setArtifactId("artifact");

    operation.projectConfigurationManager = manager;

    operation.execute(monitor);
  }

}
