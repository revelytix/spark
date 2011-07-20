/**
 * Copyright � 2011 Revelytix, Inc.  All rights reserved.
 */
package spark.spi.util;

/**
 * Exception to indicate a parse exception when processing an xsd:dateTime literal string.
 * 
 * @author Alex Hall
 * @date Jul 12, 2011
 */
public class DateFormatException extends RuntimeException {

  private static final long serialVersionUID = 5519377640991456873L;
  
  private final String input;
  private final int index;
  
  /**
   * Initialize a date format exception.
   * @param msg The exception message.
   * @param input The input string which triggered the exception.
   * @param index The index in the string at which the exception occurred.
   */
  public DateFormatException(String msg, String input, int index) {
    super(msg);
    this.input = input;
    this.index = index;
  }

  /** The index at which the exception occurred. */
  public int getIndex() { return index; }
  
  /* (non-Javadoc)
   * @see java.lang.Throwable#getMessage()
   */
  @Override
  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append("Illegal xsd:dateTime format: ");
    sb.append(super.getMessage());
    sb.append(" at index ");
    sb.append(index);
    sb.append(" of '");
    sb.append(input);
    sb.append("'");
    return sb.toString();
  }

}
