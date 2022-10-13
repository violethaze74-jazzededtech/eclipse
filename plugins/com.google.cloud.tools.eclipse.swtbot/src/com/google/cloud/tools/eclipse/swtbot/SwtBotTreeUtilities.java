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

package com.google.cloud.tools.eclipse.swtbot;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.results.WidgetResult;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

/**
 * Utilities for manipulating trees.
 */
public class SwtBotTreeUtilities {

  /**
   * Given a tree that contains an entry with <code>itemName</code> and a direct child with a name
   * matching <code>subchildName</code>, return its tree item.
   *
   * This method is useful when there is the possibility of a tree having two similarly-named
   * top-level nodes.
   *
   * @param mainTree the tree
   * @param itemName the name of a top-level node in the tree
   * @param subchildName the name of a direct child of the top-level node (used to uniquely select
   *        the appropriate tree item for the given top-level node name)
   * @return the tree item corresponding to the top-level node with <code>itemName</code>that has a
   *         direct child with <code>subchildName</code>. If there are multiple tree items that
   *         satisfy this criteria, then the first one (in the UI) will be returned
   *
   * @throws WidgetNotFoundException if no such node can be found
   */
  public static SWTBotTreeItem getUniqueTreeItem(final SWTBot bot, final SWTBotTree mainTree,
      String itemName, String subchildName) {
    for (SWTBotTreeItem item : mainTree.getAllItems()) {
      if (itemName.equals(item.getText())) {
        try {
          item.expand();
          waitUntilTreeHasText(bot, item);
          if (item.getNode(subchildName) != null) {
            return item;
          }
        } catch (WidgetNotFoundException ex) {
          // Ignore
        }
      }
    }

    throw new WidgetNotFoundException(
        "The " + itemName + " node with a child of " + subchildName + " must exist in the tree.");
  }

  /**
   * Given a tree that contains an entry with one of <code>itemNames</code> and a direct child with
   * a name matching <code>subchildName</code>, return its tree item.
   * <p>
   * This method is useful when the top-level names are ambiguous and/or variable.
   *
   * @param mainTree the tree
   * @param itemNames possible names of a top-level node in the tree
   * @param subchildName the name of a direct child of the top-level node (used to uniquely select
   *        the appropriate tree item for the given top-level node name)
   * @return the tree item corresponding to the top-level node with one of <code>itemNames</code>
   *         that has a direct child with <code>subchildName</code>. If there are multiple tree
   *         items that satisfy this criteria, then the first one (in the UI) will be returned
   *
   * @throws WidgetNotFoundException if no such node can be found
   */
  public static SWTBotTreeItem getUniqueTreeItem(final SWTBot bot, final SWTBotTree mainTree,
      String[] itemNames, String subchildName) {

    for (String itemName : itemNames) {
      try {
        return getUniqueTreeItem(bot, mainTree, itemName, subchildName);
      } catch (WidgetNotFoundException ex) {
        // Ignore
      }
    }

    throw new WidgetNotFoundException("One of the " + itemNames + " nodes with a child of "
        + subchildName + " must exist in the tree.");
  }

  private static class TreeCollapsedCondition extends DefaultCondition {
    private final TreeItem tree;

    private TreeCollapsedCondition(TreeItem tree) {
      this.tree = tree;
    }

    @Override
    public String getFailureMessage() {
      return "Could not collapse the tree of " + tree.getText();
    }

    @Override
    public boolean test() throws Exception {
      return !isTreeExpanded(tree);
    }
  }

  private static class TreeExpandedCondition extends DefaultCondition {
    private final TreeItem tree;

    private TreeExpandedCondition(TreeItem tree) {
      this.tree = tree;
    }

    @Override
    public String getFailureMessage() {
      return "Could not expand the tree of " + tree.getText();
    }

    @Override
    public boolean test() throws Exception {
      return isTreeExpanded(tree);
    }
  }

  /**
   * Set the selection in the tree to the item with the given label text.
   * 
   * @return the current node
   */
  public static SWTBotTreeItem selectTreeItem(SWTBot bot, SWTBotTreeItem tree, String itemText) {
    waitUntilTreeItemHasItem(bot, tree, itemText);
    SWTBotTreeItem item = tree.select(itemText);
    return item;
  }

  /**
   * Blocks the caller until all of the direct children of the tree have text. The assumption is
   * that the tree does not have any "empty" children.
   * 
   * @param tree the tree to search
   * @throws TimeoutException if any of the direct children of the tree do not have text within the
   *         timeout period
   */
  public static void waitUntilTreeHasText(SWTBot bot, final SWTBotTreeItem tree)
      throws TimeoutException {
    // TODO: Refactor some of this logic; it follows the same general pattern as
    // {@link #waitUntilTreeItemHasItem(SWTBot, SWTBotTreeItem, String)}.

    // Attempt #1
    if (!waitUntilTreeHasTextImpl(bot, tree.widget)) {
      // Attempt #2: Something went wrong, try to cautiously reopen it.
      bot.sleep(1000);

      // There isn't a method to collapse, so double-click instead
      tree.doubleClick();
      bot.waitUntil(new TreeCollapsedCondition(tree.widget));

      bot.sleep(1000);

      tree.expand();
      bot.waitUntil(new TreeExpandedCondition(tree.widget));

      if (!waitUntilTreeHasTextImpl(bot, tree.widget)) {
        printTree(tree.widget);
        throw new TimeoutException(
            "Timed out waiting for text of the tree's children, giving up...");
      }
    }
  }

  private static boolean treeItemHasText(final TreeItem widget) {
    return UIThreadRunnable.syncExec(new Result<Boolean>() {
      @Override
      public Boolean run() {
        TreeItem[] items = widget.getItems();
        for (TreeItem item : items) {
          if (item.getText() == null || item.getText().isEmpty()) {
            return false;
          }
        }
        return true;
      }
    });
  }

  /**
   * Helper method to check whether a given tree is expanded which can be called from any thread.
   */
  private static boolean isTreeExpanded(final TreeItem tree) {
    return UIThreadRunnable.syncExec(new Result<Boolean>() {
      @Override
      public Boolean run() {
        return tree.getExpanded();
      }
    });
  }

  private static void printTree(final TreeItem tree) {
    UIThreadRunnable.syncExec(new VoidResult() {
      @Override
      public void run() {
        System.err.println(String.format("%s has %d items:", tree.getText(), tree.getItemCount()));
        for (TreeItem item : tree.getItems()) {
          System.err.println(String.format("  - %s", item.getText()));
        }
      }
    });
  }

  private static boolean waitUntilTreeHasItemImpl(SWTBot bot, final TreeItem tree,
      final String nodeText) {
    try {
      bot.waitUntil(new DefaultCondition() {
        @Override
        public String getFailureMessage() {
          return "Could not find node with text " + nodeText;
        }

        @Override
        public boolean test() throws Exception {
          return getTreeItem(tree, nodeText) != null;
        }
      });
    } catch (TimeoutException ex) {
      return false;
    }

    return true;
  }

  private static boolean waitUntilTreeHasTextImpl(SWTBot bot, final TreeItem tree) {
    try {
      bot.waitUntil(new DefaultCondition() {
        @Override
        public String getFailureMessage() {
          return "Not all of the nodes in the tree have text.";
        }

        @Override
        public boolean test() throws Exception {
          return treeItemHasText(tree);
        }
      });
    } catch (TimeoutException ex) {
      return false;
    }

    return true;
  }

  /**
   * Blocks the caller until the tree item has the given item text.
   * 
   * @param tree the tree item to search
   * @param nodeText the item text to look for
   * @throws org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException if the item could not
   *         be found within the timeout period
   */
  private static void waitUntilTreeItemHasItem(SWTBot bot, final SWTBotTreeItem tree,
      final String nodeText) {

    // Attempt #1
    if (!waitUntilTreeHasItemImpl(bot, tree.widget, nodeText)) {
      // Attempt #2: Something went wrong, try to cautiously reopen it.
      bot.sleep(1000);

      // There isn't a method to collapse, so double-click instead
      tree.doubleClick();
      bot.waitUntil(new TreeCollapsedCondition(tree.widget));

      bot.sleep(1000);

      tree.expand();
      bot.waitUntil(new TreeExpandedCondition(tree.widget));

      if (!waitUntilTreeHasItemImpl(bot, tree.widget, nodeText)) {
        printTree(tree.widget);
        throw new TimeoutException(
            String.format("Timed out waiting for %s, giving up...", nodeText));
      }
    }
  }

  /**
   * Gets the item matching the given name from a tree item.
   * 
   * @param widget the tree item to search
   * @param nodeText the text on the node
   * @return the child tree item with the specified text
   */
  public static TreeItem getTreeItem(final TreeItem widget, final String nodeText) {
    return UIThreadRunnable.syncExec(new WidgetResult<TreeItem>() {
      @Override
      public TreeItem run() {
        TreeItem[] items = widget.getItems();
        for (TreeItem item : items) {
          if (item.getText().equals(nodeText)) {
            return item;
          }
        }
        return null;
      }
    });
  }

  /**
   * Wait until the given tree has items, and return the first item.
   * 
   * @throws TimeoutException if no items appear within the default timeout
   */
  public static SWTBotTreeItem waitUntilTreeHasItems(SWTWorkbenchBot bot, final SWTBotTree tree) {
    bot.waitUntil(new DefaultCondition() {
      @Override
      public String getFailureMessage() {
        return "Tree items never appeared";
      }

      @Override
      public boolean test() throws Exception {
        return tree.hasItems();
      }
    });
    return tree.getAllItems()[0];
  }

  /**
   * Wait until the tree item contains the given text
   */
  public static void waitUntilTreeContainsText(SWTWorkbenchBot bot, final SWTBotTreeItem treeItem,
      final String text) {
    bot.waitUntil(new DefaultCondition() {
      @Override
      public boolean test() throws Exception {
        return treeItem.getText().contains(text);
      }

      @Override
      public String getFailureMessage() {
        return "Text never appeared";
      }
    });
  }
}
