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
import java.util.Arrays;

import junit.framework.TestCase;
import spark.api.Solutions;
import spark.api.uris.XsdTypes;
import spark.protocol.SparqlCall;
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
  
  private static Solutions getTestData(String testName) throws Exception {
    String fn = TEST_DIR + testName + FILE_EXT;
    return SparqlCall.getSolution(null, new FileInputStream(fn));
  }
  
  public void testEmptyResults() throws Exception {
    Solutions s = getTestData("empty-results");
    assertNotNull(s);
    try {
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
}
