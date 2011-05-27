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
package spark.api;

import spark.api.rdf.NamedNode;
import spark.api.rdf.RDFNode;
import spark.api.rdf.Resource;
import spark.api.rdf.Triple;

/**
 * <p>Cursored access to a sequence of triples.  Each triple consists 
 * of a subject, predicate, and object</p>
 * 
 * <p>For the current triple under the cursor, these generic methods 
 * can be used to inspect the solution:</p>
 * <ul>
 * <li>{@link #getTriple()} - return a Triple with SPO</li>
 * <li>{@link #getSubject()} - just subject</li>
 * <li>{@link #getPredicate()} - just predicate</li>
 * <li>{@link #getObject()} - just object</li>
 * </ul>
 */
public interface Triples extends CursoredResult<Triple> {

  /**
   * Get the triple at the cursor.
   * @return Current triple
   */
  Triple getTriple();
  
  /**
   * Get the subject for the triple at the cursor.
   * @return The subject
   */
  Resource getSubject();
  
  /**
   * Get the predicate for the triple at the cursor.
   * @return The predicate
   */
  NamedNode getPredicate();

  /**
   * Get the object for the triple at the cursor.
   * @return The object
   */
  RDFNode getObject();  
}
