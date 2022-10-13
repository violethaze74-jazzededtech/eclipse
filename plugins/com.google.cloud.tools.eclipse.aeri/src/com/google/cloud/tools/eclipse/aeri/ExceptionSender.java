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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.epp.logging.aeri.core.IStackTraceElement;
import org.eclipse.epp.logging.aeri.core.IThrowable;

public class ExceptionSender {

  private static final Logger logger = Logger.getLogger(ExceptionSender.class.getName());

  private final String endpointUrl;

  ExceptionSender() {
    endpointUrl = "https://clients2.google.com/cr/staging_report";
  }

  ExceptionSender(String endpointUrl) {
    this.endpointUrl = endpointUrl;
  }

  void sendException(IThrowable exception, String eclipseBuildId, String javaVersion,
      String os, String osVersion, String userSeverity, String userComment) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("product", CloudToolsInfo.EXCEPTION_REPORT_PRODUCT_ID);
    parameters.put("version", CloudToolsInfo.getToolsVersion());
    parameters.put("exception_info", formatStacktrace(exception));

    String extraInfo = "eclipseBuildId: " + handleNullOrEmpty(eclipseBuildId) + "\n"
                     + "javaVersion: " + handleNullOrEmpty(javaVersion) + "\n"
                     + "os: " + os + " " + handleNullOrEmpty(osVersion) + "\n"
                     + "userSeverity: " + handleNullOrEmpty(userSeverity) + "\n"
                     + "userComment: " + handleNullOrEmpty(userComment);
    // "comments" seems to be the only viable place we can put extra info.
    parameters.put("comments", extraInfo);

    try {
      // Internal system expects the product ID and version in URL too.
      String urlParameters = "?product=" + CloudToolsInfo.EXCEPTION_REPORT_PRODUCT_ID
          + "&version=" + CloudToolsInfo.getToolsVersion();
      int code = HttpUtil.sendPostMultipart(endpointUrl + urlParameters, parameters);
      if (code != 200) {
        logger.log(Level.WARNING, "Failed to send exception report. HTTP response code: " + code);
      }
    } catch (IOException ex) {
      logger.log(Level.WARNING, "Failed to send exception report.", ex);
    }
  }

  private static String handleNullOrEmpty(String string) {
    if (string == null || string.trim().isEmpty()) {
      return "__NONE__";
    }
    return string;
  }

  /** Format the modeled stack trace. */
  @VisibleForTesting
  static String formatStacktrace(IThrowable exception) {
    if (exception == null) {
      return "__NONE__";
    }

    // We need this manual formatting because exception of type "Exception" isn't anonymized.
    StringBuilder trace =
        new StringBuilder(exception.getClassName() + ": " + exception.getMessage());
    for (IStackTraceElement frame : exception.getStackTrace()) {
      trace.append("\n\tat ").append(frame.getClassName())
          .append('.').append(frame.getMethodName());
      trace.append('(');
      if (frame.getFileName() != null) {
        trace.append(frame.getFileName());
        if (frame.getLineNumber() > 0) {
          trace.append(':').append(frame.getLineNumber());
        }
      }
      trace.append(')');
    }
    return trace.toString();
  }
}
