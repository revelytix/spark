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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import spark.api.rdf.Literal;
import spark.api.rdf.TypedLiteral;
import spark.api.uris.XsdTypes;
import spark.spi.rdf.TypedLiteralImpl;
import spark.spi.util.DateTime;

/**
 * <p>Class of convenience conversions between lexical string representations of the 
 * various XSD data types and their native Java representations.</p>
 * 
 * <p>This class provides methods to translate from native Java to RDF literals, and from RDF
 * literals and lexical values to native Java objects. In general, translating a native Java object
 * of a supported XSD datatype to an RDF literal via {@link #toLiteral(Object)} and back to a native
 * Java object via {@link #toData(TypedLiteral)} will yield an object equal to the original one.
 * The converse is not necessarily true: translating from RDF typed literal to Java object and back
 * to RDF typed literal should not be assumed to be a stable round-trip.</p>
 * 
 * <p>The following table gives the supported XSD datatypes and their Java counterparts.</p>
 * 
 * <table border='1'>
 *  <tr><th>XSD Datatype</th><th>Java class</th></tr>
 *  <tr><td><tt>xsd:byte</tt></td><td>{@link Byte}, <tt>byte</tt> primitive type</td></tr>
 *  <tr><td><tt>xsd:short</tt></td><td>{@link Short}, <tt>short</tt> primitive type</td></tr>
 *  <tr><td><tt>xsd:int</tt></td><td>{@link Integer}, <tt>int</tt> primitive type</td></tr>
 *  <tr><td><tt>xsd:long</tt></td><td>{@link Long}, <tt>long</tt> primitive type</td></tr>
 *  <tr><td><tt>xsd:boolean</tt></td><td>{@link Boolean}, <tt>boolean</tt> primitive type</td></tr>
 *  <tr><td><tt>xsd:float</tt></td><td>{@link Float}, <tt>float</tt> primitive type</td></tr>
 *  <tr><td><tt>xsd:double</tt></td><td>{@link Double}, <tt>double</tt> primitive type</td></tr>
 *  <tr><td><tt>xsd:string</tt></td><td>{@link String}</td></tr>
 *  <tr><td><tt>xsd:integer</tt></td><td>{@link BigInteger}</td></tr>
 *  <tr><td><tt>xsd:decimal</tt></td><td>{@link BigDecimal}</td></tr>
 *  <tr><td><tt>xsd:dateTime</tt></td><td>{@link Date}</td></tr>
 * </table>
 * 
 * <p>Methods are overloaded to use primitive types whenever possible, and boxing/unboxing is done as necessary.</p>
 */
public final class Conversions {

  private static final Map<URI,Conversion<?>> uriConversions = new HashMap<URI,Conversion<?>>();
  private static final Map<Class<?>,Conversion<?>> classConversions = new HashMap<Class<?>,Conversion<?>>();
  
  /** Datatype conversion class. */
  private static abstract class Conversion<T> {
    /** Datatype URI. */
    final URI typeUri;
    /** Corresponding Java class. */
    final Class<T> clazz;
    /** Initialize and register the class. */
    Conversion(Class<T> c, URI dt) {
      typeUri = dt;
      clazz = c;
      uriConversions.put(dt, this);
      classConversions.put(c, this);
    }
    /** @return an RDF literal with the appropriate datatype URI and lexical form computed from the given object. */
    final TypedLiteral literal(Object data) {
      return new TypedLiteralImpl(lexical(clazz.cast(data)), typeUri);
    }
    /** @return a native Java object translated from the given lexical string. */
    abstract T data(String s);
    /** @return the lexical string to use for the given object. */
    String lexical(T data) { return data.toString(); }
  }
  
  /** xsd:byte conversion */
  private static final Conversion<Byte> BYTE = new Conversion<Byte>(Byte.class, XsdTypes.BYTE) {
    Byte data(String s) {
      return toByte(s);
    }
  };
  
  /** xsd:short conversion */
  private static final Conversion<Short> SHORT = new Conversion<Short>(Short.class, XsdTypes.SHORT) {
    Short data(String s) {
      return toShort(s);
    }
  };
  
  /** xsd:int conversion */
  private static final Conversion<Integer> INT = new Conversion<Integer>(Integer.class, XsdTypes.INT) {
    Integer data(String s) {
      return toInteger(s);
    }
  };
  
  /** xsd:long conversion */
  private static final Conversion<Long> LONG = new Conversion<Long>(Long.class, XsdTypes.LONG) {
    Long data(String s) {
      return toLong(s);
    }
  };
  
  /** xsd:integer conversion */
  @SuppressWarnings("unused")
  private static final Conversion<BigInteger> INTEGER = new Conversion<BigInteger>(BigInteger.class, XsdTypes.INTEGER) {
    BigInteger data(String s) {
      return toBigInteger(s);
    }
  };
  
  /** xsd:boolean conversion */
  private static final Conversion<Boolean> BOOLEAN = new Conversion<Boolean>(Boolean.class, XsdTypes.BOOLEAN) {
    Boolean data(String s) {
      return toBoolean(s);
    }
  };
  
  /** xsd:float conversion */
  private static final Conversion<Float> FLOAT = new Conversion<Float>(Float.class, XsdTypes.FLOAT) {
    Float data(String s) {
      return toFloat(s);
    }
  };
  
  /** xsd:double conversion */
  private static final Conversion<Double> DOUBLE = new Conversion<Double>(Double.class, XsdTypes.DOUBLE) {
    Double data(String s) {
      return toDouble(s);
    }
  };
  
  /** xsd:decimal conversion */
  @SuppressWarnings("unused")
  private static final Conversion<BigDecimal> DECIMAL = new Conversion<BigDecimal>(BigDecimal.class, XsdTypes.DECIMAL) {
    BigDecimal data(String s) {
      return toDecimal(s);
    }
  };
  
  /** xsd:string conversion */
  @SuppressWarnings("unused")
  private static final Conversion<String> STRING = new Conversion<String>(String.class, XsdTypes.STRING) {
    String data(String s) {
      return s;
    }
  };
  
  /** xsd:dateTime conversion */
  @SuppressWarnings("unused")
  private static final Conversion<Date> DATE_TIME = new Conversion<Date>(Date.class, XsdTypes.DATE_TIME) {
    Date data(String s) {
      return toDateTime(s);
    }
    String lexical(Date data) {
      return DateTime.format(data);
    }
  };
  
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
   * Convert from lexical to byte (xsd:byte)
   * @param lexicalByte Lexical representation
   * @return Converted value
   */
  public static byte toByte(String lexicalByte) {
    return Byte.parseByte(lexicalByte);
  }
  
  /**
   * Convert from lexical to short (xsd:short)
   * @param lexicalShort Lexical representation
   * @return Converted value
   */
  public static short toShort(String lexicalShort) {
    return Short.parseShort(lexicalShort);
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
   * Convert from lexical to long (xsd:long)
   * @param lexicalLong Lexical representation
   * @return Converted value
   */
  public static long toLong(String lexicalLong) {
    return Long.parseLong(lexicalLong);
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
   * Convert from lexical to double (xsd:double)
   * @param lexicalDecimal Lexical representation
   * @return Converted value
   */
  public static BigDecimal toDecimal(String lexicalDecimal) {
    return new BigDecimal(lexicalDecimal);
  }

  /**
   * Convert from lexical to Date (xsd:dateTime)
   * @param lexicalDate Lexical representation
   * @return Converted value
   */
  public static Date toDateTime(String lexicalDate) {
    return DateTime.parse(lexicalDate);
  }
  
  /**
   * Convert from RDF literal to native Java object.
   * @param lit RDF literal.
   * @return Java object converted from the literal.
   */
  public static Object toData(Literal lit) {
    if (lit == null) throw new IllegalArgumentException("Can't convert null literal");
    if (lit instanceof TypedLiteral) return toData((TypedLiteral)lit);
    // Untyped literals are xsd:string
    // Note this isn't strictly correct; language tags will be lost here.
    return lit.getLexical();
  }
  
  /**
   * Convert from RDF typed literal to native Java object.
   * @param lit RDF typed literal
   * @return Java object converted from the lexical value based on the mappings specified by the literal datatype URI.
   */
  public static Object toData(TypedLiteral lit) {
    if (lit == null) throw new IllegalArgumentException("Can't convert null literal");
    Conversion<?> c = uriConversions.get(lit.getDataType());
    if (c == null) throw new IllegalArgumentException("Don't know how to convert literal of type " + lit.getDataType());
    return c.data(lit.getLexical());
  }
  
  /**
   * Convert from byte to xsd:byte typed literal
   * @param b Byte value
   * @return Converted literal
   */
  public static TypedLiteral toLiteral(byte b) {
    return new TypedLiteralImpl(Byte.toString(b), BYTE.typeUri);
  }
  
  /**
   * Convert from short to xsd:short typed literal
   * @param s Short value
   * @return Converted literal
   */
  public static TypedLiteral toLiteral(short s) {
    return new TypedLiteralImpl(Short.toString(s), SHORT.typeUri);
  }

  /**
   * Convert from integer to xsd:int typed literal
   * @param i Integer value
   * @return Converted literal
   */
  public static TypedLiteral toLiteral(int i) {
    return new TypedLiteralImpl(Integer.toString(i), INT.typeUri);
  }
  
  /**
   * Convert from long to xsd:long typed literal
   * @param l Long value
   * @return Converted literal
   */
  public static TypedLiteral toLiteral(long l) {
    return new TypedLiteralImpl(Long.toString(l), LONG.typeUri);
  }
  
  /**
   * Convert from boolean to xsd:boolean typed literal
   * @param b Boolean value
   * @return Converted literal
   */
  public static TypedLiteral toLiteral(boolean b) {
    return new TypedLiteralImpl(Boolean.toString(b), BOOLEAN.typeUri);
  }
  
  /**
   * Convert from float to xsd:float typed literal
   * @param f Float value
   * @return Converted literal
   */
  public static TypedLiteral toLiteral(float f) {
    return new TypedLiteralImpl(Float.toString(f), FLOAT.typeUri);
  }
  
  /**
   * Convert from double to xsd:double typed literal
   * @param d Double value
   * @return Converted literal
   */
  public static TypedLiteral toLiteral(double d) {
    return new TypedLiteralImpl(Double.toString(d), DOUBLE.typeUri);
  }
  
  /**
   * Convert from an arbitrary Java object to an RDF typed literal, using an XSD datatype if possible.
   * @param value Object value
   * @return Converted literal
   */
  public static TypedLiteral toLiteral(Object value) {
    if (value == null) throw new IllegalArgumentException("Can't convert null value");
    Conversion<?> c = classConversions.get(value.getClass());
    if (c != null) return c.literal(value);
    // The object has an unrecognized type that doesn't translate directly to XSD.
    // Omitting the datatype would imply a type of xsd:string, so use xsd:anySimpleType instead.
    // The use of xsd:anySimpleType prevents round-tripping; in the future we could possibly
    // serialize this as a byte array and use xsd:hexBinary or xsd:base64Binary.
    return new TypedLiteralImpl(value.toString(), XsdTypes.ANY_SIMPLE_TYPE);
  }
}