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

/**
 * A connection to a SPARQL processor.  Connections can be used to 
 * create commands, get metadata, or be closed. 
 */
public interface Connection extends Closeable {

  /**
   * Is this connection closed?
   * @return True if closed
   */
  boolean isClosed();
  
  /**
   * Get the DataSource which created this connection.
   * @return Originating DataSource
   */  
  DataSource getDataSource();
  
  /**
   * Get the service description metadata for this connection.
   * @return The service description metadata
   */
  ServiceDescription getDescription();
  
  /**
   * Create a command for executing SPARQL statements.
   * @param commandString The SPARQL statement
   * @return The command
   */
  Command createCommand(String commandString);
  
}