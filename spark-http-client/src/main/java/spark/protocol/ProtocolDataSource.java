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

import spark.api.Command;
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
  
  /** Value to use with {@link #setAcquireTimeout(int)} to indicate no acquire timeout. */
  public static final int NO_ACQUIRE_TIMEOUT = 0;

  /** Default connection pool size for new ProtocolDataSource instances. */
  public static final int DEFAULT_POOL_SIZE = 10;
  /** Default acquire timeout for new ProtocolDataSource instances. */
  public static final int DEFAULT_ACQUIRE_TIMEOUT = NO_ACQUIRE_TIMEOUT;
  
  // HTTP/HTTPS scheme constants.
  private static final String HTTP_SCHEME = "http";
  private static final String HTTPS_SCHEME = "https";
  private static final int HTTP_PORT = 80;
  private static final int HTTPS_PORT = 443;
  
  /** Thread-safe, re-usable HTTP client passed to child connections. */
  private HttpClient httpClient = null;
  
  /** Connection pool size; must be set before the first connection is created. */
  private int poolSize = DEFAULT_POOL_SIZE;
  
  /** Connection pool acquire timeout; must be set before the first connection is created. */
  private int acquireTimeout = DEFAULT_ACQUIRE_TIMEOUT;
  
  /** The endpoint URL. */
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

  @Override
  public void close() {
    HttpClient client = null;
    synchronized(this) {
      client = httpClient;
      httpClient = null;
    }
    if (client != null) {
      client.getConnectionManager().shutdown();
    }
  }

  /** @return the maximum size of the connection pool. */
  public int getConnectionPoolSize() {
    return poolSize;
  }

  /**
   * Sets the maximum size of the connection pool.
   * @param poolSize The maximum number of connections that can be in use at any one time for this data source.
   */
  public synchronized void setConnectionPoolSize(int poolSize) {
    if (httpClient != null) {
      throw new IllegalStateException("Cannot set the connection pool size after it is in use.");
    }
    this.poolSize = poolSize;
  }

  /** @return the timeout, in seconds, for acquiring connections from the pool. */
  public int getAcquireTimeout() {
    return acquireTimeout;
  }

  /**
   * <p>
   * Sets the timeout, in seconds, for acquiring connections from the pool, or
   * {@link #NO_ACQUIRE_TIMEOUT} to indicate that threads should wait indefinitely.
   * </p>
   * 
   * <p>
   * <b>Note:</b> HTTP connections are not acquired from the pool until a command is executed.
   * Setting this parameter has no effect on the call to {@link #getConnection(Credentials)}; that
   * method always return immediately. Instead, setting this parameter affects calls to
   * {@link Command#execute()}; the observed timeout on that method can be as large as the sum
   * of this connection pool acquire timeout plus the value of {@link Command#getTimeout()}.
   * </p>
   * 
   * @param seconds The maximum amount of time, in seconds, that a command will wait for a connection
   *        to become available from the pool before giving up and throwing an exception.
   */
  public synchronized void setAcquireTimeout(int seconds) {
    if (httpClient != null) {
      throw new IllegalStateException("Cannot set the connection pool acquire timeout after it is in use.");
    }
    this.acquireTimeout = seconds;
  }

  /** Gets the (re-usable) HTTP client backing this data source, creating it if necessary. */
  private synchronized HttpClient getClient(boolean create) {
    if (httpClient == null && create) {
      httpClient = createPooledClient();
    }
    return httpClient;
  }
  
  /**
   * Creates a new thread-safe HTTP connection pool for use with a data source.
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
    ConnManagerParams.setTimeout(httpParams, acquireTimeout * 1000);
    return new DefaultHttpClient(ccm, httpParams);
  }
}
