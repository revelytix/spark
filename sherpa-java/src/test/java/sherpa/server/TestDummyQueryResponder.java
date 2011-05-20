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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import sherpa.protocol.DataRequest;
import sherpa.protocol.DataResponse;


public class TestDummyQueryResponder {

  private void testDataResponseCount(int start, int size, int totalRowCount, int expectedCount, boolean expectedMore) throws Exception {
    DummyQueryResponder responder = new DummyQueryResponder(totalRowCount);
    DataRequest dataRequest = new DataRequest();
    dataRequest.queryId = "a";
    dataRequest.startRow = start;
    dataRequest.maxSize = size;
    DataResponse response = responder.data(dataRequest);
    
    assertEquals(dataRequest.queryId, response.queryId);
    assertEquals(dataRequest.startRow, response.startRow);
    assertEquals(expectedCount, response.data.size());    
    assertEquals(expectedMore, response.more);
  }
  
  @Test
  public void testNoRows() throws Exception {
    testDataResponseCount(1, 100, 0, 0, false);
  }

  @Test
  public void testLessThanOneBatch() throws Exception {
    testDataResponseCount(1, 10, 5, 5, false);
  }
  
  @Test
  public void testMoreThanOneBatch() throws Exception {
    testDataResponseCount(1, 10, 25, 10, true);
    testDataResponseCount(11, 10, 25, 10, true);
    testDataResponseCount(21, 10, 25, 5, false);    
    testDataResponseCount(11, 10, 20, 10, false);
  }

}
