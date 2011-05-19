package spark.api.uris;

import java.net.URI;

/**
 * URIs represented all XSD types used for TypedLiterals.
 */
public interface XsdTypes {

  public static final String XSD_BASE = "http://www.w3.org/2001/XMLSchema#";
  
  public static final URI ANY_TYPE = URI.create(XSD_BASE + "anyType");
  public static final URI ANY_SIMPLE_TYPE = URI.create(XSD_BASE + "anySimpleType");
  public static final URI DURATION = URI.create(XSD_BASE + "duration");
  public static final URI DATE_TIME = URI.create(XSD_BASE + "dateTime");
  public static final URI TIME = URI.create(XSD_BASE + "time");
  public static final URI DATE = URI.create(XSD_BASE + "date");
  public static final URI G_YEAR_MONTH = URI.create(XSD_BASE + "gYearMonth");
  public static final URI G_YEAR = URI.create(XSD_BASE + "gYear");
  public static final URI G_MONTH_DAY = URI.create(XSD_BASE + "gMonthDay");
  public static final URI G_DAY = URI.create(XSD_BASE + "gDay");
  public static final URI G_MONTH = URI.create(XSD_BASE + "gMonth");
  public static final URI BOOLEAN = URI.create(XSD_BASE + "boolean");
  public static final URI BASE_64_BINARY = URI.create(XSD_BASE + "base64Binary");
  public static final URI HEX_BINARY = URI.create(XSD_BASE + "hexBinary");
  public static final URI FLOAT = URI.create(XSD_BASE + "float");
  public static final URI DOUBLE = URI.create(XSD_BASE + "double");
  public static final URI ANY_URI = URI.create(XSD_BASE + "anyURI");
  public static final URI QNAME = URI.create(XSD_BASE + "QName");
  public static final URI NOTATION = URI.create(XSD_BASE + "NOTATION");
  public static final URI ID = URI.create(XSD_BASE + "ID");
  public static final URI IDREF = URI.create(XSD_BASE + "IDREF");
  public static final URI IDREFS = URI.create(XSD_BASE + "IDREFS");
  public static final URI ENTITY = URI.create(XSD_BASE + "ENTITY");
  public static final URI ENTITIES = URI.create(XSD_BASE + "ENTITIES");
  public static final URI NMTOKEN = URI.create(XSD_BASE + "NMTOKEN");
  public static final URI NMTOKENS = URI.create(XSD_BASE + "NMTOKENS");
  public static final URI DECIMAL = URI.create(XSD_BASE + "decimal");
  public static final URI INTEGER = URI.create(XSD_BASE + "integer");
  public static final URI NON_POSITIVE_INTEGER = URI.create(XSD_BASE + "nonPositiveInteger");
  public static final URI NEGATIVE_INTEGER = URI.create(XSD_BASE + "negativeInteger");
  public static final URI LONG = URI.create(XSD_BASE + "long");
  public static final URI INT = URI.create(XSD_BASE + "int");
  public static final URI SHORT = URI.create(XSD_BASE + "short");
  public static final URI BYTE = URI.create(XSD_BASE + "byte");
  public static final URI NON_NEGATIVE_INTEGER = URI.create(XSD_BASE + "nonNegativeInteger");
  public static final URI UNSIGNED_LONG = URI.create(XSD_BASE + "unsignedLong");
  public static final URI UNSIGNED_INT = URI.create(XSD_BASE + "unsignedInt");
  public static final URI UNSIGNED_SHORT = URI.create(XSD_BASE + "unsignedShort");
  public static final URI UNSIGNED_BYTE = URI.create(XSD_BASE + "unsignedByte");
  public static final URI POSITIVE_INTEGER = URI.create(XSD_BASE + "positiveInteger");
}
