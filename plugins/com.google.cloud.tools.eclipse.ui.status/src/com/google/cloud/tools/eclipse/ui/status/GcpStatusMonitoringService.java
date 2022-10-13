/*
 * Copyright 2018 Google Inc.
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

package com.google.cloud.tools.eclipse.ui.status;

import com.google.cloud.tools.eclipse.util.jobs.Consumer;

/** Represents a service for querying the Google Cloud Platform status. */
public interface GcpStatusMonitoringService {

  /** Return the latest status. */
  GcpStatus getCurrentStatus();

  /** Request notification of any changes. */
  void addStatusChangeListener(Consumer<GcpStatusMonitoringService> listener);

  /** Request to be no longer notified of any changes. */
  void removeStatusChangeListener(Consumer<GcpStatusMonitoringService> listener);
}
