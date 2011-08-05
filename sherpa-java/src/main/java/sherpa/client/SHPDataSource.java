/*
 * Copyright 2011 Revelytix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sherpa.client;

import spark.api.Connection;
import spark.api.Credentials;
import spark.api.DataSource;

/**
 * This is the entry point to a spark-api implementation that uses the Sherpa
 * protocol to access a Sherpa-aware server.  Sherpa provides high performance 
 * SPARQL access.  To connect to a Sherpa-enabled server and run queries:
 * 
 * <code>
 * DataSource myDS = new SHPDataSource("localhost", 41000);
 * Connection conn = myDS.getConnection(NoCredentials.INSTANCE);
 * Command query = conn.createCommand("SELECT ?p ?o WHERE { <http://dbpedia.org/resource/Terry_Gilliam> ?p ?o }");    
 * Solutions solutions = query.executeQuery();
 *     
 * System.out.println("vars = " + solutions.getVariables());
 * int row = 0;
 * for(Map<String, RDFNode> solution : solutions) {
 *   System.out.println("Row " + (row++) + ": " + solution);
 * }
 * solutions.close();
 * query.close();
 * conn.close();
 * </code>
 */
public class SHPDataSource implements DataSource {

  private volatile String host;
  private volatile int port;
  
  /**
   * Construct a SHPDataSource with host name and port.
   * @param host Host name (such as "localhost")
   * @param port Port number
   */
  public SHPDataSource(String host, int port) {
    setHost(host);
    setPort(port);
  }
  
  /**
   * Get host name
   * @return The host name
   */
  public String getHost() {
    return this.host;
  }
  
  /**
   * Set host name
   * @param host Host name
   */
  public void setHost(String host) {
    this.host = host;
  }
  
  /**
   * Get port
   * @return Port number
   */
  public int getPort() {
    return this.port;
  }
  
  /**
   * Set port number
   * @param port Port number
   */
  public void setPort(int port) {
    this.port = port;
  }
  
  /**
   * Validate whether the currently set data source parameters are valid.
   * 
   * @throws IllegalArgumentException if host is null or port < 0
   */
  public void validate() throws IllegalArgumentException {
    if(host == null) {
      throw new IllegalArgumentException("Host cannot be null");
    }
    if(port <= 0) {
      throw new IllegalArgumentException("Port must be > 0");
    }
  }
  
  @Override
  public Connection getConnection(Credentials creds) {
    validate();
    return new SHPConnection(this);
  }

  @Override
  public void close() {
    // This data source just wraps the connection parameters; nothing to close.
  }

}
