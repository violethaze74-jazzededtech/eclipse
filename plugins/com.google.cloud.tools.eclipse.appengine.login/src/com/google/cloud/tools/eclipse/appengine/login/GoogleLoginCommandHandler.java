package com.google.cloud.tools.eclipse.appengine.login;

import com.google.api.client.auth.oauth2.Credential;
import com.google.cloud.tools.eclipse.ui.util.ServiceUtils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import java.util.Map;

public class GoogleLoginCommandHandler extends AbstractHandler implements IElementUpdater {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    IGoogleLoginService loginService = ServiceUtils.getService(event, IGoogleLoginService.class);

    Credential credential = loginService.getCachedActiveCredential();  // See if already logged in.
    if (credential == null) {
      loginService.getActiveCredential(null /* no custom dialog message */);  // Log in.
    } else {
      if (MessageDialog.openConfirm(HandlerUtil.getActiveShell(event),
          Messages.LOGOUT_CONFIRM_DIALOG_TITILE, Messages.LOGOUT_CONFIRM_DIALOG_MESSAGE)) {
        loginService.clearCredential();  // Log out on confirmation.
      }
    }

    return null;
  }

  @Override
  public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
    IGoogleLoginService loginService =
        element.getServiceLocator().getService(IGoogleLoginService.class);
    boolean loggedIn = loginService.getCachedActiveCredential() != null;

    element.setText(
        loggedIn ? Messages.LOGIN_MENU_LOGGED_IN : Messages.LOGIN_MENU_LOGGED_OUT);
    element.setTooltip(
        loggedIn ? Messages.LOGIN_TOOLTIP_LOGGED_IN : Messages.LOGIN_TOOLTIP_LOGGED_OUT);
  }
}
