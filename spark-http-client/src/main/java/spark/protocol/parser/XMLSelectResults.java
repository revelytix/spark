/*
 * Copyright 2010 Paul Gearon.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.protocol.parser;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static spark.protocol.parser.XMLResultsParser.Element.BINDING;
import static spark.protocol.parser.XMLResultsParser.Element.RESULT;
import static spark.protocol.parser.XMLResultsParser.Element.RESULTS;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import spark.api.Command;
import spark.api.Solutions;
import spark.api.exception.SparqlException;
import spark.api.rdf.RDFNode;
import spark.protocol.ProtocolCommand;
import spark.protocol.ProtocolResult;
import spark.protocol.parser.XMLResultsParser.Element;
import spark.spi.StreamingSolutions;
import spark.spi.rdf.BlankNodeImpl;
import spark.spi.rdf.NamedNodeImpl;
import spark.spi.rdf.PlainLiteralImpl;
import spark.spi.rdf.TypedLiteralImpl;

/**
 * Parses an XML stream for SPARQL results, returning them as solution mappings.
 * Based on StAX to allow for streaming large answers. The StAX cursor API is used
 * to make it easy to reuse the code from jSPARQLc.
 * 
 * Based on jSPARQLc, Copyright 2010 Paul Gearon.
 * http://code.google.com/p/jsparqlc/
 * jSPARQLc is licensed under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * @author Paul Gearon
 */
public class XMLSelectResults extends StreamingSolutions implements Solutions, ProtocolResult {

  /** The list of metadata links for this result set. */
  private final List<String> metadata;

  /** The XML reader and parser. */
  private final XMLStreamReader reader;

  private static final String VAR_NAME = "name";
  private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
  private static final String LANG = "lang";
  private static final String DATATYPE = "datatype";

  // Maintain a one-row lookahead to support isLast()
  private Map<String,RDFNode> nextRow;
  
  /**
   * Creates a results object from an already opened reader that has had the header parsed to determine
   * that this contains a SPARQL XML result set.
   * @param stream The InputStream returned from a SPARQL endpoint.
   * @param query The query that led to the results.
   * @throws SparqlException If there was an error accessing the XML stream.
   */
  XMLSelectResults(Command cmd, XMLStreamReader reader, List<String> columns, List<String> metadata) throws SparqlException {
    super(cmd, columns);
    this.reader = reader;
    this.metadata = Collections.unmodifiableList(metadata);
    this.nextRow = readNext();
  }

  /** @return The metadata. */
  public List<String> getMetadata() {
    return metadata;
  }

  @Override
  public void close() throws SparqlException, IOException {
    super.close();
    try {
      reader.close();
    } catch (XMLStreamException e) {
      throw new SparqlException("Error closing stream", e);
    } finally {
      Command c = getCommand();
      // Need this check because command can be null when testing the parser...
      if (c != null && c instanceof ProtocolCommand) {
        ((ProtocolCommand)c).release();
      }
    }
  }

  @Override
  public boolean isLast() {
    return currentRow != null && nextRow == null;
  }

  @Override
  protected Map<String, RDFNode> fetchNext() throws SparqlException {
    Map<String, RDFNode> row = nextRow;
    if (row != null) {
      nextRow = readNext();
    }
    return row;
  }

  /**
   * Parse the input stream to look for a result.
   * @return A new Row based on a single result from the results section.
   * @throws XMLStreamException There was an error reading the XML stream.
   * @throws SparqlException The XML was not valid SPARQL results.
   */
  protected Map<String,RDFNode> readNext() throws SparqlException {
    try {
      // read <result> or </results>
      int eventType = reader.nextTag();
      // if a closing element, then it should be </results>
      if (eventType == END_ELEMENT) {
        // already read the final result, so clean up and return nothing
        if (nameIs(RESULTS)) {
          cleanup();
          return null;
        }
        else throw new SparqlException("Bad element closure with: " + reader.getLocalName());
      }
      // we only expect a <result> here 
      testOpen(eventType, RESULT, "Expected a new result. Got :" +
          ((eventType == END_ELEMENT) ? "/" : "") + reader.getLocalName());

      Map<String,RDFNode> result = new HashMap<String,RDFNode>();
      // read <binding> list
      while ((eventType = reader.nextTag()) == START_ELEMENT && nameIs(BINDING)) {
        // get the name of the binding
        String name = reader.getAttributeValue(null, VAR_NAME);
        result.put(name, parseValue());
        testClose(reader.nextTag(), BINDING, "Single Binding not closed correctly");
      }

      // a non- <binding> was read, so it should have been a </result>
      testClose(eventType, RESULT, "Single Result not closed correctly");
      return result;
    } catch (XMLStreamException e) {
      throw new SparqlException("Error reading from XML stream", e);
    }
  }

  /**
   * Parses the value for a variable binding. The data is either a URI, a BNode, or a Literal
   * as per {@link http://www.w3.org/TR/rdf-sparql-XMLres/#vb-results}
   * @return The parsed RDFNode.
   * @throws SparqlException If there was a consistency error in the parsed data.
   * @throws XMLStreamException If there was an error accessing the XML.
   */
  RDFNode parseValue() throws SparqlException, XMLStreamException {
    if (reader.nextTag() != START_ELEMENT) throw new SparqlException("No value in variable binding");
    Element elt = Element.valueOf(reader.getLocalName().toUpperCase());
    try {
      switch (elt) {
      case URI:
        if (reader.next() != CHARACTERS) throw new SparqlException("Unexpected data in URI binding");
        return new NamedNodeImpl(new URI(reader.getText()));
      case BNODE:
        if (reader.next() != CHARACTERS) throw new SparqlException("Unexpected data in BNode binding");
        return new BlankNodeImpl(reader.getText());
      case LITERAL:
        String dt = reader.getAttributeValue(null, DATATYPE);
        URI datatype = (dt == null) ? null : new URI(dt);
        String lang = reader.getAttributeValue(XML_NS, LANG);
        if (reader.next() != CHARACTERS) throw new SparqlException("Unexpected data in Literal binding");
        String lex = reader.getText();
        return (datatype != null) ? new TypedLiteralImpl(lex, datatype) : new PlainLiteralImpl(lex, lang);
      default:
        throw new SparqlException("Unexpected binding value: " + reader.getLocalName());
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();
      throw new SparqlException("Bad URI in binding: " + e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
      throw new SparqlException(t);
    } finally {
      testClose(reader.nextTag(), elt, "Bad close of value in binding");
    }
  }

  /**
   * Steps over the end of the XML. Invalid data will not cause an exception to be thrown, but the
   * error will be recorded.
   * @throws XMLStreamException If there was an error accessing the XML.
   */
  private void cleanup() throws XMLStreamException {
    XMLResultsParser.cleanup(reader);
  }

  /**
   * Convenience method to test if the read element is an opening tag with the correct name.
   * @param type The kind of element to test.
   * @param elt The element name.
   * @param message The message to use in case of error.
   * @throws SparqlException Thrown if the type is not a START_ELEMENT or the name is not the required name.
   */
  protected final void testOpen(int type, Element elt, String message) throws SparqlException {
    XMLResultsParser.testOpen(reader, type, elt, message);
  }

  /**
   * Convenience method to test if the read element is a closing tag with the correct name.
   * @param type The kind of element to test.
   * @param elt The element name.
   * @param message The message to use in case of error.
   * @throws SparqlException Thrown if the type is not an END_ELEMENT or the name is not the required name.
   */
  protected final void testClose(int type, Element elt, String message) throws SparqlException {
    XMLResultsParser.testClose(reader, type, elt, message);
  }

  /**
   * Convenience method to test if the current local name is the same as an expected element.
   * @param elt The element to test against.
   * @return <code>true</code> iff the current local name is the same as the element name.
   */
  protected final boolean nameIs(Element elt) {
    return XMLResultsParser.nameIs(reader, elt);
  }

}
