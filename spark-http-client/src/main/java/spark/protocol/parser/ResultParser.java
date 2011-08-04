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

import java.io.IOException;
import java.io.InputStream;

import spark.api.Command;
import spark.api.Result;
import spark.protocol.ProtocolCommand.ResultType;

/**
 * Interface for SPARQL query result parsers.
 * 
 * @author Alex Hall
 * @created Aug 3, 2011
 */
public interface ResultParser {

  /**
   * Parse a SPARQL query result from the input stream taken from a server response.
   * @param cmd The command which originated the request.
   * @param input The input stream from the server.
   * @param expectedType The expected result type specified by the caller, or null if none specified.
   *        If an expected type is given, the parser should validate that it matches the actual
   *        result type found in the input and throw an exception if it doesn't. This is relevant e.g.
   *        for distinguishing between SELECT and ASK results in formats which support both.
   * @return The parsed SPARQL query result.
   * @throws IOException if an error occurred reading from the server input stream.
   */
  Result parse(Command cmd, InputStream input, ResultType expectedType) throws IOException;
}
