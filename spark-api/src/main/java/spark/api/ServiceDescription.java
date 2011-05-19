package spark.api;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface ServiceDescription {

  List<URI> getNamedGraphs();
  Map<URI, String> getFeatures();
  // ...
  
}
