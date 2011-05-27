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
package spark.protocol;

import java.net.MalformedURLException;
import java.net.URL;

import spark.api.Credentials;
import spark.api.DataSource;

/**
 * This is the entry point to a spark-api implementation that accesses SPARQL 
 * endpoints over HTTP.  To connect to a SPARQL endpoint and run queries:
 * 
 * <code>
 * DataSource myDS = new ProtocolDataSource("http://DBpedia.org/sparql");
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
public class ProtocolDataSource implements DataSource {

  private final URL url;
  
  /**
   * Construct a ProtocolDataSource with a SPARQL endpoint URL as a string
   * @param url The url string
   * @throws MalformedURLException If the URL is invalid
   */
  public ProtocolDataSource(String url) throws MalformedURLException {
    this(new URL(url));
  }
  
  /**
   * Construct a ProtocolDataSource with a SPARQL endpoint URL.
   * @param url The url
   */
  public ProtocolDataSource(URL url) {
    this.url = url;
  }
  
  /**
   * Get the URL this DataSource is connecting to.
   * @return The endpoint url
   */
  public URL getUrl() {
    return this.url;
  }
  
  @Override
  public ProtocolConnection getConnection(Credentials creds) {
    return new ProtocolConnection(this, creds);
  }

}
