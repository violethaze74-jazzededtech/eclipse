/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.eclipse.dataflow.core.project;

import com.google.cloud.tools.eclipse.dataflow.core.project.VerifyStagingLocationJob.VerifyStagingLocationResult;
import com.google.cloud.tools.eclipse.util.jobs.FuturisticJob;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A job that verifies that a Staging Location exists.
 */
public class VerifyStagingLocationJob extends FuturisticJob<VerifyStagingLocationResult> {
  private final GcsDataflowProjectClient client;
  private final String email;
  private final String stagingLocation;

  public VerifyStagingLocationJob(GcsDataflowProjectClient client,
      String email, String stagingLocation) {
    super("Verify Staging Location " + stagingLocation);
    this.client = client;
    this.email = email;
    this.stagingLocation = stagingLocation;
  }

  @Override
  protected VerifyStagingLocationResult compute(IProgressMonitor monitor) {
    boolean locationIsAccessible = client.locationIsAccessible(stagingLocation);
    return new VerifyStagingLocationResult(email, stagingLocation, locationIsAccessible);
  }

  public String getEmail() {
    return email;
  }

  public String getStagingLocation() {
    return stagingLocation;
  }

  /**
   * The result of verifying a staging location: the staging location, the account email used
   * to access the location, and the verification result.
   */
  public static class VerifyStagingLocationResult {
    public final String email;
    public final String stagingLocation;
    public final boolean accessible;

    public VerifyStagingLocationResult(String email, String stagingLocation, boolean accessible) {
      this.email = email;
      this.stagingLocation = stagingLocation;
      this.accessible = accessible;
    }
  }
}
