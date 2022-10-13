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

import java.io.IOException;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.xml.sax.SAXException;

/**
 * Source validator for web.xml.
 */
public class WebXmlSourceValidator extends AbstractXmlSourceValidator {

  private static final String MARKER_ID = 
      "com.google.cloud.tools.eclipse.appengine.validation.servletMarker";
  /**
   * Adds an {@link IMessage} to web.xml for every 
   * {@link BannedElement} found in the file.
   */
  @Override
  protected void validate(IReporter reporter, byte[] bytes)
      throws CoreException, IOException, ParserConfigurationException {
    try {
      SaxParserResults parserResults = WebXmlSaxParser.readXml(bytes);
      Map<BannedElement, Integer> bannedElementOffsetMap =
          ValidationUtils.getOffsetMap(bytes, parserResults);
      for (Map.Entry<BannedElement, Integer> entry : bannedElementOffsetMap.entrySet()) {
        this.createMessage(reporter, entry.getKey(), entry.getValue(),
              MARKER_ID, IMessage.HIGH_SEVERITY);
      }
    } catch (SAXException ex) {
      // Do nothing
      // Default Eclipse parser flags syntax errors
    }
  }
   

}
