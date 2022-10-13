/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.eclipse.aeri;

import com.google.cloud.tools.eclipse.util.CloudToolsInfo;
import com.google.cloud.tools.eclipse.util.io.HttpUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionSender {

  private static final Logger logger = Logger.getLogger(ExceptionSender.class.getName());

  private static final String REPORT_PRODUCT_ID = "CT4E"; //$NON-NLS-1$
  private static final String REPORT_ENDPOINT = "https://clients2.google.com/cr/staging_report"; //$NON-NLS-1$

  // Be careful of PII.
  public static void sendException(String stackTrace, String extraComments) {
    sendException(REPORT_ENDPOINT, stackTrace, extraComments);
  }

  @VisibleForTesting
  static void sendException(String url, String stackTrace, String extraComments) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("product", REPORT_PRODUCT_ID); //$NON-NLS-1$
    parameters.put("version", CloudToolsInfo.getToolsVersion()); //$NON-NLS-1$
    parameters.put("exception_info", stackTrace); //$NON-NLS-1$
    if (!Strings.isNullOrEmpty(extraComments)) {
      parameters.put("comments", extraComments); //$NON-NLS-1$
    }

    try {
      // Internal system expects the product ID and version in URL too.
      String urlParameters = "?product=" + REPORT_PRODUCT_ID //$NON-NLS-1$
          + "&version=" + CloudToolsInfo.getToolsVersion(); //$NON-NLS-1$
      HttpUtil.sendPost(url + urlParameters, parameters);
    } catch (IOException ex) {
      logger.log(Level.WARNING, "Failed to send exception report.", ex); //$NON-NLS-1$
    }
  }
}
