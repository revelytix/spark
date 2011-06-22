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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spark.api.rdf.RDFNode;
import spark.spi.rdf.BlankNodeImpl;

import junit.framework.TestCase;

public class TestSolutionSet extends TestCase {
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
		
		assertTrue(s.isBeforeFirst());
		assertFalse(s.isAfterLast());
		
		s.next();
		assertFalse(s.isBeforeFirst());
		assertFalse(s.isAfterLast());
		assertEquals(node1, s.getBinding("x"));
		assertEquals(solution1, s.getSolution());
		assertEquals(Arrays.asList(new RDFNode[]{node1}), s.getSolutionList());
		
		s.next();
		assertEquals(solution2, s.getSolution());
		assertFalse(s.isBeforeFirst());
		assertFalse(s.isAfterLast());
		
		s.next();
		assertFalse(s.isBeforeFirst());
		assertTrue(s.isAfterLast());	
	}
}
