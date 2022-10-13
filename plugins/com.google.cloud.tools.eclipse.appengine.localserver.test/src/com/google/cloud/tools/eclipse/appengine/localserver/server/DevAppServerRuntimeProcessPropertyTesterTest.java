/*
 * Copyright 2018 Google LLC
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

package com.google.cloud.tools.eclipse.appengine.localserver.server;

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.console.IOConsole;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/** Tests for {@link DevAppServerRuntimeProcessPropertyTester}. */
public class DevAppServerRuntimeProcessPropertyTesterTest {

  @Test
  public void testCreatable() {
    new DevAppServerRuntimeProcessPropertyTester();
  }

  @Test
  public void testWithDevAppServerRuntimeProcess() {
    DevAppServerRuntimeProcessPropertyTester tester =
        new DevAppServerRuntimeProcessPropertyTester();
    IOConsole console = Mockito.mock(IOConsole.class);
    DevAppServerRuntimeProcess process = Mockito.mock(DevAppServerRuntimeProcess.class);
    Mockito.doReturn(process).when(console).getAttribute(IDebugUIConstants.ATTR_CONSOLE_PROCESS);

    Assert.assertTrue(
        tester.test(console, "devAppServerRuntimeProcess", new Object[0] /* args */, null));
  }

  @Test
  public void testWithOtherProcess() {
    DevAppServerRuntimeProcessPropertyTester tester =
        new DevAppServerRuntimeProcessPropertyTester();
    IOConsole console = Mockito.mock(IOConsole.class);
    IProcess process = Mockito.mock(IProcess.class);
    Mockito.doReturn(process).when(console).getAttribute(IDebugUIConstants.ATTR_CONSOLE_PROCESS);

    Assert.assertFalse(
        tester.test(console, "devAppServerRuntimeProcess", new Object[0] /* args */, null));
  }

  @Test
  public void testWithOtherObject() {
    DevAppServerRuntimeProcessPropertyTester tester =
        new DevAppServerRuntimeProcessPropertyTester();

    Assert.assertFalse(
        tester.test(new Object(), "devAppServerRuntimeProcess", new Object[0] /* args */, null));
  }
}
