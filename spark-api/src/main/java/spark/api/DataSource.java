package spark.api;

/**
 * Actual implementations of this will be a JavaBean pattern
 * that provides getter/setter methods for specifying source-specific
 * properties.
 *
 */
public interface DataSource {
  /* this is a bean, fill in your own data source props */
  
  Connection getConnection(Credentials creds);
}
