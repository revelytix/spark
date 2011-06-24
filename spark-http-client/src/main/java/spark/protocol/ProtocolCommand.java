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

import spark.api.Result;
import spark.api.Solutions;
import spark.api.exception.SparqlException;
import spark.spi.BaseCommand;
import spark.spi.SolutionSet;

/**
 * A SPARQL API Command for executing commands to the SPARQL endpoint.
 */
public class ProtocolCommand extends BaseCommand {
  
  ProtocolCommand(ProtocolConnection connection, String command) {
    super(connection, command);
  }
  
  @Override
  public Result execute() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean executeAsk() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Solutions executeQuery() {
    try {
      XMLResultSetParser sparqlResult = SparqlCall.execute(((ProtocolDataSource)getConnection().getDataSource()).getUrl(), getCommand());
      return new SolutionSet(this, sparqlResult.getVariables(), sparqlResult.getData());
    } catch(IOException e) {
      throw new SparqlException(e);
    }
  }

  @Override
  public void cancel() {
    // TODO Auto-generated method stub

  }
}
