package spark.api;

import java.io.Closeable;

public interface Connection extends Closeable {

  boolean isClosed();
  
  DataSource getDataSource();
  
  ServiceDescription getDescription();
  
  Command createCommand(String commandString);
  
}
