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

package com.google.cloud.tools.eclipse.test.util.ui;

import com.google.common.base.Predicate;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class CompositeUtil {

  @SuppressWarnings("unchecked")
  public static <T> T findControl(Composite composite, final Class<T> type) {
    return (T) findControl(composite, new Predicate<Control>() {
      @Override
      public boolean apply(Control control) {
        return type.isInstance(control);
      }
    });
  }

  public static Button findButton(Composite composite, final String text) {
    return (Button) findControl(composite, new Predicate<Control>() {
      @Override
      public boolean apply(Control control) {
        return control instanceof Button && ((Button) control).getText().equals(text);
      }
    });
  }

  public static Label findLabel(Composite composite, final String prefix) {
    return (Label) findControl(composite, new Predicate<Control>() {
      @Override
      public boolean apply(Control control) {
        return control instanceof Label && ((Label) control).getText().startsWith(prefix);
      }
    });
  }  
  
  public static Control findControl(Composite composite, Predicate<Control> predicate) {
    for (Control control : composite.getChildren()) {
      if (predicate.apply(control)) {
        return control;
      } else if (control instanceof Composite) {
        Control result = findControl((Composite) control, predicate);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  /**
   * Finds a {@link Control} of type {@code type} that comes after a {@link Label} with a text
   * {@code label}.
   */
  @SuppressWarnings("unchecked")
  public static <T> T findControlAfterLabel(Composite composite, Class<T> type, String label) {
    return (T) CompositeUtil.findControl(composite, new AfterLabel<>(type, label));
  }

  private static class AfterLabel<T> implements Predicate<Control> {

    private final String label;
    private final Class<T> type;
    private boolean labelSeen;

    private AfterLabel(Class<T> type, String label) {
      this.type = type;
      this.label = label;
    }

    @Override
    public boolean apply(Control control) {
      if (labelSeen) {
        return type.isInstance(control);
      } else {
        labelSeen = control instanceof Label && (((Label) control).getText()).equals(label);
        return false;
      }
    }
  }
}
