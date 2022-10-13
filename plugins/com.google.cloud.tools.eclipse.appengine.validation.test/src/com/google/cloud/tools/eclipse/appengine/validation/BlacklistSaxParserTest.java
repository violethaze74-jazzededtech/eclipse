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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class BlacklistSaxParserTest {
  
  @Test
  public void testReadXml_emptyXml()
      throws ParserConfigurationException, IOException, SAXException {
    String emptyXml = "";
    byte[] bytes = emptyXml.getBytes(StandardCharsets.UTF_8);
    assert(BlacklistSaxParser.readXml(bytes).getBlacklist().isEmpty());
  }
  
  @Test
  public void testReadXml_xmlWithBannedElement()
      throws ParserConfigurationException, IOException, SAXException {
    String xml = "<application></application>";
    byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
    Queue<BannedElement> blacklist = BlacklistSaxParser.readXml(bytes).getBlacklist();
    String message = "Project ID should be specified at deploy time";
    assertEquals(blacklist.poll().getMessage(), message);
  }

}
