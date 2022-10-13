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

package com.google.cloud.tools.eclipse.appengine.localserver;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "com.google.cloud.tools.eclipse.appengine.localserver.messages"; //$NON-NLS-1$

  public static String NOT_FACETED_PROJECT;
  public static String GAE_STANDARD_FACET_MISSING;
  public static String NEW_SERVER_DIALOG_PORT;
  public static String NEW_SERVER_DIALOG_INVALID_PORT_VALUE;
  public static String PORT_IN_USE;
  public static String PORT_OUT_OF_RANGE;

  public static String CREATE_APP_ENGINE_RUNTIME_WIZARD_DESCRIPTION;
  public static String CREATE_APP_ENGINE_RUNTIME_WIZARD_TITLE;
  public static String OPEN_CLOUD_SDK_PREFERENCE_BUTTON;
  public static String RUNTIME_WIZARD_CLOUD_SDK_NOT_FOUND;

  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  private Messages() {
  }
}
