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

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.epp.logging.aeri.core.IModelFactory;
import org.eclipse.epp.logging.aeri.core.IProblemState;
import org.eclipse.epp.logging.aeri.core.IReport;
import org.eclipse.epp.logging.aeri.core.IReportProcessor;
import org.eclipse.epp.logging.aeri.core.ISendOptions;
import org.eclipse.epp.logging.aeri.core.IServerConnection;
import org.eclipse.epp.logging.aeri.core.IStackTraceElement;
import org.eclipse.epp.logging.aeri.core.ISystemSettings;
import org.eclipse.epp.logging.aeri.core.IThrowable;
import org.eclipse.epp.logging.aeri.core.ProblemStatus;
import org.eclipse.epp.logging.aeri.core.SendMode;
import org.eclipse.epp.logging.aeri.core.filters.RequiredPackagesFilter;
import org.eclipse.epp.logging.aeri.core.util.Reports;

/** A very simple connector to the Google Feedback tool. */
public class GoogleCrashServerConnection implements IServerConnection {
  @VisibleForTesting
  static final String CT4E_PRODUCT_ID = "CT4E";

  @VisibleForTesting
  static final String NONE_STRING = "__NONE___";
  @VisibleForTesting
  static final String ERROR_MESSAGE_KEY = "error.message";
  @VisibleForTesting
  static final String ERROR_STACKTRACE_KEY = "error.stacktrace";
  @VisibleForTesting
  static final String ERROR_DESCRIPTION_KEY = "error.description";
  @VisibleForTesting
  static final String LAST_ACTION_KEY = "last.action";
  @VisibleForTesting
  static final String OS_NAME_KEY = "os.name";
  @VisibleForTesting
  static final String JAVA_VERSION_KEY = "java.version";
  @VisibleForTesting
  static final String JAVA_VM_VENDOR_KEY = "java.vm.vendor";
  @VisibleForTesting
  static final String APP_NAME_KEY = "app.name";
  @VisibleForTesting
  static final String APP_CODE_KEY = "app.code";
  @VisibleForTesting
  static final String APP_NAME_VERSION_KEY = "app.name.version";
  @VisibleForTesting
  static final String APP_EAP_KEY = "app.eap";
  @VisibleForTesting
  static final String APP_INTERNAL_KEY = "app.internal";
  @VisibleForTesting
  static final String APP_VERSION_MAJOR_KEY = "app.version.major";
  @VisibleForTesting
  static final String APP_VERSION_MINOR_KEY = "app.version.minor";
  @VisibleForTesting
  static final String PLUGIN_VERSION = "plugin.version";

  /** Prefix of our CT4E feature. */
  private static final String CT4E_FEATURE_ID_PREFIX = "com.google.cloud.tools.eclipse.suite";

  private ISystemSettings settings;

  @VisibleForTesting
  GoogleCrashStackTraceReporter reporter;

  // FIXME: statusFilters should be Predicate<? super IStatus> but
  // leads to a Guava mismatch as AERI requires guava [15,16)
  private RequiredPackagesFilter statusFilters;
  private List<Pattern> requiredPatterns;

  /** Class patterns to preserve when anonymizing stack traces. */
  private List<Pattern> acceptedPatterns;

  @Inject
  public GoogleCrashServerConnection(ISystemSettings settings) {
    this.settings = settings;
    this.reporter = new GoogleCrashStackTraceReporter();

    requiredPatterns = Arrays.asList(Pattern.compile("com\\.google\\.cloud\\.tools\\..*"));
    statusFilters = new RequiredPackagesFilter(requiredPatterns);

    acceptedPatterns = Arrays.asList(Pattern.compile("java\\..*"), Pattern.compile("javax\\..*"),
        Pattern.compile("sun\\..*"), Pattern.compile("org\\.eclipse\\..*"),
        Pattern.compile("com\\.google\\..*"));
  }

  private boolean neverSend() {
    return settings.isConfigured() && settings.getSendMode() == SendMode.NEVER;
  }

  @Override
  public IProblemState interested(IStatus status, IEclipseContext context,
      IProgressMonitor monitor) {
    IProblemState state = IModelFactory.eINSTANCE.createProblemState();
    // Could check if we've already reported this incident
    // Should we only accept IStatus.ERROR?
    if (!neverSend() && statusFilters.apply(status)) {
      state.setStatus(ProblemStatus.NEEDINFO);
      state.setMessage(
          "An error occurred involving Google Cloud Tools for Eclipse. "
              + "Please send it to Google for further examination. "
              + "If possible, please specify the severity and steps to reproduce.");
    } else {
      state.setStatus(ProblemStatus.IGNORED); // not interested
    }
    return state;
  }

  @Override
  public IReport transform(IStatus status, IEclipseContext context) {
    // Must use the IReportProcessors to allow users to toggle hiding
    // Grr: AnonymizeStackTracesProcessor.CTX_ACCEPTED_PACKAGES_PATTERNS isn't public
    context.set("acceptedPackagesPatterns", acceptedPatterns);

    ISendOptions options = context.get(ISendOptions.class);
    IReport report = Reports.newReport(status);
    report.setComment(options.getComment());
    report.setAnonymousId(options.getReporterId());
    report.setName(options.getReporterName());
    report.setEmail(options.getReporterEmail());
    report.setSeverity(options.getSeverity());

    buildKeyValuesMap(report);

    // Anonymization is performed by one of the many report processors, which are controlled by the
    // reporting UI. We can force anonymization of the data here.
    // if (options.isAnonymizeMessages()) {
    // Reports.anonymizeMessages(report);
    // }
    // if (options.isAnonymizeStackTraces()) {
    // Reports.anonymizeStackTraces(report, acceptedPatterns);
    // }
    for (IReportProcessor processor : options.getEnabledProcessors()) {
      processor.process(report, status, context);
    }

    return report;
  }

  @Override
  public IProblemState submit(IStatus status, IEclipseContext context, IProgressMonitor monitor)
      throws IOException {
    IReport report = transform(status, context); // seems odd that we re-transform it

    String errorMessage = status.getMessage();
    String errorDescription = report.getComment();
    Throwable exception = status.getException(); // this is not anonymized
    Map<String, String> params = report.getAuxiliaryInformation().map();
    String applicationVersion = nullToNone(getFeatureVersion());

    IProblemState response = IModelFactory.eINSTANCE.createProblemState();
    String reportId = reporter.sendFeedback(exception, report.getComment());
    // Result is a number, e.g., 1484376400916
    response.setMessage(MessageFormat.format("Thank you: submitted as report {0}.", reportId));
    response.setStatus(ProblemStatus.NEW);
    // can add a set of links to the response, shown in the UI
    // Links.addLink(response, Links.REL_SUBMISSION, resultAsUrl, "Submission");
    return response;
  }

  /** Return the version of the first com.google.cloud.tools.eclipse.suite* feature found. */
  private String getFeatureVersion() {
    IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
    if (providers != null) {
      for (IBundleGroupProvider provider : providers) {
        IBundleGroup[] bundleGroups = provider.getBundleGroups();
        for (IBundleGroup group : bundleGroups) {
          if (group.getIdentifier().startsWith(CT4E_FEATURE_ID_PREFIX)) {
            return group.getVersion();
          }
        }
      }
    }
    return null;
  }

  static void buildKeyValuesMap(IReport report) {
    EMap<String, String> map = report.getAuxiliaryInformation();
    // required parameters
    map.put(ERROR_MESSAGE_KEY, nullToNone(report.getStatus().getMessage()));
    map.put(ERROR_STACKTRACE_KEY, formatStacktrace(report.getStatus().getException()));
    // end of required parameters
    map.put(ERROR_DESCRIPTION_KEY, nullToNone(report.getComment()));
    // .put(LAST_ACTION_KEY, nullToNone(error.getLastAction()))
    map.put(OS_NAME_KEY, System.getProperty(OS_NAME_KEY, NONE_STRING));
    map.put(JAVA_VERSION_KEY, System.getProperty(JAVA_VERSION_KEY, NONE_STRING));
    map.put(JAVA_VM_VENDOR_KEY, System.getProperty(JAVA_VM_VENDOR_KEY, NONE_STRING));
    map.put(APP_NAME_KEY, nullToNone(report.getEclipseProduct()));
    map.put(APP_NAME_VERSION_KEY, nullToNone(report.getEclipseBuildId()));
    // .put(APP_VERSION_MAJOR_KEY, intelliJAppExtendedInfo.getMajorVersion())
    // .put(APP_VERSION_MINOR_KEY, intelliJAppExtendedInfo.getMinorVersion())
    // .put(APP_CODE_KEY, intelliJAppExtendedInfo.getPackageCode())
    // .put(APP_EAP_KEY, Boolean.toString(intelliJAppExtendedInfo.isEAP()))
    // .put(APP_INTERNAL_KEY, Boolean.toString(application.isInternal()))
    // .put(PLUGIN_VERSION, error.getPluginVersion())
  }

  /** Format the modelled stack trace. */
  @VisibleForTesting
  static String formatStacktrace(IThrowable exception) {
    if (exception == null) {
      return NONE_STRING;
    }
    StringBuilder trace =
        new StringBuilder(exception.getClassName() + ": " + exception.getMessage());
    for (IStackTraceElement frame : exception.getStackTrace()) {
      trace.append("\n  at ").append(frame.getClassName()).append('.').append(frame.getMethodName())
          .append('(');
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

  @Override
  public void discarded(IStatus status, IEclipseContext context) {
    // we don't record any additional detail
  }

  static String nullToNone(/* @Nullable */ String possiblyNullString) {
    return possiblyNullString == null ? NONE_STRING : possiblyNullString;
  }
}
