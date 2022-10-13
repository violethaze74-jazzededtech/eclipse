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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.epp.logging.aeri.core.IModelFactory;
import org.eclipse.epp.logging.aeri.core.IProblemState;
import org.eclipse.epp.logging.aeri.core.IReport;
import org.eclipse.epp.logging.aeri.core.IReportProcessor;
import org.eclipse.epp.logging.aeri.core.ISendOptions;
import org.eclipse.epp.logging.aeri.core.IServerConnection;
import org.eclipse.epp.logging.aeri.core.ISystemSettings;
import org.eclipse.epp.logging.aeri.core.ProblemStatus;
import org.eclipse.epp.logging.aeri.core.filters.RequiredPackagesFilter;
import org.eclipse.epp.logging.aeri.core.util.Reports;

/** A very simple connector to the Google Feedback tool. */
public class GoogleExceptionServerConnection implements IServerConnection {

  private static final List<Pattern> CLOUD_TOOLS_PACKAGES =
      Collections.unmodifiableList(Arrays.asList(
          Pattern.compile("com\\.google\\.cloud\\.tools\\..*")));

  /** Class patterns to preserve when anonymizing stack traces. */
  private static final List<Pattern> ACCEPTED_PACKAGES =
      Collections.unmodifiableList(Arrays.asList(
          Pattern.compile("java\\..*"), Pattern.compile("javax\\..*"),
          Pattern.compile("sun\\..*"), Pattern.compile("org\\.eclipse\\..*"),
          Pattern.compile("com\\.google\\..*")));

  private static final RequiredPackagesFilter cloudToolsPackagesFilter =
      new RequiredPackagesFilter(CLOUD_TOOLS_PACKAGES);

  private final ExceptionSender exceptionSender;

  @Inject
  public GoogleExceptionServerConnection(ISystemSettings settings) {
    this(new ExceptionSender());
  }

  @VisibleForTesting
  public GoogleExceptionServerConnection(ExceptionSender exceptionSender) {
    this.exceptionSender = exceptionSender;
  }

  @Override
  public IProblemState interested(IStatus status, IEclipseContext context,
      IProgressMonitor monitor) {
    IProblemState state = IModelFactory.eINSTANCE.createProblemState();
    if (cloudToolsPackagesFilter.apply(status)) {
      state.setStatus(ProblemStatus.NEEDINFO);
      state.setMessage(Messages.getString("ErrorNotificationMessage"));
    } else {
      state.setStatus(ProblemStatus.IGNORED); // not interested
    }
    return state;
  }

  @Override
  public IReport transform(IStatus status, IEclipseContext context) {
    // Unfortunately, AnonymizeStackTracesProcessor.CTX_ACCEPTED_PACKAGES_PATTERNS isn't public
    context.set("acceptedPackagesPatterns", ACCEPTED_PACKAGES);

    ISendOptions options = context.get(ISendOptions.class);
    IReport report = Reports.newReport(status);
    report.setSeverity(options.getSeverity());
    report.setComment(options.getComment());

    for (IReportProcessor processor : options.getEnabledProcessors()) {
      processor.process(report, status, context);
    }

    return report;
  }

  @Override
  public IProblemState submit(IStatus status, IEclipseContext context, IProgressMonitor monitor)
      throws IOException {
    IReport report = transform(status, context);

    exceptionSender.sendException(report.getStatus().getException(),
        report.getEclipseBuildId(), report.getJavaRuntimeVersion(),
        report.getOsgiOs(), report.getOsgiOsVersion(),
        report.getSeverity().toString(), report.getComment());

    IProblemState response = IModelFactory.eINSTANCE.createProblemState();
    response.setStatus(ProblemStatus.NEW);
    response.setMessage(Messages.getString("ReportSubmitted"));
    return response;
  }

  @Override
  public void discarded(IStatus status, IEclipseContext context) {
    // Users decided not to send it; nothing to do.
  }
}
