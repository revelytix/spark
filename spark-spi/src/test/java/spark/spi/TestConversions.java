/**
 * Copyright 2011 Revelytix, Inc.  All rights reserved.
 */
package spark.spi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Date;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.api.rdf.TypedLiteral;
import spark.api.uris.XsdTypes;
import spark.spi.rdf.PlainLiteralImpl;
import spark.spi.rdf.TypedLiteralImpl;

/**
 * @author Alex Hall
 * @date Jul 27, 2011
 */
public class TestConversions extends TestCase {

  private static final Logger logger = LoggerFactory.getLogger(TestConversions.class);
  
  public void testInt() {
    String s = "1234";
    int i = 1234;
    TypedLiteralImpl l = new TypedLiteralImpl(s, XsdTypes.INT);
    assertEquals(i, Conversions.toInteger(s));
    assertEquals(l, Conversions.toLiteral(i));
    roundTrip(Integer.valueOf(i), l);
    invalid("abc", XsdTypes.INT);
    invalid("156432418974561566571", XsdTypes.INT);
  }
  
  public void testInteger() {
    String s = "156432418974561566571";
    BigInteger i = new BigInteger(s);
    TypedLiteralImpl l = new TypedLiteralImpl(s, XsdTypes.INTEGER);
    assertEquals(i, Conversions.toBigInteger(s));
    assertEquals(l, Conversions.toLiteral(i));
    roundTrip(i, l);
    invalid("NaN", XsdTypes.INTEGER);
  }
  
  public void testBoolean() {
    String s = "true";
    boolean b = true;
    TypedLiteralImpl l = new TypedLiteralImpl(s, XsdTypes.BOOLEAN);
    assertEquals(b, Conversions.toBoolean(s));
    assertEquals(l, Conversions.toLiteral(b));
    roundTrip(Boolean.valueOf(b), l);
  }
  
  public void testByte() {
    String s = "99";
    byte b = 99;
    TypedLiteralImpl l = new TypedLiteralImpl(s, XsdTypes.BYTE);
    assertEquals(b, Conversions.toByte(s));
    assertEquals(l, Conversions.toLiteral(b));
    roundTrip(Byte.valueOf(b), l);
    invalid("256", XsdTypes.BYTE);
  }
  
  public void testShort() {
    String s = "25943";
    short i = 25943;
    TypedLiteralImpl l = new TypedLiteralImpl(s, XsdTypes.SHORT);
    assertEquals(i, Conversions.toShort(s));
    assertEquals(l, Conversions.toLiteral(i));
    roundTrip(Short.valueOf(i), l);
    invalid("45256", XsdTypes.SHORT);
  }
  
  public void testLong() {
    String s = "126476513274";
    long i = 126476513274L;
    TypedLiteralImpl l = new TypedLiteralImpl(s, XsdTypes.LONG);
    assertEquals(i, Conversions.toLong(s));
    assertEquals(l, Conversions.toLiteral(i));
    roundTrip(Long.valueOf(i), l);
    invalid("156432418974561566571", XsdTypes.LONG);
  }
  
  public void testFloat() {
    String s = "5643.475";
    float f = 5643.475f;
    TypedLiteralImpl l = new TypedLiteralImpl(s, XsdTypes.FLOAT);
    assertEquals(f, Conversions.toFloat(s));
    assertEquals(l, Conversions.toLiteral(f));
    roundTrip(Float.valueOf(f), l);
    invalid("86nd", XsdTypes.FLOAT);
  }
  
  public void testDouble() {
    String s = "8734539.39586345";
    double d = 8734539.39586345d;
    TypedLiteralImpl l = new TypedLiteralImpl(s, XsdTypes.DOUBLE);
    assertEquals(d, Conversions.toDouble(s));
    assertEquals(l, Conversions.toLiteral(d));
    roundTrip(Double.valueOf(d), l);
    invalid("86nd", XsdTypes.DOUBLE);
  }
  
  public void testDecimal() {
    String s = "8734539.39586345";
    BigDecimal d = new BigDecimal(s);
    TypedLiteralImpl l = new TypedLiteralImpl(s, XsdTypes.DECIMAL);
    assertEquals(d, Conversions.toDecimal(s));
    roundTrip(d, l);
    invalid("86nd", XsdTypes.DECIMAL);
  }
  
  public void testDate() {
    String s = "2011-07-27T22:25:16.812Z";
    Date d = new Date(1311805516812L);
    TypedLiteralImpl l = new TypedLiteralImpl(s, XsdTypes.DATE_TIME);
    assertEquals(d, Conversions.toDateTime(s));
    roundTrip(d, l);
    invalid("Wed Jul 27 18:25:16 EDT 2011", XsdTypes.DATE_TIME);
  }
  
  public void testString() {
    String s = "foo bar";
    TypedLiteralImpl l = new TypedLiteralImpl(s, XsdTypes.STRING);
    assertEquals(s, Conversions.toData(new PlainLiteralImpl(s)));
    roundTrip(s, l);
  }
  
  public void testUnknownType() {
    invalid("1234", XsdTypes.UNSIGNED_INT);
  }
  
  private static void roundTrip(Object val, TypedLiteral lit) {
    assertEquals(val, Conversions.toData(lit));
    assertEquals(lit, Conversions.toLiteral(val));
  }
  
  private static void invalid(String lexical, URI datatype) {
    TypedLiteralImpl l = new TypedLiteralImpl(lexical, datatype);
    try {
      Conversions.toData(l);
      fail("Should have thrown exception converting literal " + l);
    } catch (Exception e) {
      logger.debug("Conversion exception, message: {}", e.getMessage());
    }
  }
}
