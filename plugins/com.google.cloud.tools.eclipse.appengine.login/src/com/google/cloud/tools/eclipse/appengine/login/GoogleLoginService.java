/*******************************************************************************
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/

package com.google.cloud.tools.eclipse.appengine.login;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.cloud.tools.eclipse.appengine.login.ui.LoginServiceUi;
import com.google.cloud.tools.ide.login.GoogleLoginState;
import com.google.cloud.tools.ide.login.JavaPreferenceOAuthDataStore;
import com.google.cloud.tools.ide.login.LoggerFacade;
import com.google.cloud.tools.ide.login.OAuthDataStore;
import com.google.common.annotations.VisibleForTesting;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides service related to login, e.g., account management, getting a credential of a
 * currently active user, etc.
 */
public class GoogleLoginService implements IGoogleLoginService {

  private static final String OAUTH_DATA_STORE_PREFERENCE_PATH =
      "/com/google/cloud/tools/eclipse/login";

  // For the detailed info about each scope, see
  // https://github.com/GoogleCloudPlatform/gcloud-eclipse-tools/wiki/Cloud-Tools-for-Eclipse-Technical-Design#oauth-20-scopes-requested
  private static final SortedSet<String> OAUTH_SCOPES = Collections.unmodifiableSortedSet(
      new TreeSet<>(Arrays.asList(
          "email", //$NON-NLS-1$
          "https://www.googleapis.com/auth/cloud-platform" //$NON-NLS-1$
      )));

  /**
   * Returns a URL through which users can login.
   *
   * @param redirectUrl URL to which the login result is directed. For example, a local web
   *     server listening on the URL can receive an authorization code from it.
   */
  public static String getGoogleLoginUrl(String redirectUrl) {
    return new GoogleAuthorizationCodeRequestUrl(Constants.getOAuthClientId(), redirectUrl,
        GoogleLoginService.OAUTH_SCOPES).toString();
  }

  private GoogleLoginState loginState;

  private LoginServiceUi loginServiceUi;

  /**
   * Called by OSGi Declarative Services Runtime when the {@link GoogleLoginService} is activated
   * as an OSGi service.
   */
  protected void activate() {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    LoginServiceLogger logger = new LoginServiceLogger();
    IShellProvider shellProvider = new IShellProvider() {
      @Override
      public Shell getShell() {
        return workbench.getDisplay().getActiveShell();
      }
    };

    loginServiceUi = new LoginServiceUi(workbench, shellProvider, workbench.getDisplay());
    loginState = new GoogleLoginState(
        Constants.getOAuthClientId(), Constants.getOAuthClientSecret(), OAUTH_SCOPES,
        new JavaPreferenceOAuthDataStore(OAUTH_DATA_STORE_PREFERENCE_PATH, logger),
        loginServiceUi, logger);
  }

  /**
   * 0-arg constructor is necessary for OSGi Declarative Services. Initialization will be done
   * by {@link activate()}.
   */
  public GoogleLoginService() {}

  @VisibleForTesting
  GoogleLoginService(
      OAuthDataStore dataStore, LoginServiceUi uiFacade, LoggerFacade loggerFacade) {
    loginServiceUi = uiFacade;
    loginState = new GoogleLoginState(
        Constants.getOAuthClientId(), Constants.getOAuthClientSecret(), OAUTH_SCOPES,
        dataStore, uiFacade, loggerFacade);
  }

  @Override
  public Credential getActiveCredential(String dialogMessage) {
    // TODO: holding a lock for a long period of time (especially when waiting for UI events)
    // should be avoided. Make the login library thread-safe, and don't lock during UI events.
    // (https://github.com/GoogleCloudPlatform/ide-login/issues/21)
    synchronized (loginState) {
      if (loginState.logInWithLocalServer(dialogMessage)) {
        return loginState.getCredential();
      }
      return null;
    }
  }

  @Override
  public Credential getCachedActiveCredential() {
    synchronized (loginState) {
      if (loginState.isLoggedIn()) {
        return loginState.getCredential();
      }
      return null;
    }
  }

  @Override
  public void clearCredential() {
    synchronized (loginState) {
      loginState.logOut(false /* Don't prompt for logout. */);
    }
  }

  private static final Logger logger = Logger.getLogger(GoogleLoginService.class.getName());

  private static class LoginServiceLogger implements LoggerFacade {

    @Override
    public void logError(String message, Throwable thrown) {
      logger.log(Level.SEVERE, message, thrown);
    }

    @Override
    public void logWarning(String message) {
      logger.log(Level.WARNING, message);
    }
  };
}
