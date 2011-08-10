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
package spark.protocol.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.api.Command;
import spark.api.Result;
import spark.api.exception.SparqlException;
import spark.protocol.ProtocolCommand.ResultType;
import spark.spi.BooleanResultImpl;

/**
 * <p>This parser exists to handle responses for ASK queries from DBPedia (and other Virtuoso servers)
 * where the Accept: header was not set by the client request. In this situation, DBPedia will 
 * return a response of type <tt>text/html</tt> with content consisting of either the string "true"
 * or the sting "false".</p>
 * 
 * <p><b>Note:</b> The <tt>text/html</tt> content type <i>should not</i> be used as the requested
 * content type when making client requests, as that could cause the server to return an HTML page
 * formatted for human consumption which will not lend itself well to parsing as a result set.
 * This content type is intended solely to handle default responses to ASK queries against DBPedia.</p>
 * 
 * @author Alex Hall
 * @created Aug 10, 2011
 */
public class HTMLParser implements ResultParser {

  private static final Logger logger = LoggerFactory.getLogger(HTMLParser.class);
  
  /** Assume UTF-8 encoding. */
  private static final String UTF8 = "UTF-8";
  
  /**
   * Number of characters to read from the beginning of the stream; should just be 'true' or
   * 'false' but allow some extra room for whitespace padding.
   */
  private static final int BUFFER_LEN = 10;
  
  /* (non-Javadoc)
   * @see spark.protocol.parser.ResultParser#parse(spark.api.Command, java.io.InputStream, spark.protocol.ProtocolCommand.ResultType)
   */
  @Override
  public Result parse(Command cmd, InputStream input, ResultType type) throws SparqlException {
    BufferedReader br = null;
    try {
      // Expected type should already be validated by ResultFactory, check anyways.
      if (type != null && type != ResultType.ASK) {
        throw new SparqlException("Unexpected result type; expected " + type + " but found ASK.");
      }
      
      char[] buf = new char[BUFFER_LEN];
      
      // TODO: Find the actual encoding from the server response, if available.
      br = new BufferedReader(new InputStreamReader(input, UTF8));
      int len = fill(buf, br);
      
      String s = new String(buf, 0, len).trim();
      logger.debug("Read '{}' from text/html ASK result", s);
      
      boolean result;
      if (s.equalsIgnoreCase("true")) {
        result = true;
      } else if (s.equalsIgnoreCase("false")) {
        result = false;
      } else {
        logger.warn("Unexpected boolean value read from text/html ASK result: '{}'", s);
        result = false;
      }
      
      if (logger.isWarnEnabled() && br.read() >= 0) {
        logger.warn("Unexpected input found after boolean value");
      }
      
      return new BooleanResultImpl(cmd, result);
    } catch (IOException e) {
      throw new SparqlException("Error reading from server input", e);
    } finally {
      try {
        if (br != null) br.close();
        else input.close();
      } catch (IOException e) {
        // Don't re-throw because that could mask another exception (or prevent an otherwise usable
        // result from being returned).
        logger.warn("Error closing server input", e);
      }
    }
  }

  /**
   * Repeatedly invokes the reader until the buffer is filled or EOF is reached. Even BufferedReader
   * doesn't guarantee that a given call to read() will get all the requested input, and avoid
   * readLine() in case of malformed input.
   * @param buf The character buffer to fill
   * @param r The reader to read from
   * @return The number of characters actually written to the buffer.
   */
  private static int fill(char[] buf, Reader r) throws IOException {
    int offset = 0;
    int read = 0;
    do {
      read = r.read(buf, offset, buf.length - offset);
      if (read > 0) offset += read;
    } while (offset < buf.length && read >= 0);
    return offset;
  }
}
