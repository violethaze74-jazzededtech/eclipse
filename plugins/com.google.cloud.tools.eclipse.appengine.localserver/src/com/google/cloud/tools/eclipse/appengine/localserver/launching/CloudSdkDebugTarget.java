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

package com.google.cloud.tools.eclipse.appengine.localserver.launching;

import com.google.cloud.tools.eclipse.appengine.localserver.server.LocalAppEngineServerBehaviour;
import com.google.cloud.tools.eclipse.appengine.localserver.server.ServerLifecycleListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.wst.server.core.IServer;

/**
 * A debug element that represents the running state. The debug target provides a bridge between the
 * launch and the running server: terminated the debug target results in stopping the server.
 *
 * <ul>
 *   <li>On launch-termination, stop the server.
 *   <li>On server-stop, terminate the launch
 * </ul>
 */
public class CloudSdkDebugTarget extends DebugElement implements IDebugTarget {
  private static final Logger logger = Logger.getLogger(CloudSdkDebugTarget.class.getName());

  private final ILaunch launch;
  private final LocalAppEngineServerBehaviour serverBehaviour;
  private final IServer server;
  private IProcess process;

  /** Listen for termination of our launch so as to ensure our server is stopped. */
  private ILaunchesListener2 launchesListener =
      new ILaunchesListener2() {
        @Override
        public void launchesTerminated(ILaunch[] launches) {
          for (ILaunch terminated : launches) {
            if (terminated == launch) {
              if (server.getServerState() == IServer.STATE_STARTED) {
                logger.fine("Launch terminated; stopping server"); // $NON-NLS-1$
                server.stop(false);
              }
              return;
            }
          }
        }

        @Override
        public void launchesAdded(ILaunch[] launches) {}

        @Override
        public void launchesChanged(ILaunch[] launches) {}

        @Override
        public void launchesRemoved(ILaunch[] launches) {
          for (ILaunch removed : launches) {
            if (removed == launch) {
              getLaunchManager().removeLaunchListener(launchesListener);
            }
          }
        }
      };

  public CloudSdkDebugTarget(ILaunch launch, LocalAppEngineServerBehaviour serverBehaviour) {
    super(null);
    this.launch = launch;
    this.serverBehaviour = serverBehaviour;
    server = serverBehaviour.getServer();
  }

  /** Add required listeners */
  public void engage() {
    getLaunchManager().addLaunchListener(launchesListener);
    // Fire a {@link DebugEvent#TERMINATED} event when the server is stopped
    new ServerLifecycleListener()
        .whenStarting(event -> fireChangeEvent(DebugEvent.STATE))
        .whenStarted(event -> fireChangeEvent(DebugEvent.STATE))
        .whenStopping(event -> fireChangeEvent(DebugEvent.STATE))
        .whenStopped(
            event -> {
              fireTerminateEvent();
              try {
                logger.fine("Server stopped; terminating launch"); // $NON-NLS-1$
                launch.terminate();
              } catch (DebugException ex) {
                logger.log(Level.WARNING, "Unable to terminate launch", ex); // $NON-NLS-1$
              }
            })
        .install(server);
  }

  @Override
  public String getName() throws DebugException {
    return serverBehaviour.getDescription();
  }

  /**
   * Returns an identifier that maps to our {@link
   * com.google.cloud.tools.eclipse.appengine.localserver.ui.CloudSdkDebugTargetPresentation
   * presentation} via the {@code org.eclipse.debug.ui.debugModelPresentations} extension point.
   */
  @Override
  public String getModelIdentifier() {
    return "com.google.cloud.tools.eclipse.appengine.localserver.cloudSdkDebugTarget"; //$NON-NLS-1$
  }

  @Override
  public IDebugTarget getDebugTarget() {
    return this;
  }

  @Override
  public ILaunch getLaunch() {
    return launch;
  }

  @Override
  public boolean canTerminate() {
    return true;
  }

  @Override
  public boolean isTerminated() {
    int state = server.getServerState();
    return state == IServer.STATE_STOPPED;
  }

  @Override
  public void terminate() throws DebugException {
    int state = server.getServerState();
    if (state != IServer.STATE_STOPPED) {
      serverBehaviour.stop(state == IServer.STATE_STOPPING);
    }
  }

  @Override
  public boolean supportsBreakpoint(IBreakpoint breakpoint) {
    return false;
  }

  @Override
  public void breakpointAdded(IBreakpoint breakpoint) {}

  @Override
  public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {}

  @Override
  public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {}

  @Override
  public boolean canResume() {
    return false;
  }

  @Override
  public void resume() throws DebugException {}

  @Override
  public boolean canSuspend() {
    return false;
  }

  @Override
  public boolean isSuspended() {
    return false;
  }

  @Override
  public void suspend() throws DebugException {}

  @Override
  public boolean canDisconnect() {
    return false;
  }

  @Override
  public boolean isDisconnected() {
    return false;
  }

  @Override
  public void disconnect() throws DebugException {}

  @Override
  public boolean supportsStorageRetrieval() {
    return false;
  }

  @Override
  public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
    return null;
  }

  @Override
  public IProcess getProcess() {
    return process;
  }

  public void setProcess(IProcess serverProcess) {
    this.process = serverProcess;
    fireChangeEvent(DebugEvent.CONTENT);
  }

  @Override
  public boolean hasThreads() throws DebugException {
    return false;
  }

  @Override
  public IThread[] getThreads() throws DebugException {
    return new IThread[0];
  }
  
  private ILaunchManager getLaunchManager() {
    return DebugPlugin.getDefault().getLaunchManager();
  }
}
