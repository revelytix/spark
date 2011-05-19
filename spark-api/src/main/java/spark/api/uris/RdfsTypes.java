package spark.api.uris;

import java.net.URI;

public interface RdfsTypes {

  public static final String RDFS_BASE = "http://www.w3.org/2000/01/rdf-schema#";
  
  public static final URI RDFS_LABEL = URI.create(RDFS_BASE + "label");
  public static final URI RDFS_RESOURCE = URI.create(RDFS_BASE + "Resource");
  public static final URI RDFS_CLASS = URI.create(RDFS_BASE + "Class");
  public static final URI RDFS_SUB_CLASS_OF = URI.create(RDFS_BASE + "subClassOf");
  public static final URI RDFS_SUB_PROPERTY_OF = URI.create(RDFS_BASE + "subPropertyOf");
  public static final URI RDFS_COMMENT = URI.create(RDFS_BASE + "comment");
  public static final URI RDFS_LITERAL = URI.create(RDFS_BASE + "Literal");
  public static final URI RDFS_DOMAIN = URI.create(RDFS_BASE + "domain");
  public static final URI RDFS_RANGE = URI.create(RDFS_BASE + "range");
  public static final URI RDFS_SEE_ALSO = URI.create(RDFS_BASE + "seeAlso");
  public static final URI RDFS_IS_DEFINED_BY = URI.create(RDFS_BASE + "isDefinedBy");
  public static final URI RDFS_CONTAINER = URI.create(RDFS_BASE + "Container");
  public static final URI RDFS_CONTAINER_MEMBERSHIP_PROPERTY = URI.create(RDFS_BASE + "ContainerMembershipProperty");
  public static final URI RDFS_MEMBER = URI.create(RDFS_BASE + "member");
  public static final URI RDFS_DATATYPE = URI.create(RDFS_BASE + "Datatype");
  
}
