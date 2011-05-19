package spark.api.uris;

import java.net.URI;

public interface RdfTypes {

  public static final String RDF_BASE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  
  public static final URI RDF_TYPE = URI.create(RDF_BASE + "type");
  public static final URI RDF_PROPERTY = URI.create(RDF_BASE + "Property");
  public static final URI RDF_STATEMENT = URI.create(RDF_BASE + "Statement");
  public static final URI RDF_SUBJECT = URI.create(RDF_BASE + "subject");
  public static final URI RDF_PREDICATE = URI.create(RDF_BASE + "predicate");
  public static final URI RDF_OBJECT = URI.create(RDF_BASE + "object");
  public static final URI RDF_BAG = URI.create(RDF_BASE + "Bag");
  public static final URI RDF_SEQ = URI.create(RDF_BASE + "Seq");
  public static final URI RDF_ALT = URI.create(RDF_BASE + "Alt");
  public static final URI RDF_VALUE = URI.create(RDF_BASE + "value");
  public static final URI RDF_LIST = URI.create(RDF_BASE + "List");
  public static final URI RDF_NIL = URI.create(RDF_BASE + "nil");
  public static final URI RDF_FIRST = URI.create(RDF_BASE + "first");
  public static final URI RDF_XML_LITERAL = URI.create(RDF_BASE + "XMLLiteral");
  public static final URI RDF_PLAIN_LITERAL = URI.create(RDF_BASE + "PlainLiteral");
  
}
