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

import static spark.protocol.ProtocolCommand.ResultType.ASK;
import static spark.protocol.ProtocolCommand.ResultType.GRAPH;
import static spark.protocol.ProtocolCommand.ResultType.SELECT;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import spark.api.Command;
import spark.api.Result;
import spark.api.exception.SparqlException;
import spark.protocol.ProtocolCommand.ResultType;

/**
 * Factory for creating SPARQL {@link Result} objects from SPARQL HTTP Protocol responses.
 * Borrows heavily from Paul Gearon's ResultBuilder.java implementation in Knoodl.
 * 
 * @author Alex Hall
 * @author Paul Gearon
 * @created Aug 3, 2011
 */
public class ResultFactory {

  private static final Map<String,ResponseFormat> mimeFormats = new HashMap<String,ResponseFormat>();
  
  /**
   * Enumeration of expected response formats. A format is defined by a combination of a parser,
   * one or more supported result types, and one or more MIME types.
   */
  enum ResponseFormat {
    SPARQL_XML(new XMLResultsParser(), EnumSet.of(SELECT, ASK), "application/sparql-results+xml"),
    SPARQL_JSON(new UnsupportedFormatParser("SPARQL JSON results"), EnumSet.of(SELECT, ASK), "application/sparql-results+json"),
    RDF_XML(new UnsupportedFormatParser("RDF/XML"), EnumSet.of(GRAPH), "application/rdf+xml"),
    RDF_TURTLE(new UnsupportedFormatParser("RDF Turtle"), EnumSet.of(GRAPH), "text/turtle", "text/n3", "text/rdf+n3", "application/n3");
    
    private final ResultParser parser;
    private final EnumSet<ResultType> resultTypes;
    private final String mimeText;
    
    private ResponseFormat(ResultParser parser, EnumSet<ResultType> resultTypes, String... mimeTexts) {
      this.parser = parser;
      this.resultTypes = resultTypes;
      
      String mimeText = null;
      for (String txt : mimeTexts) {
        if (mimeText == null) mimeText = txt; // Take the first MIME type as the "preferred" one.
        mimeFormats.put(txt, this);
      }
      this.mimeText = mimeText;
    }
  }
  
  /** Define default response formats for each result type. */
  private static final Map<ResultType,ResponseFormat> defaultTypeFormats = new HashMap<ResultType,ResponseFormat>();
  static {
    defaultTypeFormats.put(SELECT, ResponseFormat.SPARQL_XML);
    defaultTypeFormats.put(ASK, ResponseFormat.SPARQL_XML);
    defaultTypeFormats.put(GRAPH, ResponseFormat.RDF_TURTLE);
  }
  
  /** System-wide default format to use if the result type is unknown. */
  private static final ResponseFormat DEFAULT_FORMAT = ResponseFormat.SPARQL_XML;
  
  /** Placeholder for un-implemented result formats; throws unsupported operation exceptions. */
  private static class UnsupportedFormatParser implements ResultParser {
    private final String format;
    UnsupportedFormatParser(String format) { this.format = format; }
    
    /** Always throws an exception. */
    @Override
    public Result parse(Command cmd, InputStream input, ResultType type) {
      throw new UnsupportedOperationException("Unsupported SPARQL result format: " + format);
    }
  }
  
  /**
   * Strips the parameters from the end of a mediaType description.
   * @param mediaType The text in a Content-Type header.
   * @return The content type string without any parameters.
   */
  private static final String stripParams(String mediaType) {
    int sc = mediaType.indexOf(';');
    if (sc >= 0) mediaType = mediaType.substring(0, sc);
    return mediaType;
  }
  
  /**
   * Determine whether the given media type supports the expected result type, i.e. don't request
   * RDF/XML if the user is expecting SELECT results.
   * @param mediaType The requested media type.
   * @param expectedType The expected result type.
   * @return <tt>false</tt> if the requested media type is unrecognized or does not support the
   *         requested result type, <tt>true</tt> otherwise.
   */
  public static boolean supports(String mediaType, ResultType expectedType) {
    if (mediaType == null) return true; // Assume the server will choose a reasonable media type.

    ResponseFormat format = mimeFormats.get(stripParams(mediaType));
    return (format != null && (expectedType == null || format.resultTypes.contains(expectedType)));
  }
  
  /**
   * Find a parser to handle the protocol response body based on the content type found in the response
   * and the expected result type specified by the user; if one or both fields is missing then
   * attempts to choose a sensible default.
   * @param mediaType The content type in the response, or null if none was given.
   * @param expectedType The expected response type indicated by the user, or 
   * @return
   */
  private static final ResultParser findParser(String mediaType, ResultType expectedType) {
    ResponseFormat format = null;
    
    // Prefer MIME type when choosing result format.
    if (mediaType != null) {
      mediaType = stripParams(mediaType);
      format = mimeFormats.get(mediaType);
      if (format == null) {
        System.out.println("Unrecognized media type: " + mediaType);
      } else {
        System.out.println("Using result format " + format + " for media type " + mediaType);
      }
    }
    
    // If MIME type was absent or unrecognized, choose default based on expected result type.
    if (format == null) {
      System.out.println("Unable to determine result format from media type");
      if (expectedType != null) {
        format = defaultTypeFormats.get(expectedType);
        System.out.println("Using default format " + format + " for expected result type " + expectedType);
      } else {
        format = DEFAULT_FORMAT;
        System.out.println("No expected type provided; using default format " + format);
      }
    }
    
    assert format != null:"Could not determine result format";
    
    // Validate that the chosen format can produce the expected result type.
    if (expectedType != null && !format.resultTypes.contains(expectedType)) {
      throw new SparqlException("Result format " + format + " does not support expected result type " + expectedType);
    }
    
    return format.parser;
  }
  
  /**
   * Creates a SPARQL {@link Result} object by parsing the given server response.
   * @param cmd The command which originated the request.
   * @param response The HTTP response from the SPARQL server.
   * @param expectedType The expected result type specified by the caller, or null if none given.
   * @return The parsed result object to return to the caller.
   * @throws SparqlException If the response from the server could not be parsed, or could not be
   *         converted to the expected result type.
   */
  public Result getResult(Command cmd, HttpResponse response, ResultType expectedType) throws SparqlException {
    HttpEntity entity = response.getEntity();
    if (entity == null) throw new SparqlException("No data in response from server");
    
    Header header = entity.getContentType();
    String mediaType = (header != null) ? header.getValue() : null;
    
    ResultParser parser = findParser(mediaType, expectedType);
    assert parser != null:"Could not find result parser";
    
    Result result = null;
    try {
      result = parser.parse(cmd, entity.getContent(), expectedType);
    } catch (IOException e) {
      throw new SparqlException("Error reading response from server", e);
    }
    
    if (result == null) {
      // Don't know how we got here, but parser should have returned a result or thrown an exception.
      throw new IllegalStateException("Could not parse result from server response.");
    }
    
    // Should never happen because the result format should have been validated against the expected
    // class when selecting the format to use for parsing, but check anyways.
    if (expectedType != null && !expectedType.getResultClass().isInstance(result)) {
      throw new IllegalStateException("Result parsed from server response (" +
          result.getClass().getName() + ") does not match expected result type (" + expectedType + ")");
    }
    
    return result;
  }
}
