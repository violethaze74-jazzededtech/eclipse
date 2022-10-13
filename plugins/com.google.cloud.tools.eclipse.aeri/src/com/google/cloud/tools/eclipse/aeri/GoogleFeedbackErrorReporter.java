/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.eclipse.aeri;

import com.android.tools.idea.diagnostics.error.AnonymousFeedback;
import com.android.tools.idea.diagnostics.error.AnonymousFeedback.HttpConnectionFactory;
import java.io.IOException;
import java.util.Map;

/**
 * Mockable wrapper
 */
public class GoogleFeedbackErrorReporter {
  private static final AnonymousFeedback.HttpConnectionFactory connectionFactory =
      new HttpConnectionFactory();

  /** Returns the feedback report token. */
  public String sendFeedback(String productName, String packageName, Throwable exception,
      Map<String, String> params, String errorMessage, String errorDescription, String version)
      throws IOException {
    if (true) {
      return AnonymousFeedback.sendFeedback(productName, packageName, connectionFactory, exception,
          params, errorMessage, errorDescription, version);
    } else {
      System.err.println("Submitting problem report: " + params);
      return "NOT ACTUALLY SUBMITTED";
    }
  }

}
