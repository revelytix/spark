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
package spark.api.uris;

import java.net.URI;

/**
 * URIs represented all XSD types used for TypedLiterals.
 */
public interface XsdTypes {

  /**
   * XSD base namespace = http://www.w3.org/2001/XMLSchema#
   */
  public static final String XSD_BASE = "http://www.w3.org/2001/XMLSchema#";
  
  /**
   * http://www.w3.org/2001/XMLSchema#anyType
   */
  public static final URI ANY_TYPE = URI.create(XSD_BASE + "anyType");

  /**
   * http://www.w3.org/2001/XMLSchema#anySimpleType
   */
  public static final URI ANY_SIMPLE_TYPE = URI.create(XSD_BASE + "anySimpleType");

  /**
   * http://www.w3.org/2001/XMLSchema#duration
   */
  public static final URI DURATION = URI.create(XSD_BASE + "duration");

  /**
   * http://www.w3.org/2001/XMLSchema#dateTime
   */
  public static final URI DATE_TIME = URI.create(XSD_BASE + "dateTime");

  /**
   * http://www.w3.org/2001/XMLSchema#time
   */
  public static final URI TIME = URI.create(XSD_BASE + "time");

  /**
   * http://www.w3.org/2001/XMLSchema#date
   */
  public static final URI DATE = URI.create(XSD_BASE + "date");

  /**
   * http://www.w3.org/2001/XMLSchema#gYearMonth
   */
  public static final URI G_YEAR_MONTH = URI.create(XSD_BASE + "gYearMonth");

  /**
   * http://www.w3.org/2001/XMLSchema#gYear
   */
  public static final URI G_YEAR = URI.create(XSD_BASE + "gYear");

  /**
   * http://www.w3.org/2001/XMLSchema#gMonthDay
   */
  public static final URI G_MONTH_DAY = URI.create(XSD_BASE + "gMonthDay");

  /**
   * http://www.w3.org/2001/XMLSchema#gDay
   */
  public static final URI G_DAY = URI.create(XSD_BASE + "gDay");

  /**
   * http://www.w3.org/2001/XMLSchema#gMonth
   */
  public static final URI G_MONTH = URI.create(XSD_BASE + "gMonth");

  /**
   * http://www.w3.org/2001/XMLSchema#boolean
   */
  public static final URI BOOLEAN = URI.create(XSD_BASE + "boolean");

  /**
   * http://www.w3.org/2001/XMLSchema#base64Binary
   */
  public static final URI BASE_64_BINARY = URI.create(XSD_BASE + "base64Binary");

  /**
   * http://www.w3.org/2001/XMLSchema#hexBinary
   */
  public static final URI HEX_BINARY = URI.create(XSD_BASE + "hexBinary");

  /**
   * http://www.w3.org/2001/XMLSchema#float
   */
  public static final URI FLOAT = URI.create(XSD_BASE + "float");

  /**
   * http://www.w3.org/2001/XMLSchema#double
   */
  public static final URI DOUBLE = URI.create(XSD_BASE + "double");

  /**
   * http://www.w3.org/2001/XMLSchema#anyURI
   */
  public static final URI ANY_URI = URI.create(XSD_BASE + "anyURI");

  /**
   * http://www.w3.org/2001/XMLSchema#QName
   */
  public static final URI QNAME = URI.create(XSD_BASE + "QName");

  /**
   * http://www.w3.org/2001/XMLSchema#NOTATION
   */
  public static final URI NOTATION = URI.create(XSD_BASE + "NOTATION");

  /**
   * http://www.w3.org/2001/XMLSchema#ID
   */
  public static final URI ID = URI.create(XSD_BASE + "ID");

  /**
   * http://www.w3.org/2001/XMLSchema#IDREF
   */
  public static final URI IDREF = URI.create(XSD_BASE + "IDREF");

  /**
   * http://www.w3.org/2001/XMLSchema#IDREFS
   */
  public static final URI IDREFS = URI.create(XSD_BASE + "IDREFS");

  /**
   * http://www.w3.org/2001/XMLSchema#ENTITY
   */
  public static final URI ENTITY = URI.create(XSD_BASE + "ENTITY");

  /**
   * http://www.w3.org/2001/XMLSchema#ENTITIES
   */
  public static final URI ENTITIES = URI.create(XSD_BASE + "ENTITIES");

  /**
   * http://www.w3.org/2001/XMLSchema#NMTOKEN
   */
  public static final URI NMTOKEN = URI.create(XSD_BASE + "NMTOKEN");

  /**
   * http://www.w3.org/2001/XMLSchema#NMTOKENS
   */
  public static final URI NMTOKENS = URI.create(XSD_BASE + "NMTOKENS");

  /**
   * http://www.w3.org/2001/XMLSchema#decimal
   */
  public static final URI DECIMAL = URI.create(XSD_BASE + "decimal");

  /**
   * http://www.w3.org/2001/XMLSchema#integer
   */
  public static final URI INTEGER = URI.create(XSD_BASE + "integer");

  /**
   * http://www.w3.org/2001/XMLSchema#nonPositiveInteger
   */
  public static final URI NON_POSITIVE_INTEGER = URI.create(XSD_BASE + "nonPositiveInteger");

  /**
   * http://www.w3.org/2001/XMLSchema#negativeInteger
   */
  public static final URI NEGATIVE_INTEGER = URI.create(XSD_BASE + "negativeInteger");

  /**
   * http://www.w3.org/2001/XMLSchema#long
   */
  public static final URI LONG = URI.create(XSD_BASE + "long");

  /**
   * http://www.w3.org/2001/XMLSchema#int
   */
  public static final URI INT = URI.create(XSD_BASE + "int");

  /**
   * http://www.w3.org/2001/XMLSchema#short
   */
  public static final URI SHORT = URI.create(XSD_BASE + "short");

  /**
   * http://www.w3.org/2001/XMLSchema#byte
   */
  public static final URI BYTE = URI.create(XSD_BASE + "byte");

  /**
   * http://www.w3.org/2001/XMLSchema#nonNegativeInteger
   */
  public static final URI NON_NEGATIVE_INTEGER = URI.create(XSD_BASE + "nonNegativeInteger");

  /**
   * http://www.w3.org/2001/XMLSchema#unsignedLong
   */
  public static final URI UNSIGNED_LONG = URI.create(XSD_BASE + "unsignedLong");

  /**
   * http://www.w3.org/2001/XMLSchema#unsignedInt
   */
  public static final URI UNSIGNED_INT = URI.create(XSD_BASE + "unsignedInt");

  /**
   * http://www.w3.org/2001/XMLSchema#unsignedShort
   */
  public static final URI UNSIGNED_SHORT = URI.create(XSD_BASE + "unsignedShort");

  /**
   * http://www.w3.org/2001/XMLSchema#unsignedByte
   */
  public static final URI UNSIGNED_BYTE = URI.create(XSD_BASE + "unsignedByte");

  /**
   * http://www.w3.org/2001/XMLSchema#positiveInteger
   */
  public static final URI POSITIVE_INTEGER = URI.create(XSD_BASE + "positiveInteger");
}
