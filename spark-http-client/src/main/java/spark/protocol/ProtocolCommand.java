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

import java.io.IOException;
import java.net.URL;

import org.apache.http.client.HttpClient;

import spark.api.Result;
import spark.api.Solutions;
import spark.api.Triples;
import spark.api.exception.SparqlException;
import spark.spi.BaseCommand;

/**
 * A SPARQL API Command for executing commands to the SPARQL endpoint.
 */
public class ProtocolCommand extends BaseCommand {
  
  public enum ResultType {
    SELECT(Solutions.class),
    ASK(Result.class), // TODO change this when a BooleanResult is added.
    GRAPH(Triples.class);
    
    private final Class<? extends Result> resultClass;
    private ResultType(Class<? extends Result> resultClass) {
      this.resultClass = resultClass;
    }
    
    /** Gets the {@link Result} interface expected for results of this type. */
    public Class<? extends Result> getResultClass() {
      return resultClass;
    }
  }
  
  ProtocolCommand(ProtocolConnection connection, String command) {
    super(connection, command);
  }
  
  @Override
  public Result execute() {
    return execute(null);
  }

  @Override
  public boolean executeAsk() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Solutions executeQuery() throws SparqlException {
    return (Solutions)execute(ResultType.SELECT);
  }
  
  @Override
  public void cancel() {
    // TODO Auto-generated method stub

  }
  
  private Result execute(ResultType cmdType) throws SparqlException {
    HttpClient client = ((ProtocolConnection)getConnection()).getHttpClient();
    URL url = ((ProtocolDataSource)getConnection().getDataSource()).getUrl();
    try {
      return new SparqlCall(client, this, url).execute();
    } catch (IOException e) {
      throw new SparqlException("Error executing SPARQL protocol request", e);
    }
  }
}
