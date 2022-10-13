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

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.function.Consumer;
import java.util.logging.Logger;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

/**
 * Register some action to be performed during the lifecycle of a single server run (i.e., a
 * starting &rarr; started &rarr; stopping &rarr; stopped). {@link ServerBehaviourDelegate}
 * instances may live across start/stops and thus server listener instances must be removed on
 * server stop if they are to be single-use.
 */
public class ServerLifecycleListener {
  private static final Logger logger = Logger.getLogger(ServerLifecycleListener.class.getName());

  private final Multimap<Integer, Consumer<ServerEvent>> handlers = HashMultimap.create();
  private final IServerListener listener = this::serverChanged;
  private IServer server;

  /**
   * Request that the handler be called when the server is starting.
   *
   * @param handler called with the {@link ServerEvent}
   * @return this instance for chaining
   * @see IServer#STATE_STARTING
   */
  public ServerLifecycleListener whenStarting(Consumer<ServerEvent> handler) {
    handlers.put(IServer.STATE_STARTING, handler);
    return this;
  }

  /**
   * Request that the handler be called when the server is started.
   *
   * @param handler called with the {@link ServerEvent}
   * @return this instance for chaining
   * @see IServer#STATE_STARTED
   */
  public ServerLifecycleListener whenStarted(Consumer<ServerEvent> handler) {
    handlers.put(IServer.STATE_STARTED, handler);
    return this;
  }

  /**
   * Request that the handler be called when the server is stopping.
   *
   * @param handler called with the {@link ServerEvent}
   * @return this instance for chaining
   * @see IServer#STATE_STOPPING
   */
  public ServerLifecycleListener whenStopping(Consumer<ServerEvent> handler) {
    handlers.put(IServer.STATE_STOPPING, handler);
    return this;
  }

  /**
   * Request that the handler be called when the server is stopped.
   *
   * @param handler called with the {@link ServerEvent}
   * @return this instance for chaining
   * @see IServer#STATE_STOPPED
   */
  public ServerLifecycleListener whenStopped(Consumer<ServerEvent> handler) {
    handlers.put(IServer.STATE_STOPPED, handler);
    return this;
  }

  /** Configure the provided server to listen for server chasnge events. */
  public void install(IServer server) {
    Preconditions.checkState(!handlers.isEmpty(), "no state handlers configured");
    this.server = server;
    server.addServerListener(listener);
  }

  void serverChanged(ServerEvent event) {
    Preconditions.checkState(server == event.getServer());
    if (event.getState() == IServer.STATE_STOPPED) {
      server.removeServerListener(listener);
    }
    for (Consumer<ServerEvent> handler : handlers.get(event.getState())) {
      handler.accept(event);
    }
  }
  
}
