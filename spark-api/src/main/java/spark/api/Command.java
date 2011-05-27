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

import java.io.Closeable;
import java.util.List;
import java.util.Map;

import spark.api.rdf.RDFNode;

/**
 * Represents a SPARQL command for execution.  Commands are created from a {@link Connection} and returns
 * a {@link Result} when executed.
 */
public interface Command extends Closeable {

  /**
   * Is the command closed?
   * @return True if closed
   */
  boolean isClosed();
  
  /**
   * Get the connection from whence this came.
   * @return The source connection
   */
  Connection getConnection();
  
  /**
   * Get the command string this command will execute.
   * @return The command string
   */
  String getCommand();

  // Timeouts and cancellations

  /**
   * Constant specifying no timeout, for use with {@link #setTimeout(long)}
   */
  public static final long NO_TIMEOUT = 0L;
  
  /**
   * Set the command timeout in seconds or {@link #NO_TIMEOUT} for none.
   * @param seconds Timeout in seconds
   */
  void setTimeout(long seconds);
  
  /**
   * Get the command timeout in seconds.
   * @return Timeout in seconds
   */
  long getTimeout();
  
  /**
   * Cancel execution of this command.
   */
  void cancel();
  
  // Preparation and batching 
  
  /**
   * Add parameter bindings prior to the execution of this command.  The Map
   * will contain variable names as keys and rdf data as values.
   * @param binding A set of bindings from variable to value
   */
  void addParameterBindings(Map<String, RDFNode> binding);
  
  /**
   * Get all parameter bindings that have been added.
   * @return A series of parameter bindings from variable to value
   */
  List<Map<String, RDFNode>> getBindings();
  
  /**
   * Clear all parameter bindings
   */
  void clearBindings();
    
  // Execute 
  
  /**
   * Execute the command and return the natural result style depending on the command.
   * @return The result of execution
   */
  Result execute();
  
  /**
   * Execute a SELECT query and return a Solutions result.
   * @return The result solutions
   */
  Solutions executeQuery();
  
  /**
   * Execute an ASK query and return either true or false.
   * @return The result of the ASK
   */
  boolean executeAsk();
}