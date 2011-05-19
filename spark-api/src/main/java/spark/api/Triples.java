package spark.api;

import spark.api.rdf.NamedNode;
import spark.api.rdf.RDFNode;
import spark.api.rdf.Resource;
import spark.api.rdf.Triple;

public interface Triples extends CursoredResult<Triple> {

  // Generic data access as map or node from the current solution
  Triple getTriple();
  
  // Get parts of the current triple
  
  Resource getSubject();
  NamedNode getPredicate();
  RDFNode getObject();  
}
