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
package spark.spi;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import spark.api.Command;
import spark.api.Solutions;
import spark.api.exception.SparqlException;
import spark.api.rdf.BlankNode;
import spark.api.rdf.Literal;
import spark.api.rdf.NamedNode;
import spark.api.rdf.RDFNode;

public class SolutionSet extends BaseResults implements Solutions {
  
  private final List<Map<String,RDFNode>> data;
  private final List<String> vars;
  
  private static final int BEFORE_FIRST = -1;
  private static final int FIRST = 0;
  private volatile int cursor = BEFORE_FIRST;
  
  public SolutionSet(Command command, List<String> vars, List<Map<String,RDFNode>> data) {
    super(command);
    this.vars = vars;
    this.data = data;
  }
  
  @Override
  public boolean isBound(String variable) {
    return getSolution().get(variable) != null;
  }

  @Override
  public Map<String, RDFNode> getSolution() {
    return data.get(cursor);
  }

  @Override
  public List<String> getVariables() {
    return this.vars;
  }
  
  @Override
  public boolean isAfterLast() {
    return cursor >= data.size();
  }

  @Override
  public boolean isBeforeFirst() {
    return cursor == BEFORE_FIRST;
  }

  @Override
  public boolean isFirst() {
    return cursor == FIRST;
  }

  @Override
  public boolean isLast() {
    return data.size() > 0 && cursor == (data.size() - 1);
  }

  @Override
  public boolean next() {
    cursor++;
    return cursor < data.size();
  }

  @Override
  public int getRow() {
    return cursor+1;
  }

  @Override
  public List<RDFNode> getSolutionList() {
    Map<String,RDFNode> solution = data.get(cursor);
    List<RDFNode> solutionList = new ArrayList<RDFNode>(vars.size());
    
    for(String var : vars) {
      solutionList.add(solution.get(var));
    }
    
    return solutionList;
  }

  @Override
  public Iterator<Map<String, RDFNode>> iterator() {
    return new SolutionIterator();
  }

  
  private class SolutionIterator implements Iterator<Map<String,RDFNode>> {
    int iterCursor = -1;
    
    @Override
    public boolean hasNext() {
      return (iterCursor + 1) < data.size();
    }

    @Override
    public Map<String,RDFNode> next() {
      iterCursor++;
      if (data.size() > 0 && iterCursor < data.size()) {
        return data.get(iterCursor);
      } else {
        return null;
      }
    }

    @Override
    public void remove() {
      throw new SparqlException("remove not supported on Solutions");
    }
    
  }
  
  @Override
  public RDFNode getBinding(String variable) {
    return getSolution().get(variable);
  }

  @Override
  public NamedNode getNamedNode(String variable) throws SparqlException {
    Object value = this.getBinding(variable);
    if(value == null) {
      return null;
    } else if(value instanceof NamedNode) {
      return (NamedNode) value; 
    } else {
      throw new SparqlException("Node for variable " + variable + " contains a " + value.getClass().getName() + ", not a NamedNode.");
    }
  }
  
  @Override
  public URI getURI(String variable) throws SparqlException {
    return getNamedNode(variable).getURI();
  }

  @Override
  public BlankNode getBlankNode(String variable) throws SparqlException {
    Object value = this.getBinding(variable);
    if(value == null) {
      return null;
    } else if(value instanceof BlankNode) {
      return (BlankNode) value; 
    } else {
      throw new SparqlException("Node for variable " + variable + " contains a " + value.getClass().getName() + ", not a BlankNode.");
    }
  }

  @Override
  public Literal getLiteral(String variable)
      throws SparqlException {
    Object value = this.getBinding(variable);
    if(value == null) {
      return null;
    } else if(value instanceof Literal) {
      return (Literal) value; 
    } else {
      throw new SparqlException("Node for variable " + variable + " contains a " + value.getClass().getName() + ", not a Literal.");
    }
  }

  @Override
  public Date getDateTime(String variable) throws SparqlException {
    return Conversions.toDateTime(getLiteral(variable).getLexical());
  }

  @Override
  public BigInteger getInteger(String variable) throws SparqlException {
    return Conversions.toBigInteger(getLiteral(variable).getLexical());
  }
  
  @Override
  public boolean getBoolean(String variable)
      throws SparqlException {
    return Conversions.toBoolean(getLiteral(variable).getLexical());
  }

  @Override
  public double getDouble(String variable) throws SparqlException {
    return Conversions.toDouble(getLiteral(variable).getLexical());
  }

  @Override
  public float getFloat(String variable) throws SparqlException {
    return Conversions.toFloat(getLiteral(variable).getLexical());
  }

  @Override
  public int getInt(String variable) throws SparqlException {
    return Conversions.toInteger(getLiteral(variable).getLexical());
  }

  @Override
  public String getString(String variable) throws SparqlException {
    return getLiteral(variable).getLexical();
  }

}
