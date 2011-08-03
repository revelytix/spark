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

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import spark.api.Credentials;
import spark.api.DataSource;

/**
 * This is the entry point to a spark-api implementation that accesses SPARQL 
 * endpoints over HTTP.  To connect to a SPARQL endpoint and run queries:
 * 
 * <pre>
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
 * </pre>
 */
public class ProtocolDataSource implements DataSource {
  
  public static final int NO_ACQUIRE_TIMEOUT = 0;

  public static final int DEFAULT_POOL_SIZE = 10;
  public static final int DEFAULT_ACQUIRE_TIMEOUT = NO_ACQUIRE_TIMEOUT;
  
  private static final String HTTP_SCHEME = "http";
  private static final String HTTPS_SCHEME = "https";
  private static final int HTTP_PORT = 80;
  private static final int HTTPS_PORT = 443;
  
  private HttpClient httpClient = null;
  
  private int poolSize = DEFAULT_POOL_SIZE;
  
  private int acquireTimeout = DEFAULT_ACQUIRE_TIMEOUT;
  
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
    return new ProtocolConnection(this, getClient(true), creds);
  }

  public int getConnectionPoolSize() {
    return poolSize;
  }

  public synchronized void setConnectionPoolSize(int poolSize) {
    if (httpClient != null) {
      throw new IllegalStateException("Cannot set the connection pool size after it is in use.");
    }
    this.poolSize = poolSize;
  }

  public int getAcquireTimeout() {
    return acquireTimeout;
  }

  public void setAcquireTimeout(int seconds) {
    this.acquireTimeout = seconds;
  }

  private synchronized HttpClient getClient(boolean create) {
    if (httpClient == null && create) {
      httpClient = createPooledClient();
    }
    return httpClient;
  }
  
  /**
   * Creates a new thread-safe HTTP connection pool for use with a data source.
   * TODO Figure out when to release the pool.
   * @param poolSize The size of the connection pool.
   * @return A new connection pool with the given size.
   */
  private HttpClient createPooledClient() {
    HttpParams connMgrParams = new BasicHttpParams();
    
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme(HTTP_SCHEME, PlainSocketFactory.getSocketFactory(), HTTP_PORT));
    schemeRegistry.register(new Scheme(HTTPS_SCHEME, SSLSocketFactory.getSocketFactory(), HTTPS_PORT));
    
    // All connections will be to the same endpoint, so no need for per-route configuration.
    // TODO See how this does in the presence of redirects.
    ConnManagerParams.setMaxTotalConnections(connMgrParams, poolSize);
    ConnManagerParams.setMaxConnectionsPerRoute(connMgrParams, new ConnPerRouteBean(poolSize));
    
    ClientConnectionManager ccm = new ThreadSafeClientConnManager(connMgrParams, schemeRegistry);
    
    HttpParams httpParams = new BasicHttpParams();
    HttpProtocolParams.setUseExpectContinue(httpParams, false);
    ConnManagerParams.setTimeout(httpParams, acquireTimeout);
    return new DefaultHttpClient(ccm, httpParams);
  }
}
