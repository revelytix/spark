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
package spark.api.rdf;

import java.net.URI;

import spark.api.uris.XsdTypes;

/**
 * Represents an RDF typed literal, which has a lexical representation and an 
 * XSD data type.  Constants for the standard XSD types are defined in {@link XsdTypes}.
 */
public interface TypedLiteral extends Literal {

  /**
   * Get the XSD data type for this typed literal
   * @return The XSD URI
   */
  public URI getDataType();
}
