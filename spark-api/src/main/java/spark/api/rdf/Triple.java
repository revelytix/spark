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
package spark.api.rdf;

/**
 * A Triple holds a subject-predicate-object triple as commonly retrieved from a CONSTRUCT query.
 */
public interface Triple {

  /**
   * Triple subject, which must be an RDF resource.
   * @return RDF resource, either named or blank
   */
  public Resource getSubject();
  
  /**
   * Triple predicate, which must be an RDF named node
   * @return RDF predicate 
   */
  public NamedNode getPredicate();
  
  /**
   * Triple object, which must be an RDF node, either resource or literal
   * @return RDF node
   */
  public RDFNode getObject();
}
