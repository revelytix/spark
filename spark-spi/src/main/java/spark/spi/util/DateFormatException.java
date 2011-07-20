/**
 * Copyright © 2011 Revelytix, Inc.  All rights reserved.
 */
package spark.spi.util;

/**
 * @author Alex Hall
 * @date Jul 12, 2011
 */
public class DateFormatException extends RuntimeException {

  private static final long serialVersionUID = 5519377640991456873L;
  
  private final String input;
  private final int index;
  
  /**
   * @param message
   */
  public DateFormatException(String msg, String input, int index) {
    super(msg);
    this.input = input;
    this.index = index;
  }

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
