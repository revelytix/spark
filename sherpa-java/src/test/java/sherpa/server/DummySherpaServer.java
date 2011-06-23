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
package sherpa.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.avro.ipc.SaslSocketServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;

import spark.api.exception.SparqlException;
import sherpa.protocol.Query;

public class DummySherpaServer extends SpecificResponder {

  private final Server server;
  
  public DummySherpaServer(int rows) {
    this(new DummyQueryResponder(rows));
  }
  
  public DummySherpaServer(Query responder) {
    super(Query.class, responder);

    try {
      server = new SaslSocketServer(this, new InetSocketAddress(InetAddress.getLocalHost(), 0));
      server.start();
    } catch(IOException e) {
      throw new SparqlException("Error starting server.");
    }
  }
  
  public InetSocketAddress getAddress() {
    try {
      return new InetSocketAddress(InetAddress.getLocalHost(), this.server.getPort());
    } catch(UnknownHostException e) {
      throw new SparqlException(e);
    }
  }
  
  public void shutdown() {
    server.close();
  }
  
  public static void main(String args[]) throws Exception {
    int rows = 100;
    if(args.length > 0) {
      rows = Integer.parseInt(args[0]);
    }
    DummySherpaServer s = new DummySherpaServer(rows);
    InetSocketAddress address = s.getAddress();
    System.out.println("Started server at: " + address.getHostName() + ":" + address.getPort());    
    Thread.currentThread().join();
  }
}
