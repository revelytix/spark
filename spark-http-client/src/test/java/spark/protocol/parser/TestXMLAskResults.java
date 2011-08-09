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

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import spark.api.BooleanResult;
import spark.protocol.ProtocolResult;

/**
 * @author Alex Hall
 * @created Aug 9, 2011
 */
public class TestXMLAskResults extends TestCase {
  
  private static final String TEST_DIR = "src/test/resources/sparql-xml/";
  private static final String FILE_EXT = ".xml";
  
  private static BooleanResult getTestData(String testName) throws Exception {
    String fn = TEST_DIR + testName + FILE_EXT;
    return (BooleanResult) XMLResultsParser.parseResults(null, new FileInputStream(fn), null);
  }

  static void booleanTest(String testName, boolean value, String... metadata) throws Exception {
    BooleanResult r = getTestData(testName);
    assertNotNull(r);
    
    assertEquals(value, r.getResult());
    
    assertTrue(r instanceof ProtocolResult);
    List<String> md = ((ProtocolResult)r).getMetadata();
    assertEquals(Arrays.asList(metadata), md);
  }
  
  public void testBooleanResult() throws Exception {
    booleanTest("boolean-true", true);
    booleanTest("boolean-false", false);
  }
  
  public void testBooleanMetadata() throws Exception {
    booleanTest("boolean-with-metadata", true,
        "http://example.org/boolean-result/metadata.rdf", "http://bar.com/baz.ttl");
  }
  
  public void testBooleanVars() throws Exception {
    // Parsing a boolean result with <variable> elements in <head> should succeed, but with warnings.
    booleanTest("boolean-with-vars", false, "http://example.org/metadata.rdf");
  }
}
