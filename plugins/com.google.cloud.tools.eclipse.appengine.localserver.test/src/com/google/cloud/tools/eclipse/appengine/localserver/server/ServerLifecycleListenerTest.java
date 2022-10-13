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

import java.util.function.Consumer;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServerLifecycleListenerTest {

  private final ServerLifecycleListener fixture = new ServerLifecycleListener();
  @Mock IServer server;
  @Mock Consumer<ServerEvent> handler;

  @Test
  public void testInstall_noHandlers() {
    try {
      fixture.install(server);
      Assert.fail("should have errored");
    } catch (IllegalStateException ex) {
      // expected
    }
  }

  @Test
  public void testInstall_withHandler() {
    fixture.whenStarted(handler).install(server);

    Mockito.verify(server).addServerListener(Mockito.any(IServerListener.class));
    Mockito.verifyZeroInteractions(server, handler);
  }

  @Test
  public void testChaining() {
    Assert.assertSame(fixture, fixture.whenStarting(handler));
    Assert.assertSame(fixture, fixture.whenStarted(handler));
    Assert.assertSame(fixture, fixture.whenStopping(handler));
    Assert.assertSame(fixture, fixture.whenStopped(handler));
  }

  @Test
  public void testServerStarting() {
    fixture.whenStarting(handler).install(server);

    ServerEvent event = newServerStateChangeEvent(IServer.STATE_STARTING);
    fixture.serverChanged(event);

    Mockito.verify(handler).accept(event);
    Mockito.verify(server).addServerListener(Mockito.any(IServerListener.class));
    Mockito.verifyZeroInteractions(server);
  }

  @Test
  public void testServerStarted() {
    fixture.whenStarted(handler).install(server);

    ServerEvent event = newServerStateChangeEvent(IServer.STATE_STARTED);
    fixture.serverChanged(event);

    Mockito.verify(handler).accept(event);
    Mockito.verify(server).addServerListener(Mockito.any(IServerListener.class));
    Mockito.verifyZeroInteractions(server);
  }

  @Test
  public void testServerStopping() {
    fixture.whenStopping(handler).install(server);

    ServerEvent event = newServerStateChangeEvent(IServer.STATE_STOPPING);
    fixture.serverChanged(event);

    Mockito.verify(handler).accept(event);
    Mockito.verify(server).addServerListener(Mockito.any(IServerListener.class));
    Mockito.verifyZeroInteractions(server);
  }

  @Test
  public void testServerStopped() {
    fixture.whenStopped(handler).install(server);

    ServerEvent event = newServerStateChangeEvent(IServer.STATE_STOPPED);
    fixture.serverChanged(event);

    Mockito.verify(handler).accept(event);
    Mockito.verify(server).addServerListener(Mockito.any(IServerListener.class));
    Mockito.verify(server).removeServerListener(Mockito.any(IServerListener.class));
    Mockito.verifyZeroInteractions(server);
  }

  private ServerEvent newServerStateChangeEvent(int serverState) {
    return new ServerEvent(
        ServerEvent.STATE_CHANGE, server, serverState, IServer.PUBLISH_AUTO, false);
  }
}
