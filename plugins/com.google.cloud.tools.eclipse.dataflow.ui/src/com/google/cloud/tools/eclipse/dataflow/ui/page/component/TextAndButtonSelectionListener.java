/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.eclipse.dataflow.ui.page.component;

import static com.google.common.base.Preconditions.checkState;

import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

/**
 * A selection listener for use in {@link TextAndButtonComponent}.
 */
public abstract class TextAndButtonSelectionListener implements SelectionListener {
  private Text text = null;
  private Button button = null;

  protected final void setTextValue(String value) {
    text.setText(value);
  }

  void init(Text text, Button button) {
    checkState(this.text == null && this.button == null,
        "Cannot call init on a %s that is already initialized.", getClass().getSimpleName());
    this.text = text;
    this.button = button;
  }
}

