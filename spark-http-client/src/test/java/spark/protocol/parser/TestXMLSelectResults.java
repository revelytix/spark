/*
 * Copyright 2011 Revelytix Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.protocol.parser;

import static spark.spi.TestCursor.AFTER_LAST;
import static spark.spi.TestCursor.BEFORE_FIRST;
import static spark.spi.TestCursor.FIRST;
import static spark.spi.TestCursor.LAST;
import static spark.spi.TestCursor.NONE;

import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import spark.api.Command;
import spark.api.Connection;
import spark.api.Solutions;
import spark.api.credentials.NoCredentials;
import spark.api.uris.XsdTypes;
import spark.protocol.ProtocolDataSource;
import spark.protocol.ProtocolResult;
import spark.spi.TestCursor;
import spark.spi.rdf.BlankNodeImpl;
import spark.spi.rdf.NamedNodeImpl;
import spark.spi.rdf.PlainLiteralImpl;
import spark.spi.rdf.TypedLiteralImpl;

/**
 * Test cases for the SPARQL XML results parser.
 * 
 * @author Alex Hall
 * @created Aug 1, 2011
 */
public class TestXMLSelectResults extends TestCase {

  private static final String TEST_DIR = "src/test/resources/sparql-xml/";
  private static final String FILE_EXT = ".xml";
  
  private static Solutions getTestData(Command cmd, String testName) throws Exception {
    String fn = TEST_DIR + testName + FILE_EXT;
    return (Solutions) XMLResultsParser.parseResults(cmd, new FileInputStream(fn), null);
  }
  
  private static Solutions getTestData(String testName) throws Exception {
    return getTestData(null, testName);
  }
  
  public void testEmptyResults() throws Exception {
    Solutions s = getTestData("empty-results");
    assertNotNull(s);
    try {
      assertTrue(s instanceof ProtocolResult);
      assertTrue(((ProtocolResult)s).getMetadata().isEmpty());
      
      assertEquals(Arrays.asList("foo", "bar"), s.getVariables());
      
      // Check cursor methods.
      TestCursor.assertCursor(s, BEFORE_FIRST);
      
      // Assert no results.
      assertFalse(s.next());
      
      // Check cursor methods.
      TestCursor.assertCursor(s, AFTER_LAST);
    } finally {
      s.close();
    }
  }
  
  public void testSingleResult() throws Exception {
    URI u = URI.create("http://example.org/members/Member00004403730");
    Solutions s = getTestData("single-result");
    assertNotNull(s);
    try {
      assertTrue(s instanceof ProtocolResult);
      assertTrue(((ProtocolResult)s).getMetadata().isEmpty());
      
      assertEquals(Arrays.asList("x", "y", "z"), s.getVariables());
      
      // Check cursor methods.
      TestCursor.assertCursor(s, BEFORE_FIRST);
      
      // Check single row
      assertTrue(s.next());
      TestCursor.assertCursor(s, FIRST | LAST);
      assertEquals(new NamedNodeImpl(u), s.getBinding("x"));
      assertEquals(u, s.getURI("x"));
      assertEquals(new PlainLiteralImpl("John Doe"), s.getBinding("y"));
      assertFalse(s.isBound("z"));
      assertNull(s.getBinding("z"));
      
      // Check end of results.
      assertFalse(s.next());
      TestCursor.assertCursor(s, AFTER_LAST);
    } finally {
      s.close();
    }
  }
  
  public void testResults() throws Exception {
    Solutions s = getTestData("sparql-results");
    assertNotNull(s);
    try {
      assertTrue(s instanceof ProtocolResult);
      assertTrue(((ProtocolResult)s).getMetadata().isEmpty());
      
      String var = "a";
      assertEquals(Arrays.asList(var), s.getVariables());
      
      // Check cursor methods.
      TestCursor.assertCursor(s, BEFORE_FIRST);

      // Check results.
      assertTrue(s.next());
      TestCursor.assertCursor(s, FIRST);
      assertEquals(URI.create("http://example.org/a"), s.getURI(var));
      
      assertTrue(s.next());
      TestCursor.assertCursor(s, NONE);
      assertEquals(new BlankNodeImpl("node0"), s.getBinding(var));
      
      assertTrue(s.next());
      TestCursor.assertCursor(s, NONE);
      assertEquals(new PlainLiteralImpl("xyz"), s.getBinding(var));
      
      assertTrue(s.next());
      TestCursor.assertCursor(s, NONE);
      assertEquals(new TypedLiteralImpl("100", XsdTypes.INT), s.getBinding(var));
      assertEquals(100, s.getInt(var));
      
      assertTrue(s.next());
      TestCursor.assertCursor(s, LAST);
      assertEquals(new PlainLiteralImpl("chat", "fr"), s.getBinding(var));
      
      // Check end of results.
      assertFalse(s.next());
      TestCursor.assertCursor(s, AFTER_LAST);
    } finally {
      s.close();
    }
  }
  
  static void metadataTest(Command cmd, String testName, String... metadata) throws Exception {
    Solutions s = getTestData(cmd, testName);
    try {
      assertNotNull(s);
      assertTrue(s instanceof ProtocolResult);
      List<String> md = ((ProtocolResult)s).getMetadata();
      assertNotNull(md);
      assertEquals(Arrays.asList(metadata), md);
      
      String varX = "x";
      String varY = "y";
      assertEquals(Arrays.asList(varX, varY), s.getVariables());
      
      TestCursor.assertCursor(s, BEFORE_FIRST);
      
      assertTrue(s.next());
      TestCursor.assertCursor(s, FIRST | LAST);
      assertEquals(URI.create("http://example.org/foo"), s.getURI(varX));
      assertEquals("bar", s.getString(varY));
      
      assertFalse(s.next());
      TestCursor.assertCursor(s, AFTER_LAST);
    } finally {
      s.close();
    }
  }
  
  public void testMetadata() throws Exception {
    URL serviceUrl = new URL("http://example.org/sparql");
    // dummy data source to pass the URL down to the parser.
    ProtocolDataSource ds = new ProtocolDataSource(serviceUrl);
    try {
      Connection c = ds.getConnection(NoCredentials.INSTANCE);
      Command cmd = c.createCommand("SELECT foo"); // query isn't actually executed.
      
      metadataTest(cmd, "results-with-metadata",
          "http://sample.org/metadata.rdf", "http://example.org/service-description.rdf");
      metadataTest(cmd, "results-with-base-uri",
          "http://sample.org/metadata.rdf", "http://revelytix.com/ns/service-description.rdf", "http://example.com/sparql.ttl");
      metadataTest(cmd, "results-with-bad-link",
          "http://sample.org/metadata.rdf", "http://example.org/service-description.rdf");
    } finally {
      ds.close();
    }
  }
}
