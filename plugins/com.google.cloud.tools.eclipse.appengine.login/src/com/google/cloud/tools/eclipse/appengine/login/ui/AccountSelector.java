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

package com.google.cloud.tools.eclipse.appengine.login.ui;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.Strings;
import com.google.cloud.tools.eclipse.appengine.login.IGoogleLoginService;
import com.google.cloud.tools.ide.login.Account;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class AccountSelector extends Composite {

  private IGoogleLoginService loginService;
  private String loginMessage;
  private Account selectedAccount;
  private ListenerList selectionListeners = new ListenerList();

  /**
   * If true and if there is no selected account and there is exactly one logged-in account, then
   * select that account.
   */
  private boolean selectDefaultSingleAccount = true;

  @VisibleForTesting Combo combo;
  @VisibleForTesting LogInOnSelect logInOnSelect = new LogInOnSelect();

  public AccountSelector(Composite parent, IGoogleLoginService loginService, String loginMessage) {
    this(parent, loginService, loginMessage, false);
  }

  public AccountSelector(Composite parent, IGoogleLoginService loginService,
      String loginMessage, boolean selectDefaultSingleAccount) {
    super(parent, SWT.NONE);
    this.loginService = loginService;
    this.loginMessage = loginMessage;
    this.selectDefaultSingleAccount = selectDefaultSingleAccount;
    GridLayoutFactory.fillDefaults().generateLayout(this);

    combo = new Combo(this, SWT.READ_ONLY);
    GridDataFactory.fillDefaults().grab(true, false).applyTo(combo);

    List<Account> sortedAccounts = new ArrayList<>(loginService.getAccounts());
    Collections.sort(sortedAccounts, new Comparator<Account>() {
      @Override
      public int compare(Account o1, Account o2) {
        return o1.getEmail().compareTo(o2.getEmail());
      }
    });
    for (Account account : sortedAccounts) {
      combo.add(account.getEmail());
      combo.setData(account.getEmail(), account);
    }
    combo.add(loginMessage);
    combo.addSelectionListener(logInOnSelect);
    selectAccount(null);
  }

  /**
   * @return true if this selector lists an account with {@code email}. For convenience of
   *     callers, {@code email} may be {@code null} or empty, which always returns false
   */
  public boolean isEmailAvailable(String email) {
    return !Strings.isNullOrEmpty(email) && combo.indexOf(email) != -1;
  }

  /**
   * Returns a {@link Credential} object associated with the account, if selected. Otherwise,
   * {@code null}.
   *
   * Note that, if an account is selected, the returned {@link Credential} cannot be {@code null}.
   * (By its contract, {@link Account} never carries a {@code null} {@link Credential}.)
   */
  public Credential getSelectedCredential() {
    return selectedAccount != null ? selectedAccount.getOAuth2Credential() : null;
  }

  /**
   * Returns the currently selected email, or empty string if none; never {@code null}.
   */
  public String getSelectedEmail() {
    return combo.getText();
  }

  /**
   * Selects an account corresponding to the given {@code email} and returns its index of the combo
   * item. If there is no account corresponding to the {@code email}, <b>and</b> there is exactly 1
   * known account and {@link #selectDefaultSingleAccount} is true, then we automatically select
   * that single account; otherwise this method does returns -1 while retaining current selection
   * (if any).
   *
   * @param email email address to use to select an account
   * @return index of the newly selected combo item; -1 if {@code email} is {@code null} or the
   *         empty string, or if there is no matching account
   */
  public int selectAccount(String email) {
    int index = Strings.isNullOrEmpty(email) ? -1 : combo.indexOf(email);
    if (index < 0 && selectDefaultSingleAccount && getAccountCount() == 1) {
      index = 0;
      email = combo.getItem(0);
    }
    if (index != -1) {
      combo.select(index);
      selectedAccount = (Account) combo.getData(email);
      fireSelectionListeners();
    }
    return index;
  }

  public boolean isSignedIn() {
    return getAccountCount() > 0;
  }

  public int getAccountCount() {
    return combo.getItemCount() - 1;  // <Add a new account...> is always in the combo
  }

  private void fireSelectionListeners() {
    for (Object o : selectionListeners.getListeners()) {
      ((Runnable) o).run();
    }
  }

  public void addSelectionListener(Runnable listener) {
    selectionListeners.add(listener);
  }

  public void removeSelectionListener(Runnable listener) {
    selectionListeners.remove(listener);
  }

  @VisibleForTesting
  class LogInOnSelect extends SelectionAdapter {
    @Override
    public void widgetSelected(SelectionEvent event) {
      if (combo.getText().equals(loginMessage)) {
        Account account = loginService.logIn(null /* no custom dialog message */);
        if (account != null) {
          // account is selected and saved
          addAndSelectAccount(account);
        } else {
          // login failed, so restore to previous combo state
          int index = selectedAccount != null ? combo.indexOf(selectedAccount.getEmail()) : -1;
          if (index != -1) {
            combo.select(index);
          } else {
            combo.deselectAll();
          }
        }
        return;
      }
      selectedAccount = (Account) combo.getData(getSelectedEmail());
      fireSelectionListeners();
    }

    private void addAndSelectAccount(Account account) {
      // If the combo already has the email, just select it.
      int index = combo.indexOf(account.getEmail());
      if (index < 0) {
        combo.add(account.getEmail(), 0 /* place at top */);
        combo.setData(account.getEmail(), account);
      }
      selectAccount(account.getEmail());
    }
  }

  @Override
  public void setToolTipText(String string) {
    combo.setToolTipText(string);
  }
}
