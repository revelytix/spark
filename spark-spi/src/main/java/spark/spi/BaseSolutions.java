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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import spark.api.Command;
import spark.api.Solutions;
import spark.api.exception.SparqlException;
import spark.api.rdf.BlankNode;
import spark.api.rdf.Literal;
import spark.api.rdf.NamedNode;
import spark.api.rdf.RDFNode;

/**
 * @author Alex Hall
 * @date Jul 28, 2011
 */
public abstract class BaseSolutions extends BaseResults implements Solutions {
  
  protected final List<String> vars;

  /**
   * @param command
   */
  public BaseSolutions(Command command, List<String> vars) {
    super(command);
    this.vars = Collections.unmodifiableList(vars);
  }

  @Override
  public boolean isBound(String variable) {
    return getResult().get(variable) != null;
  }

  @Override
  public List<String> getVariables() {
    return this.vars;
  }

  @Override
  public RDFNode getBinding(String variable) {
    return getResult().get(variable);
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
  public Literal getLiteral(String variable) throws SparqlException {
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
  public boolean getBoolean(String variable) throws SparqlException {
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