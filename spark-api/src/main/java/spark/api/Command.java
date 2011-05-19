package spark.api;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

import spark.api.rdf.RDFNode;

public interface Command extends Closeable {

  boolean isClosed();
  
  Connection getConnection();
  String getCommand();

  // Timeouts and cancellations

  public static final long NO_TIMEOUT = 0L;
  void setTimeout(long seconds);
  long getTimeout();
  void cancel();
  
  // Preparation and batching 
  
  void addParameterBindings(Map<String, RDFNode> binding);
  List<Map<String, RDFNode>> getBindings();
  void clearBindings();
    
  // Execute 
  
  Result execute();
  Solutions executeQuery();  
  boolean executeAsk();

  
}
