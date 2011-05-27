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
package spark.api.uris;

import java.net.URI;

/**
 * Helpful constants for all RDF URIs.
 */
public interface RdfTypes {

  /**
   * RDF base namespace = http://www.w3.org/1999/02/22-rdf-syntax-ns#
   */
  public static final String RDF_BASE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#type
   */
  public static final URI RDF_TYPE = URI.create(RDF_BASE + "type");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#Property
   */
  public static final URI RDF_PROPERTY = URI.create(RDF_BASE + "Property");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement
   */
  public static final URI RDF_STATEMENT = URI.create(RDF_BASE + "Statement");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#subject
   */
  public static final URI RDF_SUBJECT = URI.create(RDF_BASE + "subject");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate
   */
  public static final URI RDF_PREDICATE = URI.create(RDF_BASE + "predicate");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#type
   */
  public static final URI RDF_OBJECT = URI.create(RDF_BASE + "object");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag
   */
  public static final URI RDF_BAG = URI.create(RDF_BASE + "Bag");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq
   */
  public static final URI RDF_SEQ = URI.create(RDF_BASE + "Seq");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt
   */
  public static final URI RDF_ALT = URI.create(RDF_BASE + "Alt");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#value
   */
  public static final URI RDF_VALUE = URI.create(RDF_BASE + "value");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#List
   */
  public static final URI RDF_LIST = URI.create(RDF_BASE + "List");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#nil
   */
  public static final URI RDF_NIL = URI.create(RDF_BASE + "nil");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#first
   */
  public static final URI RDF_FIRST = URI.create(RDF_BASE + "first");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral
   */
  public static final URI RDF_XML_LITERAL = URI.create(RDF_BASE + "XMLLiteral");
  
  /**
   * http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral
   */
  public static final URI RDF_PLAIN_LITERAL = URI.create(RDF_BASE + "PlainLiteral");
  
}