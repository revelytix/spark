package spark.api;

import java.io.Closeable;


public interface Result extends Closeable {

  boolean isClosed();
  
  /**
   * Get the command that was executed to create this result.
   * @return The command that created this result
   */
  Command getCommand();
  
}
