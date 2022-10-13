/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.eclipse.appengine.standard.java8;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.tools.eclipse.util.Xslt;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Test the appengine-web.xml XSLT transforms
 */
public class AppEngineXsltTransformTest {

  @Test
  public void testAddBare() {
    Document transformed = transform("/xslt/addJava8Runtime.xsl",
        "<appengine-web-app xmlns=\"http://appengine.google.com/ns/1.0\"/>");
    assertEquals("java8", getRuntimeValue(transformed));
  }

  @Test
  public void testAddExistingRuntime() {
    Document transformed = transform("/xslt/addJava8Runtime.xsl",
        "<appengine-web-app xmlns=\"http://appengine.google.com/ns/1.0\"><runtime>XXX</runtime></appengine-web-app>");
    assertEquals("java8", getRuntimeValue(transformed));
  }

  @Test
  public void testAddRuntimeAtEnd() {
    Document transformed = transform("/xslt/addJava8Runtime.xsl",
        "<appengine-web-app xmlns=\"http://appengine.google.com/ns/1.0\"><threadsafe>true</threadsafe></appengine-web-app>");
    Element runtime = getRuntimeElement(transformed);
    assertEquals("java8", runtime.getTextContent());
    Text previous = (Text) runtime.getPreviousSibling();
    assertTrue(previous.getNodeValue().trim().isEmpty());
    Text next = (Text) runtime.getNextSibling();
    String s = next.getNodeValue();
    assertEquals("\n", s);
  }

  @Test
  public void testAddPreservesOrder() {
    Document transformed = transform("/xslt/addJava8Runtime.xsl",
        "<appengine-web-app xmlns=\"http://appengine.google.com/ns/1.0\">"
            + "<threadsafe>true</threadsafe><runtime>XXX</runtime>"
            + "<sessions-enabled>true</sessions-enabled></appengine-web-app>");
    Element documentElement = transformed.getDocumentElement();
    NodeList childElements = documentElement.getChildNodes();
    assertEquals("appengine-web-app", documentElement.getTagName());
    assertEquals(3, childElements.getLength());
    assertEquals("threadsafe", ((Element) childElements.item(0)).getTagName());
    assertEquals("true", childElements.item(0).getTextContent());
    assertEquals("runtime", ((Element) childElements.item(1)).getTagName());
    assertEquals("java8", childElements.item(1).getTextContent());
    assertEquals("sessions-enabled", ((Element) childElements.item(2)).getTagName());
    assertEquals("true", childElements.item(2).getTextContent());
  }

  @Test
  public void testAddExistingJava8Runtime() {
    Document transformed = transform("/xslt/addJava8Runtime.xsl",
        "<appengine-web-app xmlns=\"http://appengine.google.com/ns/1.0\">"
            + "<runtime>java8</runtime></appengine-web-app>");
    assertEquals("java8", getRuntimeValue(transformed));
  }

  @Test
  public void testdAddBareNoDefaultNS() {
    Document transformed = transform("/xslt/addJava8Runtime.xsl",
        "<ae:appengine-web-app xmlns:ae=\"http://appengine.google.com/ns/1.0\"/>");
    assertEquals("java8", getRuntimeValue(transformed));
  }

  @Test
  public void testRemoveBare() {
    Document transformed = transform("/xslt/removeJava8Runtime.xsl",
        "<appengine-web-app xmlns=\"http://appengine.google.com/ns/1.0\"/>");
    assertNull(getRuntimeValue(transformed));
  }

  @Test
  public void testRemovePreservesNonJava8Runtime() {
    Document transformed = transform("/xslt/removeJava8Runtime.xsl",
        "<appengine-web-app xmlns=\"http://appengine.google.com/ns/1.0\">"
            + "<runtime>XXX</runtime></appengine-web-app>");
    assertEquals("XXX", getRuntimeValue(transformed));
  }

  @Test
  public void testRemovePreservesOrder() {
    Document transformed = transform("/xslt/removeJava8Runtime.xsl",
        "<appengine-web-app xmlns=\"http://appengine.google.com/ns/1.0\">"
            + "<threadsafe>true</threadsafe>" + "<runtime>java8</runtime>"
            + "<sessions-enabled>true</sessions-enabled>" + "</appengine-web-app>");
    Element documentElement = transformed.getDocumentElement();
    NodeList childElements = documentElement.getChildNodes();
    assertEquals("appengine-web-app", documentElement.getTagName());
    assertEquals(2, childElements.getLength());
    assertEquals("threadsafe", ((Element) childElements.item(0)).getTagName());
    assertEquals("true", childElements.item(0).getTextContent());
    assertEquals("sessions-enabled", ((Element) childElements.item(1)).getTagName());
    assertEquals("true", childElements.item(1).getTextContent());
  }

  @Test
  public void testRemoveExistingJava8Runtime() {
    Document transformed = transform("/xslt/removeJava8Runtime.xsl",
        "<appengine-web-app xmlns=\"http://appengine.google.com/ns/1.0\">"
            + "<runtime>java8</runtime></appengine-web-app>");
    assertNull(getRuntimeValue(transformed));
  }

  private static Document transform(String templateFile, String inputValue) {
    try {
      URL xslPath = AppEngineXsltTransformTest.class.getResource(templateFile);
      if (xslPath == null) {
        File xslFile = new File("." + templateFile);
        assertTrue(xslFile.exists());
        xslPath = xslFile.toURI().toURL();
      }
      InputStream documentStream =
          new ByteArrayInputStream(inputValue.getBytes(StandardCharsets.UTF_8));
      try (InputStream xslStream = xslPath.openStream();
          InputStream transformed = Xslt.applyXslt(documentStream, xslStream)) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        return builder.parse(transformed);
      }
    } catch (IOException | TransformerException | ParserConfigurationException | SAXException ex) {
      // these aren't interesting for the test
      throw new RuntimeException(ex);
    }

  }

  /** Return the {@code <runtime>} value, or {@code null} if no element found. */
  private static String getRuntimeValue(Document transformed) {
    Element documentElement = transformed.getDocumentElement();
    NodeList runtimeElements =
        documentElement.getElementsByTagNameNS("http://appengine.google.com/ns/1.0", "runtime");
    if (runtimeElements.getLength() == 0) {
      return null;
    }
    assertEquals(1, runtimeElements.getLength());
    assertEquals("runtime", runtimeElements.item(0).getLocalName());
    return runtimeElements.item(0).getTextContent();
  }

  /** Return the {@code <runtime>} element. */
  private static Element getRuntimeElement(Document transformed) {
    Element documentElement = transformed.getDocumentElement();
    NodeList runtimeElements =
        documentElement.getElementsByTagNameNS("http://appengine.google.com/ns/1.0", "runtime");
    assertEquals(1, runtimeElements.getLength());
    return (Element) runtimeElements.item(0);
  }
}
