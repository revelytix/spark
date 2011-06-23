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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.util.Utf8;
import org.junit.Assert;
import org.junit.Test;

import sherpa.server.DummyQueryResponder;
import sherpa.server.DummySherpaServer;
import spark.api.Command;
import spark.api.Connection;
import spark.api.DataSource;
import spark.api.Solutions;
import spark.api.credentials.NoCredentials;
import spark.api.rdf.RDFNode;
import sherpa.protocol.QueryRequest;
import sherpa.protocol.QueryResponse;


public class TestSherpaClient {

  public void helpTestQueryCursor(int resultRows, int batchSize) {
    DummySherpaServer server = new DummySherpaServer(resultRows);
    InetSocketAddress serverAddress = server.getAddress();

    try {
      DataSource ds = new SHPDataSource(serverAddress.getHostName(),
          serverAddress.getPort());
      Connection conn = ds.getConnection(NoCredentials.INSTANCE);
      Command command = conn
          .createCommand("SELECT ?x ?y WHERE { this should be a real query but the test doesn't actually do anything real.");
      ((SHPCommand) command).setBatchSize(batchSize);
      Solutions solutions = command.executeQuery();

      int counter = 0;
      while (solutions.next()) {
        List<RDFNode> tuple = solutions.getSolutionList();
        Assert.assertNotNull(tuple);
        counter++;
      }

      System.out.println("Read " + counter + " rows");
      Assert.assertEquals(resultRows, counter);

    } finally {
      server.shutdown();
    }
  }

  @Test
  public void testCursor() {
    helpTestQueryCursor(0, 10);
    helpTestQueryCursor(1, 10);
    helpTestQueryCursor(9, 10);
    helpTestQueryCursor(10, 10);
    helpTestQueryCursor(11, 10);
    helpTestQueryCursor(20, 10);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTimeoutPassedDown() {
    final List<Object> results = new ArrayList<Object>();    
    DummySherpaServer server = new DummySherpaServer(
        new DummyQueryResponder(10) {
          public QueryResponse query(QueryRequest query)
              throws AvroRemoteException {
            results.add(query.properties);
            return super.query(query);
          }
        });
    InetSocketAddress serverAddress = server.getAddress();
    
    try {
      DataSource ds = new SHPDataSource(serverAddress.getHostName(), serverAddress.getPort());
      Connection conn = ds.getConnection(NoCredentials.INSTANCE);
      Command command = conn.createCommand("SELECT ?x ?y WHERE { this should be a real query but the test doesn't actually do anything real. }");
      command.setTimeout(1234);
      command.executeQuery();
      
      // Kind of tricky here - the keys and values are now Avro Utf8 instances which don't compare equal to Strings
      Map<CharSequence,CharSequence> serverProps = (Map<CharSequence,CharSequence>)results.get(0);
      Assert.assertEquals(new Utf8("1234"), serverProps.get(new Utf8(QueryManager.TIMEOUT)));
      
    } finally {
      server.shutdown();
    }

  }
}
