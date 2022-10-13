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

package com.google.cloud.tools.eclipse.appengine.localserver.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.devserver.DefaultRunConfiguration;
import java.net.InetAddress;
import java.util.function.BiPredicate;
import org.eclipse.core.runtime.CoreException;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocalAppEngineServerBehaviourTest {
  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock private BiPredicate<InetAddress, Integer> alwaysTrue;
  @Mock private BiPredicate<InetAddress, Integer> alwaysFalse;
  @Mock private BiPredicate<InetAddress, Integer> portProber;

  private final LocalAppEngineServerBehaviour serverBehavior = new LocalAppEngineServerBehaviour();

  @Before
  public void setUp() {
    when(alwaysTrue.test(any(InetAddress.class), anyInt())).thenReturn(true);
    when(alwaysFalse.test(any(InetAddress.class), anyInt())).thenReturn(false);
  }

  @Test
  public void testCheckPort_port0() throws CoreException {
    // port 0 should never be checked if in use
    assertEquals(0, LocalAppEngineServerBehaviour.checkPort(null, 0, portProber));
    verify(portProber, never()).test(any(InetAddress.class), any(Integer.class));
  }

  @Test
  public void testCheckPort_portNotInUse() throws CoreException {
    assertEquals(1, LocalAppEngineServerBehaviour.checkPort(null, 1, alwaysFalse));
    verify(alwaysFalse).test(any(InetAddress.class), any(Integer.class));
  }

  @Test
  public void testCheckPort_portInUse() {
    try {
      LocalAppEngineServerBehaviour.checkPort(null, 1, alwaysTrue);
      fail("Should throw CoreException");
    } catch (CoreException ex) {
      assertEquals("Port 1 is in use.", ex.getMessage());
      verify(alwaysTrue).test(any(InetAddress.class), any(Integer.class));
    }
  }

  @Test
  public void testCheckPort_portOutOfBounds_negative() {
    try {
      LocalAppEngineServerBehaviour.checkPort(null, -1, portProber);
      fail("Should throw CoreException");
    } catch (CoreException ex) {
      assertEquals("Port must be between 0 and 65535.", ex.getMessage());
      verify(portProber, never()).test(any(InetAddress.class), any(Integer.class));
    }
  }

  @Test
  public void testCheckPort_portOutOfBounds_positive() {
    try {
      LocalAppEngineServerBehaviour.checkPort(null, 65536, portProber);
      fail("Should throw CoreException");
    } catch (CoreException ex) {
      assertEquals("Port must be between 0 and 65535.", ex.getMessage());
      verify(portProber, never()).test(any(InetAddress.class), any(Integer.class));
    }
  }

  private static final String[] devappserver1Output = new String[] {
      "Apr 05, 2017 9:25:17 PM com.google.apphosting.utils.jetty.JettyLogger info",
      "INFO: jetty-6.1.x",
      "Apr 05, 2017 9:25:17 PM com.google.apphosting.utils.jetty.JettyLogger info",
      "INFO: Started SelectChannelConnector@localhost:7979",
      "Apr 05, 2017 9:25:17 PM com.google.appengine.tools.development.AbstractModule startup",
      "INFO: Module instance default is running at http://localhost:7979/",
      "Apr 05, 2017 9:25:17 PM com.google.appengine.tools.development.AbstractModule startup",
      "INFO: The admin console is running at http://localhost:7979/_ah/admin",
      "Apr 05, 2017 5:25:17 PM com.google.appengine.tools.development.DevAppServerImpl doStart",
  };
  
  private static final String[] devappserver2OutputWithDefaultModule1 = new String[] {
      "WARNING  2016-11-03 21:11:21,930 devappserver2.py:785] DEFAULT_VERSION_HOSTNAME will not be set correctly with --port=0",
      "INFO     2016-11-03 21:11:21,956 api_server.py:205] Starting API server at: http://localhost:52892",
      "INFO     2016-11-03 21:11:21,959 dispatcher.py:197] Starting module \"default\" running at: http://localhost:55948",
      "INFO     2016-11-03 21:11:21,959 dispatcher.py:197] Starting module \"second\" running at: http://localhost:8081",
      "INFO     2016-11-03 21:11:21,959 admin_server.py:116] Starting admin server at: http://localhost:43679",
      "Nov 03, 2016 9:11:23 PM com.google.appengine.tools.development.SystemPropertiesManager setSystemProperties"
  };

  private static final String[] devappserver2OutputWithDefaultModule2 = new String[] {
      "WARNING  2016-11-03 21:11:21,930 devappserver2.py:785] DEFAULT_VERSION_HOSTNAME will not be set correctly with --port=0",
      "INFO     2016-11-03 21:11:21,956 api_server.py:205] Starting API server at: http://localhost:52892",
      "INFO     2016-11-03 21:11:21,959 dispatcher.py:197] Starting module \"first\" running at: http://localhost:55948",
      "INFO     2016-11-03 21:11:21,959 dispatcher.py:197] Starting module \"default\" running at: http://localhost:8081",
      "INFO     2016-11-03 21:11:21,959 admin_server.py:116] Starting admin server at: http://localhost:43679",
      "Nov 03, 2016 9:11:23 PM com.google.appengine.tools.development.SystemPropertiesManager setSystemProperties"
  };

  private static final String[] serverOutputWithNoDefaultModule = new String[] {
      "WARNING  2016-11-03 21:11:21,930 devappserver2.py:785] DEFAULT_VERSION_HOSTNAME will not be set correctly with --port=0",
      "INFO     2016-11-03 21:11:21,956 api_server.py:205] Starting API server at: http://localhost:52892",
      "INFO     2016-11-03 21:11:21,959 dispatcher.py:197] Starting module \"first\" running at: http://localhost:8181",
      "INFO     2016-11-03 21:11:21,959 dispatcher.py:197] Starting module \"second\" running at: http://localhost:8182",
      "INFO     2016-11-03 21:11:21,959 dispatcher.py:197] Starting module \"third\" running at: http://localhost:8183",
      "INFO     2016-11-03 21:11:21,959 admin_server.py:116] Starting admin server at: http://localhost:43679",
      "Nov 03, 2016 9:11:23 PM com.google.appengine.tools.development.SystemPropertiesManager setSystemProperties"
  };
  
  @Test
  public void testExtractServerPortFromOutput_devappserver1() {
    setUpServerPort(0);
    simulateOutputParsing(devappserver1Output);
    assertEquals(7979, serverBehavior.getServerPort());
    assertEquals(7979, serverBehavior.getAdminPort());
  }

  @Test
  public void testExtractServerPortFromOutput_firstModuleIsDefault() {
    setUpServerPort(0);
    simulateOutputParsing(devappserver2OutputWithDefaultModule1);
    assertEquals(55948, serverBehavior.getServerPort());
  }

  @Test
  public void testExtractServerPortFromOutput_secondModuleIsDefault() {
    setUpServerPort(0);
    simulateOutputParsing(devappserver2OutputWithDefaultModule2);
    assertEquals(8081, serverBehavior.getServerPort());
  }

  @Test
  public void testExtractServerPortFromOutput_noDefaultModule() {
    setUpServerPort(0);
    simulateOutputParsing(serverOutputWithNoDefaultModule);
    assertEquals(8181, serverBehavior.getServerPort());
  }

  @Test
  public void testExtractServerPortFromOutput_defaultModuleDoesNotOverrideUserSpecifiedPort() {
    setUpServerPort(12345);
    simulateOutputParsing(devappserver2OutputWithDefaultModule1);
    assertEquals(12345, serverBehavior.getServerPort());
  }

  @Test
  public void testExtractModuleUrlFromOutput_firstModuleIsDefault() {
    simulateOutputParsing(devappserver2OutputWithDefaultModule1);
    assertEquals("http://localhost:55948", serverBehavior.getServiceUrl("default"));
    assertEquals("http://localhost:8081", serverBehavior.getServiceUrl("second"));
  }

  @Test
  public void testExtractModuleUrlFromOutput_noDefaultModule() {
    simulateOutputParsing(serverOutputWithNoDefaultModule);
    assertNull(serverBehavior.getServiceUrl("default"));
    assertEquals("http://localhost:8181", serverBehavior.getServiceUrl("first"));
    assertEquals("http://localhost:8182", serverBehavior.getServiceUrl("second"));
    assertEquals("http://localhost:8183", serverBehavior.getServiceUrl("third"));
  }

  @Test
  public void testExtractAdminPortFromOutput() {
    setUpServerPort(9080);
    setUpAdminPort(0);
    simulateOutputParsing(devappserver2OutputWithDefaultModule1);
    assertEquals(43679, serverBehavior.adminPort);
  }

  private void setUpServerPort(int port) {
    serverBehavior.serverPort = port;
  }

  private void setUpAdminPort(int port) {
    serverBehavior.adminPort = port;
  }

  private void simulateOutputParsing(String[] output) {
    LocalAppEngineServerBehaviour.DevAppServerOutputListener outputListener =
        serverBehavior.new DevAppServerOutputListener();
    for (String line : output) {
      outputListener.onOutputLine(line);
    }
  }

  @Test
  public void testStartDevServer_ignoresAdminPortWhenDevAppserver1() throws CoreException {
    Assume.assumeFalse(LocalAppEngineServerLaunchConfigurationDelegate.DEV_APPSERVER2);

    DefaultRunConfiguration runConfig = new DefaultRunConfiguration();
    runConfig.setAdminPort(8000);
    when(portProber.test(any(InetAddress.class), anyInt())).thenReturn(false);
    when(portProber.test(any(InetAddress.class), eq(8000))).thenReturn(true);

    serverBehavior.checkPorts(runConfig, portProber);
    assertEquals(-1, serverBehavior.adminPort);
    verify(portProber, never()).test(any(InetAddress.class), eq(8000));
  }

}
