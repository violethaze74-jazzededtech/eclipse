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

package com.google.cloud.tools.eclipse.ui.util.event;

import com.google.cloud.tools.eclipse.ui.util.Messages;
import com.google.cloud.tools.eclipse.util.status.StatusUtil;
import com.google.common.annotations.VisibleForTesting;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class OpenUriSelectionListener implements SelectionListener {

  private final BiConsumer<Exception, URI> errorHandler;
  private final IWorkbenchBrowserSupport browserSupport;
  private final Supplier<Map<String, String>> queryParameterProvider;

  public OpenUriSelectionListener(Supplier<Map<String, String>> queryParameterProvider,
      BiConsumer<Exception, URI> errorHandler) {
    this(queryParameterProvider, errorHandler, PlatformUI.getWorkbench().getBrowserSupport());
  }

  public OpenUriSelectionListener(BiConsumer<Exception, URI> errorHandler) {
    this(Collections::emptyMap, errorHandler, PlatformUI.getWorkbench().getBrowserSupport());
  }

  @VisibleForTesting
  OpenUriSelectionListener(Supplier<Map<String, String>> queryParameterProvider,
      BiConsumer<Exception, URI> errorHandler, IWorkbenchBrowserSupport browserSupport) {
    this.queryParameterProvider = queryParameterProvider;
    this.errorHandler = errorHandler;
    this.browserSupport = browserSupport;
  }

  @Override
  public void widgetSelected(SelectionEvent event) {
    openUri(event.text);
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent event) {
    openUri(event.text);
  }

  private void openUri(String uriString) {
    URI uri = null;
    try {
      uri = appendQueryParameters(new URI(uriString));
      browserSupport.getExternalBrowser().openURL(uri.toURL());
    } catch (PartInitException | MalformedURLException | URISyntaxException ex) {
      errorHandler.accept(ex, uri);
    }
  }

  private URI appendQueryParameters(URI uri) throws URISyntaxException {
    String queryString = uri.getQuery();
    if (queryString == null) {
      queryString = "";
    }
    StringBuilder query = new StringBuilder(queryString);
    for (Entry<String, String> parameter : queryParameterProvider.get().entrySet()) {
      if (query.length() > 0) {
        query.append('&');
      }
      query.append(parameter.getKey())
           .append('=')
           .append(parameter.getValue());
    }

    return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
                   uri.getPort(), uri.getPath(), query.toString(), uri.getFragment());
  }

  public static class ErrorDialogErrorHandler implements BiConsumer<Exception, URI> {

    private final Shell shell;

    public ErrorDialogErrorHandler(Shell shell) {
      this.shell = shell;
    }

    @Override
    public void accept(Exception ex, URI uri) {
      String title = Messages.getString("openurllistener.error.title");
      String message = Messages.getString("openurllistener.error.message");
      if (uri != null) {
        message += ": " + uri.toString();
      }
      ErrorDialog.openError(shell, title, message, StatusUtil.error(this, message, ex));
    }
  }
}
