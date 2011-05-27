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

/**
 * <p>Cursored access to a sequence of SPARQL solutions.  Each solution consists 
 * of a set of variables and values.  Variables may be unbound in a solution - how 
 * this is perceived in the accessors depends on the method and is documented in
 * the method.</p>
 * 
 * <p>For the solution set as a whole, these methods apply:</p>
 * <ul>
 * <li>{@link #getVariables()}</li>
 * </ul>
 * 
 * <p>For the current solution under the cursor, these generic methods 
 * can be used to inspect the solution:</p>
 * <ul>
 * <li>{@link #getSolution()} - as map (unbound omitted)</li>
 * <li>{@link #getSolutionList()} - as list (unbound are null)</li>
 * <li>{@link #getBinding(String)} - get variable's value or null if unbound</li>
 * <li>{@link #isBound(String)} - check whether variable is bound in solution</li>
 * </ul>
 * 
 * <p>If you have prior knowledge of the return types, these methods can be 
 * used to attempt the conversion and simplify your access code (with the caveat
 * that if you are incorrect in your knowledge an exception will be thrown).</p>
 * <ul>
 * <li>{@link #getNamedNode(String)}</li>
 * <li>{@link #getBlankNode(String)}</li>
 * <li>{@link #getLiteral(String)}</li>
 * <li>{@link #getURI(String)}</li>
 * <li>{@link #getString(String)}</li>
 * <li>{@link #getInt(String)}</li>
 * <li>{@link #getBoolean(String)}</li>
 * <li>{@link #getFloat(String)}</li>
 * <li>{@link #getDouble(String)}</li>
 * <li>{@link #getInteger(String)}</li>
 * <li>{@link #getDateTime(String)}</li>
 * </ul>
 */
public interface Solutions extends CursoredResult<Map<String,RDFNode>> {
  
  /**
   * Get all variables possible in these solutions.
   * @return List of variable names
   */
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
  
  /**
   * Get the value of a variable as a named node (IRI).  If 
   * the value is not a named node, then a SparqlException is thrown.
   * @param variable The variable name
   * @return The named node value for this variable or null if unbound 
   * @throws SparqlException If the value is not a named node
   */
  NamedNode getNamedNode(String variable) throws SparqlException;
  
  /**
   * Get the value of a variable as a blank node.  If the value 
   * is not a blank node, then a SparqlException is thrown.
   * @param variable The variable name
   * @return The blank node value for this variable or null if unbound
   * @throws SparqlException If the value is not a blank node
   */  
  BlankNode getBlankNode(String variable) throws SparqlException;
  
  /**
   * Get the value of a variable as a literal.  If the value 
   * is not a literal, then a SparqlException is thrown. 
   * @param variable The variable name
   * @return The literal value for this variable or null if unbound
   * @throws SparqlException If the value is not a literal
   */
  Literal getLiteral(String variable) throws SparqlException;
  
  // Literal value mappings based on XSD, may fail if var is wrong type
  
  /**
   * Get the value of a variable as a URI.  If the value is not a named 
   * node, then a SparqlException is thrown.
   * @param variable The variable name
   * @return The URI for this named node resource or null if unbound
   * @throws SparqlException If the value is not a named node
   */
  URI getURI(String variable) throws SparqlException;
  
  /**
   * Get the value of a variable as a string.  If the value is not a plain
   * literal or an xsd:string typed literal, then a SparqlException is 
   * thrown.
   * @param variable The variable name
   * @return The string for this plain literal or typed xsd:string literal
   * @throws SparqlException If the value is not a plain literal or xsd:string typed literal
   */
  String getString(String variable) throws SparqlException;
  
  /**
   * Get the value of a variable as an int.  If the value is not
   * an xsd:int typed literal, then a SparqlException is thrown.
   * @param variable The variable name
   * @return The int for this typed xsd:int literal
   * @throws SparqlException If the value is not an xsd:int literal
   */  
  int getInt(String variable) throws SparqlException;
  
  /**
   * Get the value of a variable as a boolean.  If the value is not
   * an xsd:boolean typed literal, then a SparqlException is thrown.
   * @param variable The variable name
   * @return The boolean for this typed xsd:boolean literal
   * @throws SparqlException If the value is not an xsd:boolean literal
   */  
  boolean getBoolean(String variable) throws SparqlException;
  
  /**
   * Get the value of a variable as a float.  If the value is not
   * an xsd:float typed literal, then a SparqlException is thrown.
   * @param variable The variable name
   * @return The float for this typed xsd:float literal
   * @throws SparqlException If the value is not an xsd:float literal
   */  
  float getFloat(String variable) throws SparqlException;
  
  /**
   * Get the value of a variable as a double.  If the value is not
   * an xsd:double typed literal, then a SparqlException is thrown.
   * @param variable The variable name
   * @return The long for this typed xsd:double literal
   * @throws SparqlException If the value is not an xsd:double literal
   */  
  double getDouble(String variable) throws SparqlException;
  
  /**
   * Get the value of a variable as a BigInteger.  If the value is not 
   * an xsd:integer typed literal, then a SparqlException is thrown.
   * @param variable The variable name
   * @return The BigInteger for this typed xsd:integer literal
   * @throws SparqlException If the value is not an xsd:integer literal
   */  
  BigInteger getInteger(String variable) throws SparqlException;

  /**
   * Get the value of a variable as a Date.  If the value is not a 
   * an xsd:dateTime typed literal, then a SparqlException is thrown.
   * @param variable The variable name
   * @return The Date for this typed xsd:dateTime literal
   * @throws SparqlException If the value is not an xsd:dateTime literal
   */  
  Date getDateTime(String variable) throws SparqlException;  
}