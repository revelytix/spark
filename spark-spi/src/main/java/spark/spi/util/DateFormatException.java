/*
 * Copyright 2011 Revelytix Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
