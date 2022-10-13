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

import org.xml.sax.helpers.DefaultHandler;

import com.google.common.annotations.VisibleForTesting;

import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.Locator2;

/**
 * Builds a DOM tree that maintains element line and column numbers.
 */
class PositionalXmlHandler extends DefaultHandler {
  
    private Document document;
    private StringBuilder textBuffer = new StringBuilder();
    private Locator2 locator;
    private final Stack<Element> elementStack = new Stack<>();

    @Override
    public void startDocument() {
      try {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();  
      } catch (ParserConfigurationException ex) {
        throw new RuntimeException("Cannot create document", ex);
      }
    }
    
    @Override
    public void setDocumentLocator(Locator locator) {
      this.locator = (Locator2) locator;
    }
      
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException {               
      addText();
      Element element = document.createElementNS(uri, qName);
      for (int i = 0; i < attributes.getLength(); i++) {
        element.setUserData(attributes.getQName(i), attributes.getValue(i), null);
      }
      DocumentLocation location = new DocumentLocation(
          locator.getLineNumber(), locator.getColumnNumber());
      element.setUserData("location", location, null);
      elementStack.push(element);
    }
      
    @Override
    public void endElement(String uri, String localName, String qName){
      addText();
      Element closedElement = elementStack.pop();
      if (elementStack.isEmpty()) { // If this is the root element
        closedElement.setUserData("encoding", locator.getEncoding(), null);
        document.appendChild(closedElement);
      } else {
        Element parentElement = elementStack.peek();
        parentElement.appendChild(closedElement);                   
      }
    }
      
    @Override
    public void characters (char ch[], int start, int length) throws SAXException {
      textBuffer.append(ch, start, length);
    }
    
    /**
     *  Returns the text inside the current tag
     */
    @VisibleForTesting
    void addText() {
      if (textBuffer.length() > 0) {
        Element element = elementStack.peek();
        Node textNode = document.createTextNode(textBuffer.toString());
        element.appendChild(textNode);
        textBuffer = new StringBuilder();
      }
    }
    
    Document getDocument() {
      return document;
    }
    
    @VisibleForTesting
    Stack<Element> getElementStack() {
      return elementStack;
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
    
}
