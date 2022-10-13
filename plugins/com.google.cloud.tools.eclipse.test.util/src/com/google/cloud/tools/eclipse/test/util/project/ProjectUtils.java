/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.eclipse.test.util.project;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Joiner;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.common.project.facet.core.util.internal.ZipUtil;
import org.eclipse.wst.validation.internal.operations.ValidationBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * A set of utility methods for dealing with projects.
 */
public class ProjectUtils {
  private static boolean DEBUG = false;

  /**
   * Import the Eclipse projects found within the bundle containing {@code clazz} at the
   * {@code relativeLocation}. Return the list of projects imported.
   *
   * @throws IOException if the zip cannot be accessed
   * @throws CoreException if a project cannot be imported
   */
  public static List<IProject> importProjects(Class<?> clazz, String relativeLocation,
      boolean checkBuildErrors, IProgressMonitor monitor)
      throws IOException, CoreException {
    SubMonitor progress = SubMonitor.convert(monitor, 100);

    // Resolve the zip from within this bundle
    Bundle bundle = FrameworkUtil.getBundle(clazz);
    URL bundleLocation = bundle.getResource(relativeLocation);
    assertNotNull(bundleLocation);
    URL zipLocation = FileLocator.toFileURL(bundleLocation);
    if (!zipLocation.getProtocol().equals("file")) {
      throw new IOException("could not resolve location to a file");
    }
    File zippedFile = new File(zipLocation.getPath());
    assertTrue(zippedFile.exists());
    progress.worked(5);

    IWorkspaceRoot root = getWorkspace().getRoot();
    // extract projects into our workspace using WTP internal utility class
    // assumes projects are contained in subdirectories within the zip
    ZipUtil.unzip(zippedFile, root.getLocation().toFile(), progress.newChild(10));

    List<IPath> projectFiles = new ArrayList<>();
    try (ZipFile zip = new ZipFile(zippedFile)) {
      for (Enumeration<? extends ZipEntry> it = zip.entries(); it.hasMoreElements();) {
        ZipEntry entry = it.nextElement();
        if (entry.getName().endsWith("/.project")) {
          IPath projectFileLocation = root.getLocation().append(new Path(entry.getName()));
          projectFiles.add(projectFileLocation);
        }
      }
      progress.worked(5);
    }

    // import the projects
    progress.setWorkRemaining(10 * projectFiles.size() + 10);
    List<IProject> projects = new ArrayList<>(projectFiles.size());
    for (IPath projectFile : projectFiles) {
      IProjectDescription descriptor =
          root.getWorkspace().loadProjectDescription(projectFile);
      IProject project = root.getProject(descriptor.getName());
      // bring in the project to the workspace
      project.create(descriptor, progress.newChild(2));
      project.open(progress.newChild(8));
      projects.add(project);
    }

    // wait for any post-import operations too
    waitForProjects();
    if (checkBuildErrors) {
      failIfBuildErrors("Imported projects have errors", projects);
    }

    return projects;
  }

  /** Fail if there are any build errors on any project in the workspace. */
  public static void failIfBuildErrors() throws CoreException {
    failIfBuildErrors("Projects have build errors", getWorkspace().getRoot().getProjects());
  }

  /** Fail if there are any build errors on the specified projects. */
  public static void failIfBuildErrors(String message, Collection<IProject> projects)
      throws CoreException {
    failIfBuildErrors(message, projects.toArray(new IProject[projects.size()]));
  }

  /** Fail if there are any build errors on the specified projects. */
  public static void failIfBuildErrors(String message, IProject... projects) throws CoreException {
    List<String> errors = getAllBuildErrors(projects);
    assertTrue(message + "\n" + Joiner.on("\n").join(errors), errors.isEmpty());
  }

  /** Return a list of all build errors on the specified projects. */
  public static List<String> getAllBuildErrors(IProject... projects) throws CoreException {
    List<String> errors = new ArrayList<>();
    for (IProject project : projects) {
      IMarker[] problems = project.findMarkers(IMarker.PROBLEM, true /* includeSubtypes */,
          IResource.DEPTH_INFINITE);
      for (IMarker problem : problems) {
        int severity = problem.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
        if (severity >= IMarker.SEVERITY_ERROR) {
          errors.add(formatProblem(problem));
        }
      }
    }
    return errors;
  }

  private static String formatProblem(IMarker problem) {
    StringBuilder sb = new StringBuilder();
    sb.append(problem.getResource().getFullPath());
    sb.append(':');
    sb.append(problem.getAttribute(IMarker.LINE_NUMBER, -1));
    sb.append(": ");
    sb.append(problem.getAttribute(IMarker.MESSAGE, ""));
    return sb.toString();
  }

  /** Wait for any spawned jobs and builds to complete (e.g., validation jobs). */
  public static void waitForProjects(IProject... projects) {
    Runnable delayTactic = new Runnable() {
      public void run() {
        Display display = Display.getCurrent();
        if (display != null) {
          while (display.readAndDispatch()) {
            /* spin */
          }
        }
        Thread.yield();
      }
    };
    waitForProjects(delayTactic, projects);
  }

  /** Wait for any spawned jobs and builds to complete (e.g., validation jobs). */
  public static void waitForProjects(Runnable delayTactic, IProject... projects) {
    IJobManager jobManager = Job.getJobManager();
    try {
      Set<Job> jobs = new LinkedHashSet<>();
      List<String> allBuildErrors;
      do {
        delayTactic.run();
        for (Job job : jobs) {
          job.join();
        }
        jobs.clear();

        Collections.addAll(jobs, jobManager.find(ResourcesPlugin.FAMILY_MANUAL_BUILD));
        Collections.addAll(jobs, jobManager.find(ResourcesPlugin.FAMILY_AUTO_BUILD));
        // J2EEElementChangedListener.PROJECT_COMPONENT_UPDATE_JOB_FAMILY
        Collections.addAll(jobs, jobManager.find("org.eclipse.jst.j2ee.refactor.component"));
        // ServerPlugin.SHUTDOWN_JOB_FAMILY
        Collections.addAll(jobs, jobManager.find("org.eclipse.wst.server.core.family"));
        Collections.addAll(jobs, jobManager.find("org.eclipse.wst.server.ui.family"));
        Collections.addAll(jobs, jobManager.find(ValidationBuilder.FAMILY_VALIDATION_JOB));
        allBuildErrors = getAllBuildErrors(projects);
        if (DEBUG) {
          if (!jobs.isEmpty()) {
            System.err.printf("ProjectUtils#waitForProjects: waiting for %d jobs: %s\n",
                jobs.size(), jobs);
          } else if (!allBuildErrors.isEmpty()) {
            System.err.printf("ProjectUtils#waitForProjects: waiting for %d build errors\n",
                allBuildErrors.size());
          } else {
            // report any other jobs found in case we're missing something above
            Job[] otherJobs = jobManager.find(null);
            System.err.printf("Ignoring %d unrelated jobs:\n", otherJobs.length);
            for (Job job : otherJobs) {
              System.err.printf("  %s: %s\n", job.getClass().getName(), job);
            }
            // new ThreadDumpingWatchdog(0, TimeUnit.DAYS).run();
          }
        }
      } while (!(jobs.isEmpty() && allBuildErrors.isEmpty()));
    } catch (CoreException | InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  private static IWorkspace getWorkspace() {
    return ResourcesPlugin.getWorkspace();
  }

  private ProjectUtils() {}
}
