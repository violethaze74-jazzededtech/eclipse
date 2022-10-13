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

package com.google.cloud.tools.eclipse.googleapis.internal;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.appengine.v1.Appengine;
import com.google.api.services.appengine.v1.Appengine.Apps;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.CloudResourceManager.Projects;
import com.google.api.services.servicemanagement.ServiceManagement;
import com.google.api.services.storage.Storage;
import com.google.cloud.tools.eclipse.googleapis.IGoogleApiFactory;
import com.google.cloud.tools.eclipse.util.CloudToolsInfo;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.eclipse.core.net.proxy.IProxyChangeEvent;
import org.eclipse.core.net.proxy.IProxyChangeListener;
import org.eclipse.core.net.proxy.IProxyService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Class to obtain various Google Cloud Platform related APIs.
 */
@Component
public class GoogleApiFactory implements IGoogleApiFactory {

  private IProxyService proxyService;

  private final JsonFactory jsonFactory = Utils.getDefaultJsonFactory();
  private final ProxyFactory proxyFactory;
  private LoadingCache<GoogleApi, HttpTransport> transportCache;

  private final IProxyChangeListener proxyChangeListener = new IProxyChangeListener() {
    @Override
    public void proxyInfoChanged(IProxyChangeEvent event) {
      if (transportCache != null) {
        transportCache.invalidateAll();
      }
    }
  };

  public GoogleApiFactory() {
    this(new ProxyFactory());
  }

  @VisibleForTesting
  public GoogleApiFactory(ProxyFactory proxyFactory) {
    Preconditions.checkNotNull(proxyFactory, "proxyFactory is null");
    this.proxyFactory = proxyFactory;
  }

  @Activate
  public void init() {
    // NetHttpTransport advises: "For maximum efficiency, applications should use a single
    // globally-shared instance of the HTTP transport." But as we need a separate proxy per URL,
    // we cannot reuse the same httptransport.
    transportCache =
        CacheBuilder.newBuilder().weakValues().build(new TransportCacheLoader(proxyFactory));
  }

  @Override
  public Projects newProjectsApi(Credential credential) {
    Preconditions.checkNotNull(transportCache, "transportCache is null");
    HttpTransport transport = transportCache.getUnchecked(GoogleApi.CLOUDRESOURCE_MANAGER_API);
    Preconditions.checkNotNull(transport, "transport is null");
    Preconditions.checkNotNull(jsonFactory, "jsonFactory is null");

    CloudResourceManager resourceManager =
        new CloudResourceManager.Builder(transport, jsonFactory, credential)
            .setApplicationName(CloudToolsInfo.USER_AGENT).build();
    return resourceManager.projects();
  }

  @Override
  public Storage newStorageApi(Credential credential) {
    Preconditions.checkNotNull(transportCache, "transportCache is null");
    HttpTransport transport = transportCache.getUnchecked(GoogleApi.CLOUD_STORAGE_API);
    Preconditions.checkNotNull(transport, "transport is null");
    Preconditions.checkNotNull(jsonFactory, "jsonFactory is null");

    Storage.Builder builder = new Storage.Builder(transport, jsonFactory, credential)
        .setApplicationName(CloudToolsInfo.USER_AGENT);
    Storage storage = builder.build();
    return storage;
  }

  @Override
  public Apps newAppsApi(Credential credential) {
    Preconditions.checkNotNull(transportCache, "transportCache is null");
    HttpTransport transport = transportCache.getUnchecked(GoogleApi.APPENGINE_ADMIN_API);
    Preconditions.checkNotNull(transport, "transport is null");
    Preconditions.checkNotNull(jsonFactory, "jsonFactory is null");

    Appengine appengine =
        new Appengine.Builder(transport, jsonFactory, credential)
            .setApplicationName(CloudToolsInfo.USER_AGENT).build();
    return appengine.apps();
  }


  @Override
  public ServiceManagement newServiceManagementApi(Credential credential) {
    Preconditions.checkNotNull(transportCache, "transportCache is null");
    HttpTransport transport = transportCache.getUnchecked(GoogleApi.SERVICE_MANAGEMENT_API);
    Preconditions.checkNotNull(transport, "transport is null");
    Preconditions.checkNotNull(jsonFactory, "jsonFactory is null");

    ServiceManagement serviceManagement =
        new ServiceManagement.Builder(transport, jsonFactory, credential)
            .setApplicationName(CloudToolsInfo.USER_AGENT).build();
    return serviceManagement;
  }

  @Reference(policy=ReferencePolicy.DYNAMIC, cardinality=ReferenceCardinality.OPTIONAL)
  public void setProxyService(IProxyService proxyService) {
    this.proxyService = proxyService;
    this.proxyService.addProxyChangeListener(proxyChangeListener);
    proxyFactory.setProxyService(this.proxyService);
    if (transportCache != null) {
      transportCache.invalidateAll();
    }
  }

  public void unsetProxyService(IProxyService proxyService) {
    if (this.proxyService == proxyService) {
      proxyService.removeProxyChangeListener(proxyChangeListener);
      this.proxyService = null;
      proxyFactory.setProxyService(null);
      if (transportCache != null) {
        transportCache.invalidateAll();
      }
    }
  }

  @VisibleForTesting
  void setTransportCache(LoadingCache<GoogleApi, HttpTransport> transportCache) {
    this.transportCache = transportCache;
  }
}
