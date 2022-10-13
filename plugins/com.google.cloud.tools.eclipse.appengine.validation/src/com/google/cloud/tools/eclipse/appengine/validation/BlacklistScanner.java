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

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayDeque;
import java.util.Queue;
import org.xml.sax.Attributes;
import org.xml.sax.ext.Locator2;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Parses a given XML file and adds blacklisted elements to a {@link Queue}.
 * Wraps blacklisted element Queue and the file's character encoding.
 */
class BlacklistScanner extends DefaultHandler {
  
  private Locator2 locator;
  private Queue<BannedElement> blacklist;
  private String characterEncoding;
  private SaxParserResults results;
  
  @Override
  public void setDocumentLocator(Locator locator) {
    this.locator = (Locator2) locator;
  }
  
  /**
   * Ensures parser always starts with an empty queue.
   */
  @Override
  public void startDocument() throws SAXException {
    this.blacklist = new ArrayDeque<>();
    setCharacterEncoding(locator.getEncoding());
  }
  
  /**
   * Adds blacklisted start element to stack. Adding 2 to length accounts
   * for the start and end angle brackets of the tag.
   */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    if (AppEngineWebBlacklist.contains(qName)) {
      DocumentLocation start = new DocumentLocation(locator.getLineNumber(),
          locator.getColumnNumber() - qName.length() - 2);
      BannedElement element = new BannedElement(qName, start, qName.length() + 2);
      blacklist.add(element);
    }
  }
  
  @Override
  public void endDocument() throws SAXException {
    this.results = new SaxParserResults(blacklist, characterEncoding);
  }
  
  SaxParserResults getParserResults() {
    return results;
  }
  
  @Override
  public void error(SAXParseException ex) throws SAXException {
    //nests ex to conserve exception line number
    throw new SAXException(ex.getMessage(), ex);
  }
  
  @Override
  public void fatalError(SAXParseException ex) throws SAXException {
    throw new SAXException(ex.getMessage(), ex);
  }

  @Override
  public void warning(SAXParseException exception) throws SAXException { //do nothing
  }
  
  @VisibleForTesting
  public Queue<BannedElement> getBlacklist() {
    return blacklist;
  }
  
  void setCharacterEncoding(String encoding) {
    if (encoding != null) {
      this.characterEncoding = encoding;
    } else {
      this.characterEncoding = "UTF-8";
    }
  }
    
}
