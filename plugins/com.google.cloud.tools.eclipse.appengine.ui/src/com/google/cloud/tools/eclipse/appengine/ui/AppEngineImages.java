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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Accessors for shared icons.
 */
public class AppEngineImages {
  public static final ImageDescriptor APPENGINE_IMAGE_DESCRIPTOR =
      getIcon("icons/obj16/appengine.png");

  public static final ImageDescriptor APPENGINE_GREY_IMAGE_DESCRIPTOR =
      getIcon("icons/obj16/grey/appengine.png");

  public static ImageDescriptor appEngine(int size) {
    String imageFilePath = "icons/gae-" + size + "x" + size + ".png";
    return getIcon(imageFilePath);
  }

  private static ImageDescriptor getIcon(String path) {
    return AbstractUIPlugin.imageDescriptorFromPlugin(
        "com.google.cloud.tools.eclipse.appengine.ui", path);
  }
}
