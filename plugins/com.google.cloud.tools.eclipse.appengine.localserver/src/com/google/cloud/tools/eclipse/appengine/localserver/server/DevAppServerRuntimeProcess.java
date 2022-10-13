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

import com.google.cloud.tools.appengine.cloudsdk.process.ProcessOutputLineListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;

/** A wrapper around the Cloud SDK devappserver process. */
public class DevAppServerRuntimeProcess extends PlatformObject implements IProcess {

  private static class CloudSdkStreamMonitor implements IStreamMonitor, ProcessOutputLineListener {
    private ListenerList<IStreamListener> listeners = new ListenerList<>();
    private StringBuffer contents = new StringBuffer();

    @Override
    public void onOutputLine(String line) {
      String newContent = line + "\n";
      contents.append(newContent);

      for (IStreamListener listener : listeners) {
        listener.streamAppended(newContent, this);
      }
    }

    @Override
    public void addListener(IStreamListener listener) {
      listeners.add(listener);
    }

    @Override
    public String getContents() {
      return contents.toString();
    }

    @Override
    public void removeListener(IStreamListener listener) {
      listeners.remove(listener);
    }
  }
  
  public class CloudSdkStreamsProxy implements IStreamsProxy {
    private final CloudSdkStreamMonitor standardOutputMonitor = new CloudSdkStreamMonitor();
    private final CloudSdkStreamMonitor standardErrorMonitor = new CloudSdkStreamMonitor();

    @Override
    public IStreamMonitor getErrorStreamMonitor() {
      return standardErrorMonitor;
    }

    @Override
    public IStreamMonitor getOutputStreamMonitor() {
      return standardOutputMonitor;
    }

    @Override
    public void write(String input) throws IOException {
      throw new IOException("no input stream");
    }
  }

  private final ILaunch launch;
  private final String name;
  private final Map<String, String> attributes;
  private Process process;
  private CloudSdkStreamsProxy streamsProxy = new CloudSdkStreamsProxy();

  public DevAppServerRuntimeProcess(ILaunch launch, String name, Map<String, String> attributes) {
    this.launch = launch;
    this.name = name;
    this.attributes = new HashMap<>(attributes);
  }

  public void setProcess(Process process) {
    this.process = process;
  }

  @Override
  public void terminate() throws DebugException {
    try {
      sendQuitRequest();
    } catch (CoreException e) {
      throw new DebugException(e.getStatus());
    }
  }

  private void sendQuitRequest() throws CoreException {
    final IServer server = ServerUtil.getServer(getLaunch().getLaunchConfiguration());
    if (server == null) {
      return;
    }
    server.stop(true);
    try {
      // the stop command is async, let's give it some time to execute
      Thread.sleep(2000L);
    } catch (InterruptedException e) {
    }
  }

  @Override
  public boolean canTerminate() {
    return true;
  }

  @Override
  public boolean isTerminated() {
    return !process.isAlive();
  }

  @Override
  public String getLabel() {
    return name;
  }

  @Override
  public ILaunch getLaunch() {
    return launch;
  }

  @Override
  public IStreamsProxy getStreamsProxy() {
    return streamsProxy;
  }

  @Override
  public void setAttribute(String key, String value) {
    attributes.put(key, value);
  }

  @Override
  public String getAttribute(String key) {
    return attributes.get(key);
  }

  @Override
  public int getExitValue() throws DebugException {
    return process.exitValue();
  }

  public ProcessOutputLineListener getStandardOutputLineListener() {
    return streamsProxy.standardOutputMonitor;
  }

  public ProcessOutputLineListener getStandardErrorLineListener() {
    return streamsProxy.standardErrorMonitor;
  }
}
