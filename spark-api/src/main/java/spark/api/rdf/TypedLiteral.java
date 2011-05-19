package spark.api.rdf;

import java.net.URI;

public interface TypedLiteral extends Literal {

  public URI getDataType();
}
