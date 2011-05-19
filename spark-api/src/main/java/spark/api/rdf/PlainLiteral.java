package spark.api.rdf;

public interface PlainLiteral extends Literal {

  /**
   * Return the language tag, such as "en" or "es".  If no language tag is present, return null.
   * @return Language tag or null
   */
  String getLanguage();
}
