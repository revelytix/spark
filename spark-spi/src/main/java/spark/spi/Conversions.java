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
package spark.spi;

import java.math.BigInteger;
import java.util.Date;

import spark.spi.util.DateTime;

/**
 * Class of convenience conversions from lexical string representation to the 
 * various XSD data type native Java representations.
 */
public final class Conversions {

  private Conversions() {}

  /**
   * Convert from lexical to BigInteger (xsd:integer)
   * @param lexicalBigInteger Lexical representation
   * @return Converted value
   */
  public static BigInteger toBigInteger(String lexicalBigInteger) {
    return new BigInteger(lexicalBigInteger);
  }
  
  /**
   * Convert from lexical to int (xsd:int)
   * @param lexicalInteger Lexical representation
   * @return Converted value
   */
  public static int toInteger(String lexicalInteger) {
    return Integer.parseInt(lexicalInteger);
  }
  
  /**
   * Convert from lexical to boolean (xsd:boolean)
   * @param lexicalBoolean Lexical representation
   * @return Converted value
   */
  public static boolean toBoolean(String lexicalBoolean) {
    return Boolean.parseBoolean(lexicalBoolean);
  }

  /**
   * Convert from lexical to float (xsd:float)
   * @param lexicalFloat Lexical representation
   * @return Converted value
   */
  public static float toFloat(String lexicalFloat) {
    return Float.parseFloat(lexicalFloat);
  }
  
  /**
   * Convert from lexical to double (xsd:double)
   * @param lexicalDouble Lexical representation
   * @return Converted value
   */
  public static double toDouble(String lexicalDouble) {
    return Double.parseDouble(lexicalDouble);
  }

  /**
   * Convert from lexical to Date (xsd:dateTime)
   * @param lexicalDate Lexical representation
   * @return Converted value
   */
  public static Date toDateTime(String lexicalDate) {
    return DateTime.parse(lexicalDate);
  }
}