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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.avro.AvroRemoteException;

import sherpa.protocol.CancelRequest;
import sherpa.protocol.CloseRequest;
import sherpa.protocol.CloseResponse;
import sherpa.protocol.DataRequest;
import sherpa.protocol.DataResponse;
import sherpa.protocol.ErrorResponse;
import sherpa.protocol.IRI;
import sherpa.protocol.QueryRequest;
import sherpa.protocol.QueryResponse;
import sherpa.protocol.ReasonCode;
import sherpa.protocol.SherpaServer;

public class DummyQueryResponder implements SherpaServer {

  public final int rows;
  public List<String> messages = new ArrayList<String>();
  
  public DummyQueryResponder(int rows) {
    this.rows = rows;
  }
  
  public void record(Object... pairs) {
    StringBuilder str = new StringBuilder();
    int i = 0;
    while(i < pairs.length) {
      str.append(pairs[i++] + "=" + pairs[i++] + " ");
    }
    messages.add(str.toString());
  }
  
  @Override
  public QueryResponse query(QueryRequest query) throws AvroRemoteException,
      ErrorResponse {
  
    System.out.println("Server got query request for " + query.sparql);
    
    QueryResponse response = new QueryResponse();
    response.queryId = "1";
    response.vars = new ArrayList<CharSequence>();
    response.vars.add("x");
    response.vars.add("y");

    System.out.println("Server sending query response");
   
    record("Message", "query", "sparql", query.sparql, "params", query.parameters, "props", query.properties);
    return response;
  }

  private List<List<Object>> batch(int begin, int size) {
    List<List<Object>> tuples = new ArrayList<List<Object>>();
    for(int i=begin; i<begin+size; i++) {
      List<Object> tuple = new ArrayList<Object>();
      IRI iri = new IRI();
      iri.iri = "http://foobar.baz/this/uri/" + i;
      tuple.add(iri);
      tuple.add(i);
      tuples.add(tuple);
    }
    return tuples;
  }
  
  @Override
  public DataResponse data(DataRequest dataRequest) throws AvroRemoteException,
      ErrorResponse {
    
    System.out.println("Server got data request for " + dataRequest.startRow);
    record("Message", "data", "queryId", dataRequest.queryId, 
        "startRow", dataRequest.startRow, 
        "maxSize", dataRequest.maxSize);
    
    if(rows == 0) {
      DataResponse response = new DataResponse();
      response.queryId = dataRequest.queryId;
      response.startRow = 1;
      response.more = false;
      response.data = Collections.emptyList();
      System.out.println("Server sending empty response for 0 row result.");
      return response;
      
    } else if(dataRequest.startRow <= rows) {
      DataResponse response = new DataResponse();
      response.queryId = dataRequest.queryId;
      response.startRow = dataRequest.startRow;
      
      int size = dataRequest.maxSize;
      int last = dataRequest.startRow + size - 1;   // 1-based
      if(last > rows) {
        size = rows - dataRequest.startRow + 1;
      }
      response.data = batch(dataRequest.startRow, size);
      response.more = (response.startRow + size - 1) < rows;
      
      System.out.println("Server sending response for " + dataRequest.startRow + ".." + (dataRequest.startRow+response.data.size()-1));
      return response;
      
    } else {
      ErrorResponse response = new ErrorResponse();
      response.code = ReasonCode.Error;
      response.message = "Invalid request for rows outside the result set.";
      throw response;
    }
  }

  @Override
  public CloseResponse cancel(CancelRequest cancel) throws AvroRemoteException,
      ErrorResponse {
    
    record("Message", "cancel", "queryId", cancel.queryId);

    CloseResponse response = new CloseResponse();
    response.queryId = cancel.queryId;
    return response;
  }

  @Override
  public CloseResponse close(CloseRequest close) throws AvroRemoteException,
      ErrorResponse {

    record("Message", "close", "queryId", close.queryId);

    CloseResponse response = new CloseResponse();
    response.queryId = close.queryId;
    return response;
  }

}
