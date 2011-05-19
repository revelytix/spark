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

import spark.api.rdf.PlainLiteral;

public class PlainLiteralImpl implements PlainLiteral {

  private final String lexical;
  private final String language;

  public PlainLiteralImpl(String lexical, String language) {
    if(lexical == null) {
      throw new NullPointerException("Plain literals must have non-null lexical values.");
    }
    this.lexical = lexical;
    this.language = language;
  }
  
  public PlainLiteralImpl(String lexical) {
    this(lexical, null);
  }
  
  @Override
  public String getLanguage() {
    return this.language;
  }

  @Override
  public String getLexical() {
    return this.lexical;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    result = prime * result + ((lexical == null) ? 0 : lexical.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof PlainLiteral))
      return false;
    PlainLiteral other = (PlainLiteral) obj;
    if (language == null) {
      if (other.getLanguage() != null)
        return false;
    } else if (!language.equals(other.getLanguage()))
      return false;
    if (lexical == null) {
      if (other.getLexical() != null)
        return false;
    } else if (!lexical.equals(other.getLexical()))
      return false;
    return true;
  }

  public String toString() {
    return "\"" + this.lexical + "\"" + (language != null ? "@" + language: "");
  }
}
