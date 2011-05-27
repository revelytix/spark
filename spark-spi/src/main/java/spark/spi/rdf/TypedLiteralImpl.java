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
package spark.spi.rdf;

import java.net.URI;

import spark.api.rdf.TypedLiteral;

/**
 * Basic implementation of {@link TypedLiteral}.
 * 
 * toString as: "25"^^<http://www.w3.org/2001/XMLSchema#int>
 */
public class TypedLiteralImpl implements TypedLiteral {

  private final String lexical;
  private final URI dataType;
  
  /**
   * Construct a TypedLiteralImpl with a lexical representation and dataType, 
   * both required.
   * @param lexical Lexical representation, must be non-null
   * @param dataType XSD data type, must be non-null
   * @throws NullPointerException If parameters are null
   */
  public TypedLiteralImpl(String lexical, URI dataType) {
    if(lexical == null) {
      throw new NullPointerException("Plain literals must have non-null lexical values.");
    }
    if(dataType == null) {
      throw new NullPointerException("Plain literals must have non-null data types.");
    }

    this.lexical = lexical;
    this.dataType = dataType;
  }
  
  @Override
  public URI getDataType() {
    return this.dataType;
  }

  @Override
  public String getLexical() {
    return this.lexical;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
    result = prime * result + ((lexical == null) ? 0 : lexical.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof TypedLiteral))
      return false;
    TypedLiteral other = (TypedLiteral) obj;
    if (dataType == null) {
      if (other.getDataType() != null)
        return false;
    } else if (!dataType.equals(other.getDataType()))
      return false;
    if (lexical == null) {
      if (other.getLexical() != null)
        return false;
    } else if (!lexical.equals(other.getLexical()))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "\"" + lexical + "\"^^<" + dataType.toString() + ">";
  }
}
