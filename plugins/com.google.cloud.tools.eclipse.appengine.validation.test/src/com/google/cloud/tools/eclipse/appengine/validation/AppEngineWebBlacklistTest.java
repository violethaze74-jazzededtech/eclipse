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

package com.google.cloud.tools.eclipse.appengine.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import org.junit.Test;

public class AppEngineWebBlacklistTest {
  
  @Test(expected = NullPointerException.class)
  public void testGetBlacklistElementMessage_nullArg() {
    AppEngineWebBlacklist.getBlacklistElementMessage(null);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testGetBlacklistElementMessage_elementNotInBlacklist() {
    AppEngineWebBlacklist.getBlacklistElementMessage("test");
  }
  
  @Test
  public void testGetBlacklistElementMessage() {
    assertEquals("Project ID should be specified at deploy time",
      AppEngineWebBlacklist.getBlacklistElementMessage("application"));
  }
  
  @Test
  public void testGetQuickAssistProcessor() {
    assertEquals(VersionQuickAssistProcessor.class.getName(),
        AppEngineWebBlacklist.getQuickFixProcessor("version").getClass().getName());
  }
  
  @Test
  public void testGetBlacklistElements() {
    ArrayList<String> elements = AppEngineWebBlacklist.getBlacklistElements();
    assertEquals(2, elements.size());
    assertTrue("application".equals(elements.get(0)) ^ "application".equals(elements.get(1)));
    assertTrue("version".equals(elements.get(0)) ^ "version".equals(elements.get(1)));
  }
}

