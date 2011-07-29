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
package sherpa.client;

/**
 * Represents a Solutions implementation.
 */
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sherpa.protocol.BNode;
import sherpa.protocol.IRI;
import sherpa.protocol.PlainLiteral;
import sherpa.protocol.TypedLiteral;
import spark.api.Solutions;
import spark.api.rdf.RDFNode;
import spark.spi.Conversions;
import spark.spi.StreamingSolutions;
import spark.spi.rdf.BlankNodeImpl;
import spark.spi.rdf.NamedNodeImpl;
import spark.spi.rdf.PlainLiteralImpl;
import spark.spi.rdf.TypedLiteralImpl;

public class SHPSolutions extends StreamingSolutions implements Solutions {

  private final QueryExecution query;
  
  /**
   * Construct an SHPSolutions where 
   * @param command
   * @param query
   */
  public SHPSolutions(SHPCommand command, QueryExecution query) {
    super(command, query.getVars());
    if (vars == null) throw new IllegalStateException("SHPSolutions constructed with un-initialized QueryExecution");
    this.query = query;    
  }

  @Override
  protected Map<String, RDFNode> fetchNext() {
    List<Object> rowData = null;
    if (query.incrementCursor()) rowData = query.getRow();
    if (rowData != null) {
      if (rowData.size() != vars.size()) throw new IllegalStateException("Mis-matched variable and data list");
      Map<String, RDFNode> result = new HashMap<String, RDFNode>();
      for (int i = 0; i < vars.size(); i++) {
        result.put(vars.get(i), toNode(rowData.get(i)));
      }
      return result;
    }
    return null;
  }

  /** Convert a protocol data object to an RDFNode. */
  private static RDFNode toNode(Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof RDFNode) {
      return (RDFNode) value;
    } else if (value instanceof IRI) {
      return new NamedNodeImpl(URI.create(((IRI)value).iri.toString()));
    } else if (value instanceof PlainLiteral) {
      PlainLiteral pl = (PlainLiteral)value;
      String lang = pl.language != null ? pl.language.toString() : null;
      return new PlainLiteralImpl(pl.lexical.toString(), lang);
    } else if (value instanceof TypedLiteral) {
      TypedLiteral tl = (TypedLiteral)value;
      return new TypedLiteralImpl(tl.lexical.toString(), URI.create(tl.datatype.toString()));
    } else if (value instanceof BNode) {
      return new BlankNodeImpl(((BNode)value).label.toString());
    } else {
      // Sherpa passes strings as something other than java.lang.String, so convert.
      if (value instanceof CharSequence) {
        value = value.toString();
      }
      // What's left is a primitive Java type, convert it to an XSD-typed literal.
      // Falls back to xsd:anySimpleType for unrecognized classes
      return Conversions.toLiteral(value);
    }
  }
  
  @Override
  public boolean isLast() {
    return this.query.isLast();
  }

  @Override
  public void close() throws IOException {
    super.close();
    this.query.close();
  }

}
