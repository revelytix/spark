package spark.api.rdf;

import java.net.URI;

public interface NamedNode extends Resource {

  /**
   * Gets a wrapped URI.
   */
  URI getURI();

}
