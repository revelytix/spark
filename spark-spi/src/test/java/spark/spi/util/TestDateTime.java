/**
 * Copyright 2011 Revelytix, Inc.  All rights reserved.
 */
package spark.spi.util;

import java.util.Date;
import java.util.Formatter;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.Test;


/**
 * @author Alex Hall
 * @date Jul 14, 2011
 */
public class TestDateTime {

  private static final long A_DATE = 1310685769478L;
  private static final String A_NORMAL_STRING = "2011-07-14T23:22:49.478Z";
  private static final TimeZone A_TIMEZONE = TimeZone.getTimeZone("GMT-04:00");
  private static final String A_TIMEZONE_STRING = "2011-07-14T19:22:49.478-04:00";
  
  @Test
  public void testNormalForm() {
    Date d = new Date(A_DATE);
    Assert.assertEquals(d, DateTime.parse(A_NORMAL_STRING));
    Assert.assertEquals(A_NORMAL_STRING, DateTime.format(d));
  }
  
  @Test
  public void testTimezoneOffset() {
    Date d = new Date(A_DATE);
    Assert.assertEquals(d, DateTime.parse(A_TIMEZONE_STRING));
    Assert.assertEquals(A_TIMEZONE_STRING, DateTime.format(d, A_TIMEZONE));
  }
  
  @Test
  public void testLocalTimezone() {
    Date d = new Date(A_DATE);
    String s = DateTime.format(d, null);
    //System.out.println(s);
    Assert.assertNotNull(s);
    Assert.assertFalse(s.isEmpty());
    Assert.assertTrue(Character.isDigit(s.charAt(s.length() - 1))); // No 'Z' indicator
    Assert.assertEquals(d, DateTime.parse(s));
    
    d = new Date(A_DATE / 1000 * 1000);
    s = DateTime.format(d, null);
    //System.out.println(s);
    Assert.assertEquals(d, DateTime.parse(s));
  }
  
  @Test
  public void testMidnight() {
    String s1 = "2011-07-14T24:00:00Z";
    String s2 = "2011-07-15T00:00:00Z";
    Date d1 = DateTime.parse(s1);
    Date d2 = DateTime.parse(s2);
    Assert.assertEquals(d1, d2);
    Assert.assertEquals(s2, DateTime.format(d1));
  }
  
  @Test
  public void testDecimalPlaces() {
    // Check that trailing zeros in the fractional seconds field are dropped.
    long ms = A_DATE - (A_DATE % 100);
    Date d = new Date(ms);
    String s = "2011-07-14T23:22:49.4Z";
    Assert.assertEquals(s, DateTime.format(d));
    Assert.assertEquals(d, DateTime.parse(s));
    
    // Check that extra decimal places are truncated when parsing.
    Assert.assertEquals(A_DATE, DateTime.parse("2011-07-14T23:22:49.47853495Z").getTime());
  }
  
  @Test
  public void testNonStrict() {
    Date d = new Date(A_DATE);
    nonStrict(d, "2011-7-14T23:22:49.478Z", 5); // omit leading zero
    nonStrict(d, "2011-007-14T23:22:49.478Z", 5); // extra leading zero
    nonStrict(d, "2011/07/14T23:22:49.478Z", 4); // non-standard delimiters
    nonStrict(d, "2011-07-14T19:22:49.478-04:00?", 29); // extra character at end
    nonStrict(d, "2011-07-15T23:22:49.478+24:00", 25); // too large timezone offset
    nonStrict(d, "2011-07-14T08:52:49.478-14:30", 28); // too large timezone offset
    nonStrict(d, "2011-06-44T23:22:49.478Z", 9); // wrap the days field
    nonStrict(d, "2011-07-13T47:22:49.478Z", 12); // wrap the hours field
    nonStrict(d, "2011-07-14T22:82:49.478Z", 15); // wrap the minutes field
    
    d = DateTime.parse("2011-07-14T23:22:29.478Z");
    nonStrict(d, "2011-07-14T23:21:89.478Z", 18); // wrap the seconds field
    
    d = DateTime.parse("2011-07-15T00:00:15Z");
    nonStrict(d, "2011-07-14T24:00:15Z", 18); // past midnight
  }
  
  @Test
  public void testInvalid() {
    invalid("2011-00-14T23:22:49.478Z", 6); // invalid month
    invalid("2011-13-14T23:22:49.478Z", 6); // invalid month
    invalid("2011-07-14T23::49.478Z", 14); // missing minutes
    invalid("2011-07-14T23:22:49.", 20); // end of input field
    invalid("2011-07-14T23:22:49?", 19); // ambiguous char at end
  }
  
  @Test
  public void testOverflow() {
    overflow("292277024-01-01T00:00:00Z");
    overflow("-292277024-01-01T00:00:00Z");
    overflow("-292277023-01-01T00:00:00Z");
    overflow("2010-10000000000-00T00:00:00Z");
    overflow(new Date(Long.MAX_VALUE));
    roundTrip("292277023-12-12T23:59:59Z");
    roundTrip("-292275054-01-01T00:00:00Z");
  }
  
  private static final String YR_FORMAT = "%04d-01-01T00:00:00Z";
  private static final String LEAP_YR_FORMAT = "%04d-02-29T00:00:00Z";
  
  @Test
  public void testLeapYears() {
    for (int yr = -5000; yr <= 5000; yr++) {
      roundTrip(format(YR_FORMAT, yr));
      String s = format(LEAP_YR_FORMAT, yr);
      if (DateTime.isLeapYear(yr)) {
        roundTrip(s);
      } else {
        try {
          DateTime.parse(s);
          Assert.fail("Should have thrown exception for non-leap year date: " + s);
        } catch (DateFormatException IGNORE) { /* expected failure */ }
      }
    }
  }
  
  private static String format(String fmt, int yr) {
    StringBuilder sb = new StringBuilder();
    if (yr < 0) {
      sb.append('-');
      yr = -yr;
    }
    Formatter f = new Formatter(sb);
    f.format(fmt, yr);
    return sb.toString();
  }
  
  private static void nonStrict(Date d, String s, int idx) {
    Assert.assertEquals(d, DateTime.parse(s, false));
    try {
      DateTime.parse(s);
      Assert.fail("Should have thrown parse exception for '" + s + "' with strict parsing.");
    } catch (DateFormatException e) {
      //System.out.println(e.getMessage());
      Assert.assertEquals(idx, e.getIndex());
    }
  }
  
  private static void invalid(String s, int idx) {
    try {
      DateTime.parse(s, false);
      Assert.fail("Should have thrown parse exception for '" + s + "' with strict parsing.");
    } catch (DateFormatException e) {
      //System.out.println(e.getMessage());
      Assert.assertEquals(idx, e.getIndex());
    }
  }
  
  private static void overflow(String s) {
    try {
      DateTime.parse(s);
      Assert.fail("Should have thrown overflow exception for '" + s + "'.");
    } catch (ArithmeticException e) {
      //System.out.println(e.getMessage());
    }
  }
  
  private static void overflow(Date d) {
    try {
      DateTime.format(d);
      Assert.fail("Should have thrown overflow exception for '" + d + "'.");
    } catch (ArithmeticException e) {
      //System.out.println(e.getMessage());
    }
  }
  
  private static void roundTrip(String s) {
    Date d = DateTime.parse(s);
    Assert.assertNotNull(d);
    String out = DateTime.format(d);
    Assert.assertEquals(s, out);
  }
  
}
