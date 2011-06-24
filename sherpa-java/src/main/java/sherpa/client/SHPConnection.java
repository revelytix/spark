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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;

import sherpa.client.QueryExecution;
import sherpa.protocol.SherpaServer;
import spark.api.Command;
import spark.api.Connection;
import spark.api.DataSource;
import spark.api.ServiceDescription;
import spark.api.exception.SparqlException;
import spark.spi.BaseConnection;

public class SHPConnection extends BaseConnection implements Connection {
  
  private Transceiver transceiver;
  private SpecificRequestor requestor;
  private SherpaServer server;

  public SHPConnection(DataSource dataSource) {
    super(dataSource);
    
    SHPDataSource shpDS = (SHPDataSource) dataSource;
    String host = shpDS.getHost();
    int port = shpDS.getPort();
    
    InetSocketAddress address;
    try {
      address = new InetSocketAddress(InetAddress.getByName(host), port);
    } catch(UnknownHostException e) {
      throw new SparqlException("Invalid host: " + host, e);
    }

    try {
      transceiver = new SaslSocketTransceiver(address);
    } catch(IOException e) {
      throw new SparqlException("Socket error connecting client", e);
    }
    
    try {
      requestor = new SpecificRequestor(SherpaServer.class, transceiver);
    } catch (IOException e) {
      throw new SparqlException("Error creating client requestor", e);
    }

    try {
      server = SpecificRequestor.getClient(SherpaServer.class, requestor);
    } catch (IOException e) {
      throw new SparqlException("Unable to create client data proxy.", e);
    }

  }
  
  @Override
  public Command createCommand(String commandString) {
    return new SHPCommand(this, commandString, new QueryExecution(server));
  }

  @Override
  public ServiceDescription getDescription() {
    // TODO Auto-generated method stub
    return null;
  }

}
