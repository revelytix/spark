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

import org.apache.http.HttpResponse;

import spark.api.Result;
import spark.api.Solutions;
import spark.api.Triples;
import spark.api.exception.SparqlException;
import spark.protocol.parser.ResultFactory;
import spark.spi.BaseCommand;

/**
 * A SPARQL API Command for executing commands to the SPARQL endpoint.
 */
public class ProtocolCommand extends BaseCommand {
  
  /** Enumeration of possible query result types. */
  public enum ResultType {
    /** SPARQL SELECT query results. */
    SELECT(Solutions.class),
    /** SPARQL ASK query results. */
    ASK(Result.class), // TODO change this when a BooleanResult is added.
    /** SPARQL DESCRIBE or CONSTRUCT query results (both return graphs). */
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
  
  /** Media content type for content negotiation. */
  private String contentType = null;
  
  /** Create a SPARQL protcol command to execute over the given connection. */
  ProtocolCommand(ProtocolConnection connection, String command) {
    super(connection, command);
  }
  
  /** @return the media content type to specify when doing content negotiation for this request. */
  public String getContentType() {
    return contentType;
  }

  /**
   * Sets the media content type to use when doing content negotiation for this request.
   * @param contentType The MIME content type.
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
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
  
  /** Executes the request, and parses the response. */
  private Result execute(ResultType cmdType) throws SparqlException {
    String mimeType = contentType;
    
    // Validate the user-supplied MIME type.
    if (mimeType != null && !ResultFactory.supports(mimeType, cmdType)) {
      System.out.println("Requested MIME content type '" + mimeType + 
          "' does not support expected response type: " + cmdType);
      mimeType = null;
    }
    
    // Get the default MIME type to request
    if (mimeType == null) {
      mimeType = ResultFactory.getDefaultMediaType(cmdType);
    }
    
    StringBuilder sb = new StringBuilder("Executing SPARQL protocol request ");
    if (mimeType != null) {
      sb.append("for content type '").append(mimeType).append("' ");
    } else {
      sb.append("for unknown content type ");
    }
    if (cmdType != null) {
      sb.append("with expected results of type ").append(cmdType).append(".");
    } else {
      sb.append("with unknown expected result type.");
    }
    System.out.println(sb.toString());
    
    HttpResponse response = SparqlCall.executeRequest(this, mimeType);
    return ResultFactory.getResult(this, response, cmdType);
  }
}
