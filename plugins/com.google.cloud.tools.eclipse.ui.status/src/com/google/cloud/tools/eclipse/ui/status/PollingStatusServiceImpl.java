/*
 * Copyright 2018 Google Inc.
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

package com.google.cloud.tools.eclipse.ui.status;

import com.google.cloud.tools.eclipse.ui.status.Incident.Severity;
import com.google.cloud.tools.eclipse.util.jobs.Consumer;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * An implementation that polls the Google Cloud Platform's status page. The Google Cloud Platform
 * status page provides a incident log in JSON, which appears to be ordered from most recent to
 * oldest. We fetch the first N bytes and process the incidents listed. Incidents that are still
 * on-going do not have an "end".
 */
@Component(name = "polling")
public class PollingStatusServiceImpl implements GcpStatusMonitoringService {
  private static final Logger logger = Logger.getLogger(PollingStatusServiceImpl.class.getName());

  private static final URI STATUS_JSON_URI =
      URI.create("https://status.cloud.google.com/incidents.json");

  private Job pollingJob =
      new Job("Retrieving Google Cloud Platform status") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
          refreshStatus();
          if (active) {
            schedule(pollTime);
          }
          return Status.OK_STATUS;
        }
      };

  private boolean active = false;
  private long pollTime = 3 * 60 * 1000; // poll every 3 minutes
  private IProxyService proxyService;
  private ListenerList /*<Consumer<GcpStatusMonitoringService>>*/ listeners = new ListenerList /*<>*/();
  private Gson gson = new Gson();

  private GcpStatus currentStatus = GcpStatus.OK_STATUS;

  @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
  public void setProxyService(IProxyService proxyService) {
    this.proxyService = proxyService;
  }

  public void unsetProxyService(IProxyService proxyService) {
    if (this.proxyService == proxyService) {
      this.proxyService = null;
    }
  }

  @Activate
  public void start() {
    active = true;
    pollingJob.schedule();
  }

  @Deactivate
  public void stop() {
    active = false;
    pollingJob.cancel();
  }

  @Override
  public GcpStatus getCurrentStatus() {
    return currentStatus;
  }

  void refreshStatus() {
    try {
      // As of 2018-01-30 the incidents log is 258k! But the incidents appear to be sorted from most
      // recent to the oldest.  Although fetching with gzip encoding reduces to 36k over the wire,
      // we can still do better by retrieving only the first 8k.
      URLConnection connection = STATUS_JSON_URI.toURL().openConnection(getProxy(STATUS_JSON_URI));
      connection.addRequestProperty("Range", "bytes=0-8192");
      try (InputStream input = connection.getInputStream()) {
        InputStreamReader streamReader = new InputStreamReader(input, StandardCharsets.UTF_8);
        Collection<Incident> active = extractIncidentsInProgress(gson, streamReader);
        if (active.isEmpty()) {
          currentStatus = GcpStatus.OK_STATUS;
        } else {
          Severity highestSeverity = Incident.getHighestSeverity(active);
          Collection<String> affectedServices = Incident.getAffectedServiceNames(active);
          currentStatus =
              new GcpStatus(highestSeverity, Joiner.on(", ").join(affectedServices), active);
        }
      }
    } catch (IOException ex) {
      currentStatus = new GcpStatus(Severity.ERROR, ex.toString(), null);
    }
    logger.info("current GCP status = " + currentStatus);
    for (Object listener : listeners.getListeners()) {
      ((Consumer<GcpStatusMonitoringService>) listener).accept(this);
    }
  }

  /**
   * Process and accumulate the incidents from the input stream. As the the input stream may be
   * incomplete (e.g., partial download), we ignore any JSON exceptions and {@link IOException}s
   * that may occur.
   */
  static Collection<Incident> extractIncidentsInProgress(Gson gson, Reader reader) {
    // Process the individual incident elements. An active incident has no {@code end} element.
    List<Incident> incidents = new LinkedList<Incident>();
    try {
      JsonReader jsonReader = new JsonReader(reader);
      jsonReader.beginArray();
      while (jsonReader.hasNext()) {
        Incident incident = gson.fromJson(jsonReader, Incident.class);
        if (incident.end == null) {
          incidents.add(incident);
        }
      }
    } catch (JsonParseException | IOException ex) {
      // ignore this since we don't request all of the data
    }
    return incidents;
  }

  private Proxy getProxy(URI uri) {
    if (proxyService == null) {
      return Proxy.NO_PROXY;
    }
    IProxyData[] proxies = proxyService.select(uri);
    for (IProxyData proxyData : proxies) {
      switch (proxyData.getType()) {
        case IProxyData.HTTPS_PROXY_TYPE:
        case IProxyData.HTTP_PROXY_TYPE:
          return new Proxy(
              Type.HTTP, new InetSocketAddress(proxyData.getHost(), proxyData.getPort()));
        case IProxyData.SOCKS_PROXY_TYPE:
          return new Proxy(
              Type.SOCKS, new InetSocketAddress(proxyData.getHost(), proxyData.getPort()));
        default:
          logger.warning("Unknown proxy-data type: " + proxyData.getType());
          break;
      }
    }
    return Proxy.NO_PROXY;
  }

  @Override
  public void addStatusChangeListener(Consumer<GcpStatusMonitoringService> listener) {
    listeners.add(listener);
  }

  @Override
  public void removeStatusChangeListener(Consumer<GcpStatusMonitoringService> listener) {
    listeners.remove(listener);
  }
}
