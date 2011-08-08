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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  
  private static final Logger logger = LoggerFactory.getLogger(ProtocolCommand.class);
  
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
  
  /** Lock to protecte the request being executed. */
  private final Lock requestLock = new ReentrantLock();
  
  /** The request being executed for this command. */
  private HttpUriRequest request = null;
  
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
    requestLock.lock();
    try {
      if (request != null) request.abort();
    } finally {
      requestLock.unlock();
    }
  }
  
  /** Releases the currently executing request, freeing this command to be re-executed if desired. */
  public void release() {
    requestLock.lock();
    try {
      this.request = null;
    } finally {
      requestLock.unlock();
    }
  }
  
  /** Sets the currently executing request. */
  void setRequest(HttpUriRequest request) {
    requestLock.lock();
    try {
      if (this.request != null) {
        throw new SparqlException("Command is already executing a request.");
      }
      this.request = request;
    } finally {
      requestLock.unlock();
    }
  }
  
  /** Executes the request, and parses the response. */
  private Result execute(ResultType cmdType) throws SparqlException {
    String mimeType = contentType;
    
    // Validate the user-supplied MIME type.
    if (mimeType != null && !ResultFactory.supports(mimeType, cmdType)) {
      logger.warn("Requested MIME content type '{}' does not support expected response type: {}", mimeType, cmdType);
      mimeType = null;
    }
    
    // Get the default MIME type to request
    if (mimeType == null) {
      mimeType = ResultFactory.getDefaultMediaType(cmdType);
    }
    
    if (logger.isDebugEnabled()) {
      logRequest(cmdType, mimeType);
    }
    
    try {
      HttpResponse response = SparqlCall.executeRequest(this, mimeType);
      return ResultFactory.getResult(this, response, cmdType);
    } catch (Throwable t) {
      release();
      throw SparqlException.convert("Error creating SPARQL result from server response", t);
    }
  }
  
  /** Log the enpoint URL and request parameters. */
  private void logRequest(ResultType cmdType, String mimeType) {
    StringBuilder sb = new StringBuilder("Executing SPARQL protocol request ");
    sb.append("to endpoint <").append(((ProtocolDataSource)getConnection().getDataSource()).getUrl()).append("> ");
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
    logger.debug(sb.toString());
  }
}
