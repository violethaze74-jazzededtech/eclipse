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

import com.example.TestRunnable;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

/**
 * Test of status-handling logic.
 */
public class GoogleFeedbackServerConnectionTest {

  //GoogleFeedbackServerConnection serverConnection;

  @Before
  public void setUp() {
//    ISystemSettings settings = mock(ISystemSettings.class);
//    when(settings.isConfigured()).thenReturn(true);
//    when(settings.getSendMode()).thenReturn(SendMode.NOTIFY);
//    serverConnection = new GoogleFeedbackServerConnection(mock(ISystemSettings.class));
//    serverConnection.reporter = mock(GoogleFeedbackErrorReporter.class);
  }

  @Test
  public void testInterestedWithCT4ETrace() {
//    IStatus status =
//        new Status(IStatus.ERROR, "com.google.cloud.tools.aeri", "Test", new Exception());
//    IEclipseContext context = EclipseContextFactory.create();
//    IProblemState problemState = serverConnection.interested(status, context, null);
//    assertEquals(ProblemStatus.NEEDINFO, problemState.getStatus());
  }

  @Test
  public void testNotInterestedWithPlainTrace() {
//    IStatus status =
//        new Status(IStatus.ERROR, "com.google.cloud.tools.aeri", "Test", getIsolatedException());
//    IEclipseContext context = EclipseContextFactory.create();
//    IProblemState problemState = serverConnection.interested(status, context, null);
//    assertEquals(ProblemStatus.IGNORED, problemState.getStatus());
  }

  @Test
  public void testTransform() {
//    IStatus status =
//        new Status(IStatus.ERROR, "com.google.cloud.tools.aeri", "Test", new Exception());
//    IEclipseContext context = EclipseContextFactory.create();
//    ISendOptions sendOptions = mock(ISendOptions.class);
//    when(sendOptions.getEnabledProcessors()).thenReturn(ECollections.emptyEList());
//    context.set(ISendOptions.class, sendOptions);
//
//    IReport report = serverConnection.transform(status, context);
//
//    assertNotNull(report);
//    assertEquals(Severity.UNKNOWN, report.getSeverity());
//    assertEquals(IStatus.ERROR, report.getStatus().getSeverity());
//    assertEquals(status.getPlugin(), report.getStatus().getPluginId());
//    assertEquals(status.getMessage(), report.getStatus().getMessage());
//    assertFalse(report.getAuxiliaryInformation().isEmpty());
  }

  @Test
  public void testSubmit() throws IOException {
//    IStatus status =
//        new Status(IStatus.ERROR, "com.google.cloud.tools.aeri", "Test", new Exception());
//    IEclipseContext context = EclipseContextFactory.create();
//    ISendOptions sendOptions = mock(ISendOptions.class);
//    when(sendOptions.getEnabledProcessors()).thenReturn(ECollections.emptyEList());
//    context.set(ISendOptions.class, sendOptions);
//
//    IProblemState problemState = serverConnection.submit(status, context, null);
//
//    assertEquals(ProblemStatus.NEW, problemState.getStatus());
//    verify(serverConnection.reporter, times(1)).sendFeedback(anyString(), anyString(),
//        Matchers.notNull(Exception.class), anyMap(), anyString(), anyString(), anyString());
  }

  /**
   * @return
   */
  private Throwable getIsolatedException() {
    TestRunnable target = new TestRunnable();
    Thread thread = new Thread(target);
    thread.start();
    do {
      Thread.currentThread().yield();
    } while (target.exception == null);
    return target.exception;
  }

}
