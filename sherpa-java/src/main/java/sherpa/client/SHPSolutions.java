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
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sherpa.client.QueryExecution;
import sherpa.protocol.IRI;
import spark.api.Solutions;
import spark.api.exception.SparqlException;
import spark.api.rdf.BlankNode;
import spark.api.rdf.Literal;
import spark.api.rdf.NamedNode;
import spark.api.rdf.RDFNode;
import spark.api.uris.XsdTypes;
import spark.spi.BaseResults;
import spark.spi.rdf.NamedNodeImpl;
import spark.spi.rdf.TypedLiteralImpl;

public class SHPSolutions extends BaseResults implements Solutions {

  private final QueryExecution query;
  
  /**
   * Construct an SHPSolutions where 
   * @param command
   * @param query
   */
  public SHPSolutions(SHPCommand command, QueryExecution query) {
    super(command);
    this.query = query;    
  }

  @Override
  public RDFNode getBinding(String variable) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BlankNode getBlankNode(String variable) throws SparqlException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean getBoolean(String variable) throws SparqlException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Date getDateTime(String variable) throws SparqlException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double getDouble(String variable) throws SparqlException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public float getFloat(String variable) throws SparqlException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getInt(String variable) throws SparqlException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public BigInteger getInteger(String variable) throws SparqlException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Literal getLiteral(String variable) throws SparqlException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NamedNode getNamedNode(String variable) throws SparqlException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, RDFNode> getResult() {
    // TODO Auto-generated method stub
    return null;
  }

  private RDFNode toNode(Object value) {
    if(value instanceof RDFNode) {
      return (RDFNode) value;
    } else if(value instanceof IRI) {
      return new NamedNodeImpl(URI.create(((IRI)value).iri.toString()));
    } else if(value instanceof Integer) {
      return new TypedLiteralImpl(""+value, XsdTypes.INT);
    } else {
      throw new SparqlException("Can't convert value to RDFNode. Type: " + value.getClass().getName());
    }
  }
  
  @Override
  public List<RDFNode> getSolutionList() {
    List<Object> rowData = query.getRow();
    
    if(rowData != null) {
      List<RDFNode> row = new ArrayList<RDFNode>();
  
      for(Object value : rowData) {
        row.add(toNode(value));
      }    
      return row;
      
    } else {
      return null;
    }
  }

  @Override
  public String getString(String variable) throws SparqlException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public URI getURI(String variable) throws SparqlException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getVariables() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isBound(String variable) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Iterator<Map<String, RDFNode>> iterator() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getRow() {
    return this.query.getCursor();
  }

  @Override
  public boolean isAfterLast() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isBeforeFirst() {
    return this.query.getCursor() == 0;
  }

  @Override
  public boolean isFirst() {
    return this.query.getCursor() == 1;
  }

  @Override
  public boolean isLast() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean next() {
    return this.query.incrementCursor();
  }

  @Override
  public void close() throws IOException {
    super.close();
    this.query.close();
  }

}
