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
import java.util.List;

import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.junit.Assert;
import org.junit.Test;

import sherpa.protocol.SherpaServer;
import sherpa.server.DummySherpaServer;

public class TestQueryProtocolIterator {

  public void helpTestProtocolIterator(int resultRows) throws Exception {
    DummySherpaServer server = new DummySherpaServer(resultRows);
    InetSocketAddress serverAddress = server.getAddress();
    
    try {
      Transceiver tr = new SaslSocketTransceiver(serverAddress);
      SpecificRequestor requestor = new SpecificRequestor(SherpaServer.class, tr);
      SherpaServer queryApi = SpecificRequestor.getClient(SherpaServer.class, requestor);
      QueryManager protocol = new QueryManager(queryApi);

      protocol.query("fake command",null,null);      
      
      int counter = 0;
      for(List<Object> row : protocol) {
        Assert.assertNotNull(row);
        counter++;
      }
            
      System.out.println("Read " + counter + " rows");
      Assert.assertEquals(resultRows, counter);
      
    } finally {
      server.shutdown();
    }
  }

  @Test
  public void testIterator() throws Exception {
    helpTestProtocolIterator(0);
    helpTestProtocolIterator(1);
    helpTestProtocolIterator(10);
    helpTestProtocolIterator(1100);
  }

  @Test
  public void testRepeatOnTransceiver() throws Throwable {
    int resultRows = 15;
  
    DummySherpaServer server = new DummySherpaServer(resultRows);
    InetSocketAddress serverAddress = server.getAddress();
    
    try {
      Transceiver tr = new SaslSocketTransceiver(serverAddress);
      SpecificRequestor requestor = new SpecificRequestor(SherpaServer.class, tr);
      SherpaServer queryApi = SpecificRequestor.getClient(SherpaServer.class, requestor);

      for(int i=0; i<3; i++) {
        //System.out.println("\nrunning command " + i);
        
        QueryManager protocol = new QueryManager(queryApi);
        protocol.query("fake command",null,null);      

        int counter = 0;
        for(List<Object> row : protocol) {
          Assert.assertNotNull(row);
          counter++;
        }
              
        //System.out.println("Read " + counter + " rows");
        Assert.assertEquals(resultRows, counter);

      }
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    } finally {
      server.shutdown();
    }    
  }
}
  
