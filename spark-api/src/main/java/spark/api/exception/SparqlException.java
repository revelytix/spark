package spark.api.exception;

public class SparqlException extends RuntimeException {

  private static final long serialVersionUID = -7174855352655879684L;

  public SparqlException() {
    super();
  }

  public SparqlException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public SparqlException(String arg0) {
    super(arg0);
  }

  public SparqlException(Throwable arg0) {
    super(arg0);
  }  
}
