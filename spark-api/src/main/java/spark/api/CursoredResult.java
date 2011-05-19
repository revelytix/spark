package spark.api;

public interface CursoredResult <R> extends Result, Iterable<R> {

  // Movement methods 
    
  boolean next();
  
  // Location questions 
  
  int getRow();
  boolean isBeforeFirst();
  boolean isFirst();
  boolean isLast();
  boolean isAfterLast();
  
}
