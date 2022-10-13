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

package com.google.cloud.tools.eclipse.appengine.ui;

/**
 * Wizard page that displays a message that the Cloud SDK is
 * out of date with instructions on how to update it. This page disables the 'Finish' button.
 */
public class CloudSdkOutOfDatePage extends MissingComponentPage {

  public CloudSdkOutOfDatePage() {
    super("cloudSdkOutOfDatePage", //$NON-NLS-1$
        Messages.getString("cloud.sdk.out.of.date"),
        Messages.getString("cloud.sdk.out.of.date"),
        Messages.getString("fix.cloud.sdk.version"));
  }

}
