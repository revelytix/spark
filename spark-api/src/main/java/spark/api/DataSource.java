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

/**
 * Entry point for working with the Spark API.  The DataSource 
 * is a factory for connections and implementations should use standard
 * JavaBean patterns to specify any necessary connection properties 
 * (host, port, etc).
 * 
 * Depending on the implementation, a DataSource might be a thin wrapper
 * around a collection of source-specific connection properties, or it
 * might provide additional features such as connection pooling.
 */
public interface DataSource {

  /**
   * Create (or prepare) a Connection using the specified Credentials.
   * 
   * @param creds The connections credentials (user, password, certificate, etc)
   * @return The connection
   */
  Connection getConnection(Credentials creds);
  
  /**
   * Close the DataSource, releasing any underlying resources. Users should
   * close data sources when they are finished working with them, and once
   * closed a data source should not be used any further.
   */
  void close();
  
}