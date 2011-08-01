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
package spark.spi;

import static spark.spi.TestCursor.AFTER_LAST;
import static spark.spi.TestCursor.BEFORE_FIRST;
import static spark.spi.TestCursor.FIRST;
import static spark.spi.TestCursor.LAST;
import static spark.spi.TestCursor.NONE;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import spark.api.rdf.BlankNode;
import spark.api.rdf.Literal;
import spark.api.rdf.NamedNode;
import spark.api.rdf.RDFNode;
import spark.spi.rdf.BlankNodeImpl;
import spark.spi.rdf.NamedNodeImpl;
import spark.spi.rdf.PlainLiteralImpl;

public class TestSolutionSet extends TestCase {
  
  private static final Date A_DATE = new Date(1311190453435L);
  private static final String A_DATE_TIME = "2011-07-20T15:34:13.435-04:00";
  
	public void testSolution() {
		// setup solutions
		Map<String, RDFNode> solution1 = new HashMap<String, RDFNode>();
		BlankNodeImpl node1 = new BlankNodeImpl("1");
		solution1.put("x", node1);
		Map<String, RDFNode> solution2 = new HashMap<String, RDFNode>();
		solution2.put("x", new BlankNodeImpl("2"));
		List<Map<String, RDFNode>> solutionList = new ArrayList<Map<String, RDFNode>>();
		solutionList.add(solution1);
		solutionList.add(solution2);
				
		// exercise SolutionSet
		SolutionSet s = new SolutionSet( null, 
				Arrays.asList(new String[]{"x"}),
				solutionList  );
		
    TestCursor.assertCursor(s, BEFORE_FIRST);
		
		assertTrue(s.next());
    TestCursor.assertCursor(s, FIRST);
		assertEquals(node1, s.getBinding("x"));
		assertEquals(solution1, s.getResult());
		
    assertTrue(s.next());
		assertEquals(solution2, s.getResult());
    TestCursor.assertCursor(s, LAST);
		
    assertFalse(s.next());
    TestCursor.assertCursor(s, AFTER_LAST);
	}
	
	public void testDatatypes() {
	  String var = "x";
	  List<Map<String,RDFNode>> sl = new ArrayList<Map<String,RDFNode>>();
	  for (int i = 0; i < 10; i++) {
	    sl.add(new HashMap<String,RDFNode>());
	  }
	  
	  NamedNode uriRef = new NamedNodeImpl(URI.create("http://example.org/test"));
	  BlankNode bn = new BlankNodeImpl("1");
	  Literal lit = new PlainLiteralImpl("foo");
	  
	  sl.get(0).put(var, uriRef);
	  sl.get(1).put(var, bn);
	  sl.get(2).put(var, lit);
	  sl.get(3).put(var, new PlainLiteralImpl(A_DATE_TIME));
    sl.get(4).put(var, new PlainLiteralImpl("198765415975423167465132498465"));
    sl.get(5).put(var, new PlainLiteralImpl("true"));
    sl.get(6).put(var, new PlainLiteralImpl("3.14"));
    sl.get(7).put(var, new PlainLiteralImpl("98.6"));
    sl.get(8).put(var, new PlainLiteralImpl("42"));
    
    SolutionSet s = new SolutionSet(null, Arrays.asList("x"), sl);
    TestCursor.assertCursor(s, BEFORE_FIRST);
    
    assertTrue(s.next());
    TestCursor.assertCursor(s, FIRST);
    assertEquals(uriRef, s.getNamedNode(var));    
    assertEquals(uriRef.getURI(), s.getURI(var));    
    
    assertTrue(s.next());
    TestCursor.assertCursor(s, NONE);
    assertEquals(bn, s.getBlankNode(var));    
    
    assertTrue(s.next());
    TestCursor.assertCursor(s, NONE);
    assertEquals(lit, s.getLiteral(var));
    assertEquals("foo", s.getString(var));
    
    assertTrue(s.next());
    TestCursor.assertCursor(s, NONE);
    assertEquals(A_DATE, s.getDateTime(var));    
    
    assertTrue(s.next());
    TestCursor.assertCursor(s, NONE);
    assertEquals(new BigInteger("198765415975423167465132498465"), s.getInteger(var));    
    
    assertTrue(s.next());
    TestCursor.assertCursor(s, NONE);
    assertEquals(true, s.getBoolean(var));    
    
    assertTrue(s.next());
    TestCursor.assertCursor(s, NONE);
    assertEquals(3.14d, s.getDouble(var));    
    
    assertTrue(s.next());
    TestCursor.assertCursor(s, NONE);
    assertEquals(98.6f, s.getFloat(var));    
    
    assertTrue(s.next());
    TestCursor.assertCursor(s, NONE);
    assertEquals(42, s.getInt(var));    
    
    assertTrue(s.next());
    TestCursor.assertCursor(s, LAST);
    assertNull(s.getBinding(var));
    
    assertFalse(s.next());
    TestCursor.assertCursor(s, AFTER_LAST);
	}
	
	public void testIterator() {
    // setup solutions
    Map<String, RDFNode> solution1 = new HashMap<String, RDFNode>();
    BlankNodeImpl node1 = new BlankNodeImpl("1");
    solution1.put("x", node1);
    Map<String, RDFNode> solution2 = new HashMap<String, RDFNode>();
    solution2.put("x", new BlankNodeImpl("2"));
    List<Map<String, RDFNode>> solutionList = new ArrayList<Map<String, RDFNode>>();
    solutionList.add(solution1);
    solutionList.add(solution2);
        
    // setup SolutionSet
    SolutionSet s = new SolutionSet( null, 
        Arrays.asList(new String[]{"x"}),
        solutionList  );

    // exercise iterator
    List<Map<String,RDFNode>> resultList = new ArrayList<Map<String,RDFNode>>(solutionList.size());
    for (Map<String,RDFNode> result : s) {
      resultList.add(result);
    }
    assertEquals(solutionList, resultList);
	}
}
