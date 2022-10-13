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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.console.TextConsole;

/**
 * A property tester, intended for use with the {@code
 * org.eclipse.ui.console.consolePatternMatchListeners} extension point, to test whether a console
 * is associated with a {@link DevAppServerRuntimeProcess}.
 */
public class DevAppServerRuntimeProcessPropertyTester extends PropertyTester {

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    if (receiver instanceof TextConsole) {
      TextConsole console = (TextConsole) receiver;
      IProcess process = (IProcess) console.getAttribute(IDebugUIConstants.ATTR_CONSOLE_PROCESS);
      return process != null && process instanceof DevAppServerRuntimeProcess;
    }
    return false;
  }
}
