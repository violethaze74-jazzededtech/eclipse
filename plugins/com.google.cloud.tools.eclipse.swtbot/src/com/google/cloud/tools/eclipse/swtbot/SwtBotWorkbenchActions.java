/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

package com.google.cloud.tools.eclipse.swtbot;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.hamcrest.Matcher;

import java.util.List;

/**
 * SWTBot utility methods that perform general workbench actions.
 */
public final class SwtBotWorkbenchActions {

  private static final int OPEN_PREFERENCES_DIALOG_DELAY_MS = 1000;

  /**
   * Opens the preferences dialog from the main Eclipse window.
   * <p>
   * Note: There are some platform-specific intricacies that this abstracts
   * away.
   */
  public static void openPreferencesDialog(final SWTWorkbenchBot bot) {
    SwtBotTestingUtilities.performAndWaitForWindowChange(bot, new Runnable() {
      @Override
      public void run() {
        if (SwtBotTestingUtilities.isMac()) {
          // TODO: Mac has "Preferences..." under the "Eclipse" menu item.
          // However,
          // the "Eclipse" menu item is a system menu item (like the Apple menu
          // item), and can't be reached via SWTBot.
          openPreferencesDialogViaEvents(bot);
        } else {
          SWTBotMenu windowMenu = bot.menu("Window");
          windowMenu.menu("Preferences").click();
        }
      }
    });
  }

  /**
   * Wait until all background tasks are complete.
   */
  public static void waitForIdle(SWTBot bot) {
    while (!Job.getJobManager().isIdle()) {
      bot.sleep(300);
    }
  }

  /**
   * Wait for the main shell progress bar to get removed.
   */
  public static void waitForMainShellProgressBarToFinish(final SWTWorkbenchBot bot) {
    // wait for progress bar to disappear
    bot.waitUntil(new ICondition() {
      @Override
      public boolean test() throws Exception {
        // First lower the amount of timeout, otherwise waiting for widget not to be found exception
        // is a long time
        SwtBotTimeoutManager.setTimeout(3000);
        try {
          // Find the progress bar in the main shell and wait for it to be removed
          @SuppressWarnings("unchecked")
          Matcher<ProgressBar> matcher =
              org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory
                  .allOf(org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType(ProgressBar.class));
          List<? extends ProgressBar> bars = bot.widgets(matcher);
          // keep polling until there are no progress bars found
          return bars == null || bars.isEmpty();
        } catch (WidgetNotFoundException ex) {
          return true;
        } finally {
          // Restore the original timeout
          SwtBotTimeoutManager.setTimeout();
        }

      }

      @Override
      public void init(SWTBot bot) {}

      @Override
      public String getFailureMessage() {
        return "waitForMainShellProgressBarToFinish() error.";
      }
    });
  }

  private static void openPreferencesDialogViaEvents(SWTBot bot) {
    Display display = bot.getDisplay();
    Event event = new Event();

    // Move to the "Apple" menu item (it catches 0, 0)
    event.type = SWT.MouseMove;
    event.x = 0;
    event.y = 0;
    display.post(event);

    bot.sleep(OPEN_PREFERENCES_DIALOG_DELAY_MS);

    // Click
    event.type = SWT.MouseDown;
    event.button = 1;
    display.post(event);
    bot.sleep(SwtBotTestingUtilities.EVENT_DOWN_UP_DELAY_MS);
    event.type = SWT.MouseUp;
    display.post(event);

    bot.sleep(OPEN_PREFERENCES_DIALOG_DELAY_MS);

    // Right to the "Eclipse" menu item
    SwtBotTestingUtilities.sendKeyDownAndUp(bot, SWT.ARROW_RIGHT, '\0');
    bot.sleep(OPEN_PREFERENCES_DIALOG_DELAY_MS);

    // Down two to the "Preferences..." menu item
    SwtBotTestingUtilities.sendKeyDownAndUp(bot, SWT.ARROW_DOWN, '\0');
    bot.sleep(OPEN_PREFERENCES_DIALOG_DELAY_MS);

    SwtBotTestingUtilities.sendKeyDownAndUp(bot, SWT.ARROW_DOWN, '\0');
    bot.sleep(OPEN_PREFERENCES_DIALOG_DELAY_MS);

    // Press enter
    SwtBotTestingUtilities.sendKeyDownAndUp(bot, 0, '\r');
    bot.sleep(OPEN_PREFERENCES_DIALOG_DELAY_MS);
  }

  /**
   * Close the Welcome/Intro view, if found. Usually required on the first launch.
   */
  public static void closeWelcome(SWTWorkbenchBot bot) {
    SWTBotView activeView = bot.activeView();
    if (activeView != null && activeView.getTitle().equals("Welcome")) {
      activeView.close();
    }
  }

  private SwtBotWorkbenchActions() {}
}
