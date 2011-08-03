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
 * A sparql API Connection connected to a SPARQL endpoint.
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

  HttpClient getHttpClient() {
    return httpClient;
  }
}
