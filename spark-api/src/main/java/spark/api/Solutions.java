package spark.api;

import java.math.BigInteger;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import spark.api.exception.SparqlException;
import spark.api.rdf.BlankNode;
import spark.api.rdf.NamedNode;
import spark.api.rdf.Literal;
import spark.api.rdf.RDFNode;

public interface Solutions extends CursoredResult<Map<String,RDFNode>> {
  
  // Metadata 
  List<String> getVariables();
  
  // Generic data access as map or node from the current solution
  
  /** 
   * Get the current solution as a Map from variable name to value.
   * Unbound variables will not be included.
   * @return Map of variable to value for the current solution
   */
  Map<String, RDFNode> getSolution();
  
  /**
   * Get the current solution as a List of values in the same
   * order as {@link #getVariables()}.  Unbound variables will
   * be null.
   * @return List of values that match the variables
   */
  List<RDFNode> getSolutionList();
  
  /**
   * Check whether the specified variable is bound in the 
   * current solution.
   * @param variable Variable name 
   * @return True if bound 
   */
  boolean isBound(String variable);
  
  /**
   * Get the bound value for the specified variable or null
   * if unbound. 
   * @param variable The variable name
   * @return The value or null if unbound
   */
  RDFNode getBinding(String variable);
    
  // Generic node access, may fail if var is wrong type
  NamedNode getNamedNode(String variable) throws SparqlException;
  BlankNode getBlankNode(String variable) throws SparqlException;
  Literal getLiteral(String variable) throws SparqlException;
  
  // Literal value mappings based on XSD, may fail if var is wrong type
  URI getURI(String variable) throws SparqlException;
  String getString(String variable) throws SparqlException;
  int getInt(String variable) throws SparqlException;
  boolean getBoolean(String variable) throws SparqlException;
  float getFloat(String variable) throws SparqlException;
  double getDouble(String variable) throws SparqlException;
  BigInteger getInteger(String variable) throws SparqlException;
  Date getDateTime(String variable) throws SparqlException;  
}

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