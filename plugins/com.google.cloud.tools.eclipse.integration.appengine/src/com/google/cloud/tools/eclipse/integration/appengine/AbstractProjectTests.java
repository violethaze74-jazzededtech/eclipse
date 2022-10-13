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

package com.google.cloud.tools.eclipse.integration.appengine;

import com.google.cloud.tools.eclipse.swtbot.SwtBotProjectActions;
import com.google.cloud.tools.eclipse.swtbot.SwtBotWorkbenchActions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.After;
import org.junit.BeforeClass;

/**
 * Common infrastructure for workbench-based tests that create a single project.
 */
public class AbstractProjectTests {

  protected static SWTWorkbenchBot bot;
  protected IProject project;

  @BeforeClass
  public static void setUp() throws Exception {
    bot = new SWTWorkbenchBot();
    SwtBotWorkbenchActions.closeWelcome(bot);
  }

  @After
  public void tearDown() {
    if (project != null) {
      // ensure there are no jobs
      SwtBotWorkbenchActions.waitForIdle(bot);
      SwtBotProjectActions.deleteProject(bot, project.getName());
      project = null;
    }
    bot.resetWorkbench();
  }

  /**
   * Returns the named project; it may not yet exist.
   */
  private static IProject findProject(String projectName) {
    return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
  }

  /**
   * Return true if a project with the given name exists.
   */
  protected static boolean projectExists(String projectName) {
    IProject project = findProject(projectName);
    return project.exists();
  }
}
