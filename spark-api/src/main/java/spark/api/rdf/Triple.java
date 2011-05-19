package spark.api.rdf;

public interface Triple {

  public Resource getSubject();
  
  public NamedNode getPredicate();
  
  public RDFNode getObject();
}
