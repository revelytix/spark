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

import org.apache.http.client.HttpClient;

import spark.api.Connection;
import spark.api.Credentials;
import spark.api.ServiceDescription;
import spark.spi.BaseConnection;

/**
 * <p>
 * A SPARQL API Connection connected to a SPARQL endpoint. Ideally we would like to maintain a
 * low-level HTTP connection in this class for re-use by all commands originating from this
 * connection. However, we've made the design decision to use the Apache {@link HttpClient}
 * for executing HTTP requests because of the long list of features which it automatically provides
 * (redirect handling, authentication, proxying, connection pooling, etc). The HttpClient gets an
 * HTTP connection from the pool for each request that is executed, and releases it when the 
 * request is complete.
 * </p>
 * 
 * <p>
 * The down-side of this design choice is that <tt>ProtocolConnection</tt> instances do not have
 * dedicated dedicated low-level HTTP connections; the ability of one command created on this
 * ProtocolConnection to obtain and use an HTTP connection has no effect on the ability of
 * subsequent commands created on the same ProtocolConnection to obtain an HTTP connection. The
 * up-side, other than richness of features, is that an idle ProtocolConnection instance does not
 * occupy an open HTTP connection, and the HTTP connection is free to be used by other
 * ProtocolConnection instances.
 * </p>
 */
public class ProtocolConnection extends BaseConnection implements Connection {

  /** The HTTP client which is shared by all connections originating from the parent data source. */
  private final HttpClient httpClient;
  
  ProtocolConnection(ProtocolDataSource dataSource, HttpClient httpClient, Credentials creds) {
    super(dataSource);
    if (httpClient == null) throw new IllegalArgumentException("Missing HTTP client.");
    this.httpClient = httpClient;
    // TODO: something with creds
  }
  
  @Override
  public ProtocolCommand createCommand(String commandString) {
    return new ProtocolCommand(this, commandString);
  }

  @Override
  public ServiceDescription getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

  /** Gets the shared HTTP client backing this connection. */
  HttpClient getHttpClient() {
    return httpClient;
  }
}
