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
 * Helpful constants for all RDFS URIs.
 */
public interface RdfsTypes {

  /**
   * RDFS namespace: http://www.w3.org/2000/01/rdf-schema#
   */
  public static final String RDFS_BASE = "http://www.w3.org/2000/01/rdf-schema#";
  
  /**
   * http://www.w3.org/2000/01/rdf-schema#label
   */
  public static final URI RDFS_LABEL = URI.create(RDFS_BASE + "label");
  
  /**
   * http://www.w3.org/2000/01/rdf-schema#Resource
   */
  public static final URI RDFS_RESOURCE = URI.create(RDFS_BASE + "Resource");

  /**
   * http://www.w3.org/2000/01/rdf-schema#Class
   */
  public static final URI RDFS_CLASS = URI.create(RDFS_BASE + "Class");
  
  /**
   * http://www.w3.org/2000/01/rdf-schema#subClassOf
   */
  public static final URI RDFS_SUB_CLASS_OF = URI.create(RDFS_BASE + "subClassOf");
  
  /**
   * http://www.w3.org/2000/01/rdf-schema#subPropertyOf
   */
  public static final URI RDFS_SUB_PROPERTY_OF = URI.create(RDFS_BASE + "subPropertyOf");
  
  /**
   * http://www.w3.org/2000/01/rdf-schema#comment
   */
  public static final URI RDFS_COMMENT = URI.create(RDFS_BASE + "comment");
  
  /**
   * http://www.w3.org/2000/01/rdf-schema#Literal
   */
  public static final URI RDFS_LITERAL = URI.create(RDFS_BASE + "Literal");

  /**
   * http://www.w3.org/2000/01/rdf-schema#domain
   */
  public static final URI RDFS_DOMAIN = URI.create(RDFS_BASE + "domain");

  /**
   * http://www.w3.org/2000/01/rdf-schema#range
   */
  public static final URI RDFS_RANGE = URI.create(RDFS_BASE + "range");

  /**
   * http://www.w3.org/2000/01/rdf-schema#seeAlso
   */
  public static final URI RDFS_SEE_ALSO = URI.create(RDFS_BASE + "seeAlso");

  /**
   * http://www.w3.org/2000/01/rdf-schema#isDefinedBy
   */
  public static final URI RDFS_IS_DEFINED_BY = URI.create(RDFS_BASE + "isDefinedBy");

  /**
   * http://www.w3.org/2000/01/rdf-schema#Container
   */
  public static final URI RDFS_CONTAINER = URI.create(RDFS_BASE + "Container");

  /**
   * http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty
   */
  public static final URI RDFS_CONTAINER_MEMBERSHIP_PROPERTY = URI.create(RDFS_BASE + "ContainerMembershipProperty");

  /**
   * http://www.w3.org/2000/01/rdf-schema#member
   */
  public static final URI RDFS_MEMBER = URI.create(RDFS_BASE + "member");

  /**
   * http://www.w3.org/2000/01/rdf-schema#Datatype
   */
  public static final URI RDFS_DATATYPE = URI.create(RDFS_BASE + "Datatype");
  
}
