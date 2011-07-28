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

import java.util.Date;
import java.util.TimeZone;

/**
 * <p>
 * This class provides conversion between Java {@link Date} objects and the lexical form specified
 * for the XSD 1.1 <tt><a href="http://www.w3.org/TR/xmlschema11-2/#dateTime">dateTime</a></tt>
 * datatype.
 * </p>
 * 
 * <p>
 * While XSD <tt>dateTime</tt> representations are often used to encode Java date objects in RDF
 * (and vice versa) there are differences between the two. Most notably, XSD <tt>dateTime</tt>
 * represents dates using the ISO-8601 timeline, and dates on this timeline are measured relative
 * to the year 1 (0001-01-01T00:00:00Z). Java dates are measured in milliseconds relative to the
 * year 1970 (1970-01-01T00:00:00Z) but the exact calendar system they use is system-dependent.
 * We handle this by treating all Java dates as using the ISO-8601 timeline. While these two systems
 * will generally yield identical results for modern dates, there will likely be discrepancies
 * between how ISO-8601 and Java treat ancient dates (pre-1500s or so).
 * </p>
 * 
 * <p>
 * The discrepancy between date representations is mitigated by the fact that this class provides
 * stable round-tripping of date representations. For any Date <tt>d</tt> it is always true that
 * <pre>  d.equals(parse(format(d)))</pre>
 * and for any lexical string <tt>s</tt> it is always true that
 * <pre>  s.equals(format(parse(s)))</pre>
 * given that <tt>s</tt> is in normalized form, as described in the next paragraph.
 * </p>
 * 
 * <p>
 * The mapping from XSD <tt>dateTime</tt> string to Java date is not one-to-one. The following
 * information may be lost when parsing a <tt>dateTime</tt> lexical string:
 * <ul>
 *  <li>If the lexical string specifies a timezone offset, then the timezone offset will be applied
 *  as the date is converted but the timezone itself will be lost. When writing dates, the
 *  {@link #format(Date)} method assumes Zulu timezone ('Z' in the lexical form); all other timezone
 *  offsets will be lost. (The {@link #format(Date, TimeZone)} method allows you to include a
 *  timezone offset when converting to a lexical string, but there is no way of extracting the
 *  offset from the string.)</li>
 *  <li>If the lexical string omits the timezone offset, then it is considered to be relative to
 *  the local timezone and will be adjusted accordingly, but the fact that it is a local time will
 *  be lost.</li>
 *  <li>The lexical form allows the time portion to be <tt>24:00:00</tt> to indicate midnight, which
 *  is the same as the first moment of the following day. If the lexical string includes the
 *  midnight indicator, the resulting date will be written as <tt>00:00:00</tt> of the following
 *  day.</li>
 *  <li>The lexical form allows an unlimited number of decimal places for specifying fractional
 *  seconds; Java dates are limited to millisecond precision. Any decimal places after the first
 *  three will be truncated, and any trailing zeros will also be lost.</li>
 * </ul>
 * Any string which meets the XSD <tt>dateTime</tt> lexical constraints, and to which
 * <strong>none</strong> of the above four situations apply (i.e. it specifies the Zulu timezone,
 * does not include the midnight indicator, and has three or fewer decimal places in the fractional
 * seconds field with no trailing zeros) is considered to be in normalized form and is subject to
 * the round-tripping conditions described in the previous paragraph.
 * </p>
 * 
 * <p>
 * Conversion between XSD <tt>dateTime</tt> strings and Java dates is implemented by translating
 * between the ISO-8601 epoch (0001-01-01T00:00:00Z) and the Java epoch (1970-01-01T00:00:00Z) and
 * using the Time on Timeline algorithm described
 * <a href="http://www.w3.org/TR/xmlschema11-2/#vp-dt-timeOnTimeline">here</a>.
 * </p>
 * 
 * @author Alex Hall
 * @date Jul 11, 2011
 */
public class DateTime {

  // Zulu timezone; will use this by default.
  private static final TimeZone ZULU_TZ = TimeZone.getTimeZone("GMT+00:00");
  
  // Local timezone.
  private static final TimeZone LOCAL_TZ = TimeZone.getDefault();
  
  // Used for writing the milliseconds field.
  private static final int[] TENS = { 1, 10, 100 };
  
  private static final int FIRST_MONTH = 1; // January
  private static final int LAST_MONTH = 12; // December
  
  // Special month for leap years.
  private static final int FEBRUARY = 2;
  
  // The days for each month (zero-indexed)
  private static final int [] DAYS_IN_MONTH = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
  
  // The maximum year which will fit into a java.util.Date
  private static final int MAX_YEAR = 292277023;
  
  // Conversion factors.
  private static final int DAYS_IN_YEAR = 365;
  private static final int MINS_IN_HOUR = 60;
  private static final int MS_IN_SEC = 1000;
  private static final int MS_IN_MIN = 60000;
  private static final int MS_IN_HOUR = 3600000;
  private static final int MS_IN_DAY = 86400000;
  
  // Offset between the Java epoch (1970-01-01T00:00:00Z) and the ISO-8601 epoch (0001-01-01T00:00:00Z)
  private static final long EPOCH_OFFSET_MS = elapsedDays(1970) * MS_IN_DAY;
  
  // Separators in the lexical string.
  private static final char DATE_SEP = '-';
  private static final char DATE_TIME_SEP = 'T';
  private static final char TIME_SEP = ':';
  
  /**
   * Formats the given date as an XSD <tt>dateTime</tt> lexical string, using Zulu time.
   * @param d A date.
   * @return The corresponding lexical string.
   */
  public static String format(Date d) {
    return format(d, ZULU_TZ);
  }
  
  /**
   * <p>
   * Formats the given date as an XSD <tt>dateTime</tt> lexical string, using the offset specified
   * by the given timezone.
   * </p>
   * 
   * <p>
   * If no timezone is specified, then the given date is treated as a local time; it will be written
   * using the offset of the system default timezone, but the timezone offset will be omitted from
   * the lexical string. This is <strong>not</strong> recommended, as the resulting <tt>dateTime</tt>
   * will be relative to some unknown timezone and could be interpreted differently by different systems.
   * </p>
   * 
   * @param d A date.
   * @param tz A timezone.
   * @return The corresponding lexical string.
   */
  public static String format(Date d, TimeZone tz) {
    if (d == null) throw new IllegalArgumentException("Null date value");
    
    long offset = d.getTime();
    int tzOffsetMs = (tz != null ? tz : LOCAL_TZ).getOffset(d.getTime());
    if (offset > Long.MAX_VALUE - EPOCH_OFFSET_MS - tzOffsetMs) throw new ArithmeticException("Cannot convert to ISO-8601 offset.");
    offset += EPOCH_OFFSET_MS + tzOffsetMs;
    
    long da = offset / MS_IN_DAY;
    int millis = (int)(offset - (da * MS_IN_DAY));
    // Adjust for negative offsets.
    if (millis < 0) {
      da--;
      millis += MS_IN_DAY;
    }
    
    int year = (int)(da * 400 / 146097) + 1; // == da / 365.2425 + 1
    int day = (int)(da - elapsedDays(year)) + 1;
    while (day < 1) day += daysInYear(--year);
    int temp;
    while (day > (temp = daysInYear(year))) {
      day -= temp;
      year++;
    }
    
    int month = FIRST_MONTH;
    while (day > (temp = daysInMonth(year, month))) {
      day -= temp;
      month++;
    }
    
    int hour = millis / MS_IN_HOUR;
    millis -= hour * MS_IN_HOUR;
    int min = millis / MS_IN_MIN;
    millis -= min * MS_IN_MIN;
    int sec = millis / MS_IN_SEC;
    millis -= sec * MS_IN_SEC;

    StringBuilder sb = new StringBuilder();
    if (year < 0) {
      sb.append('-');
      year = -year;
    }
    
    append(sb, year, 4);
    sb.append(DATE_SEP);
    append(sb, month, 2);
    sb.append(DATE_SEP);
    append(sb, day, 2);
    sb.append(DATE_TIME_SEP);
    append(sb, hour, 2);
    sb.append(TIME_SEP);
    append(sb, min, 2);
    sb.append(TIME_SEP);
    append(sb, sec, 2);
    
    if (millis > 0) {
      sb.append('.');
      for (int i = 2; i >= 0 && millis > 0; i--) {
        sb.append(millis / TENS[i]);
        millis = millis % TENS[i];
      }
    }
    
    if (tz != null) {
      if (tzOffsetMs == 0) {
        sb.append('Z');
      } else {
        int tzOffset = tzOffsetMs / MS_IN_MIN; // XSD specifies minutes.
        sb.append(tzOffset < 0 ? '-' : '+');
        tzOffset = Math.abs(tzOffset);
        append(sb, tzOffset / MINS_IN_HOUR, 2);
        sb.append(TIME_SEP);
        append(sb, tzOffset % MINS_IN_HOUR, 2);
      }
    }
    return sb.toString();
  }
  
  /** Append the given value to the string builder, with leading zeros to result in the given minimum width. */
  private static void append(StringBuilder sb, int val, int width) {
    String s = Integer.toString(val);
    for (int i = s.length(); i < width; i++) sb.append('0');
    sb.append(s);
  }
  
  /**
   * Parse an XSD <tt>dateTime</tt> lexical string to a Java date, using strict parsing.
   * @param str The lexical string.
   * @return The corresponding Java date.
   * @throws DateFormatException if the string could not be parsed as an XSD <tt>dateTime</tt>.
   * @throws ArithmeticException if the date represented by the string will not fit into a Java date.
   */
  public static Date parse(String str) {
    return parse(str, true);
  }
  
  /**
   * Parse an XSD <tt>dateTime</tt> lexical string to a Java date. If <tt>strict</tt> is true, then
   * exceptions will be thrown if the lexical string does not exactly match the form specified by
   * XSD. If <tt>strict</tt> is false, then these constraints will be relaxed somewhat to allow such
   * things as alternate delimiters, omitting leading zeros, and out-of-range values for certain
   * fields (e.g. '2011-07-32' will be treated as the first day of the following month). 
   * @param str The lexical string.
   * @param strict Flag to indicate strict or lenient parsing.
   * @return The corresponding Java date.
   * @throws DateFormatException if the string could not be parsed as an XSD <tt>dateTime</tt>.
   * @throws ArithmeticException if the date represented by the string will not fit into a Java date.
   */
  public static Date parse(String str, boolean strict) {
    if (str != null) str = str.trim();
    if (str == null || str.isEmpty()) throw new IllegalArgumentException("String must be non-empty");

    Input s = new Input(str);
    boolean yrNeg = s.getChar() == '-';
    if (yrNeg) s.index++;
    
    int year = parseField("year", s, DATE_SEP, 4, 0, strict);
    // Always validate the year, or it won't fit in a long.
    if (year > MAX_YEAR) throw new ArithmeticException("The given year (" + (yrNeg ? "-" : "") + year + ") exceeds the limit for java.util.Date");
    if (yrNeg) year = -year;
    
    int month = parseField("month", s, DATE_SEP, 2, 2, strict);
    // Always validate the month
    if (month < FIRST_MONTH || month > LAST_MONTH) throw new DateFormatException("month out of range [1..12]", s.str, s.index - 2);
    
    int day = parseField("day", s, DATE_TIME_SEP, 2, 2, strict);
    if (strict && (day < 0 || day > daysInMonth(year, month))) throw new DateFormatException("day out of range [1.." + daysInMonth(year, month) + "]", s.str, s.index - 2);
    
    int hour = parseField("hour", s, TIME_SEP, 2, 2, strict);
    if (strict && hour > 24) throw new DateFormatException("hour out of range [0..24]", s.str, s.index - 2);
    
    int min = parseField("minute", s, TIME_SEP, 2, 2, strict);
    if (strict && min > 59) throw new DateFormatException("minutes out of range [0..59]", s.str, s.index - 2);
    
    int sec = parseField("second", s, null, 2, 2, strict);
    if (strict && sec > 59) throw new DateFormatException("seconds out of range [0..59]", s.str, s.index - 1);
    
    int millis = parseMillis(s);
    if (strict && hour == 24 && (min > 0 || sec > 0 || millis > 0)) {
      throw new DateFormatException("midnight indicator may not include non-zero minutes or seconds", s.str, s.index - 1);
    }
    
    Integer tzOffsetMs = parseTzOffsetMs(s, strict);
    boolean local = tzOffsetMs == null;
    if (local) tzOffsetMs = 0;
    if (strict && s.index < s.len) throw new DateFormatException("unexpected content, expected EOF", s.str, s.index);
    
    // Calculate the number of days elapsed between the epoch and the start of this day.
    long da = elapsedDays(year);
    for (int m = FIRST_MONTH; m < month; m++) {
      da += daysInMonth(year, m);
    }
    da += (day - 1);
    
    // Calculate the number of milliseconds elapsed between the epoch and the given time, accounting for timezone offset.
    long ms = da * MS_IN_DAY + hour * MS_IN_HOUR + min * MS_IN_MIN + sec * MS_IN_SEC + millis - tzOffsetMs;
    
    // Translate from ISO-8601 epoch to Java Date epoch.
    if (ms < (Long.MIN_VALUE + EPOCH_OFFSET_MS)) throw new ArithmeticException("The specified date will not fit into java.util.Date: " + str);
    ms -= EPOCH_OFFSET_MS;
    
    // Handle local timezone.
    if (local) {
      tzOffsetMs = LOCAL_TZ.getOffset(ms);
      if (tzOffsetMs > 0 && ms < (Long.MIN_VALUE + tzOffsetMs)) throw new ArithmeticException("The specified date will not fit into java.util.Date: " + str);
      ms -= tzOffsetMs;
    }
    
    return new Date(ms);
  }
  
  /** Find the number of elapsed days from the epoch to the beginning of the given year. */
  private static long elapsedDays(int year) {
    int y = year - 1;
    return DAYS_IN_YEAR * (long)y + div(y, 400) - div(y, 100) + div(y, 4);
  }
  
  /**
   * Implementation of the XSD div operation, which differs from Java integer division in that it rounds
   * down for negative quotients while Java rounds up, i.e.:
   *   -1 / 2 == 0
   *   div(-1, 2) == -1
   */
  private static int div(int a, int b) {
    return a > 0 ? (b > 0 ? a / b : (a - b - 1) / b)
                 : (b > 0 ? (a - b + 1) / b : a / b);
  }
  
  /** Find the number of days in the given month, given the year. */
  private static int daysInMonth(int year, int month) {
    assert month >= FIRST_MONTH && month <= LAST_MONTH;
    int d = DAYS_IN_MONTH[month - 1];
    if (month == FEBRUARY && isLeapYear(year)) d++;
    return d;
  }
  
  /** Find the number of days in the given year, taking leap years into account. */
  private static int daysInYear(int year) {
    return isLeapYear(year) ? DAYS_IN_YEAR + 1 : DAYS_IN_YEAR;
  }
  
  /** Determine whether the given year is a leap year. */
  static boolean isLeapYear(int year) {
    return (year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0));
  }
  
  /** Parse the fractional seconds field from the input, returning the number of milliseconds and truncating extra places. */
  private static int parseMillis(Input s) {
    if (s.index < s.len && s.getChar() == '.') {
      int startIndex = ++s.index;
      int ms = parseInt(s);
      int len = s.index - startIndex;
      for (; len < 3; len++) ms *= 10;
      for (; len > 3; len--) ms /= 10; // truncate because it's easier than rounding.
      return ms;
    }
    return 0;
  }
  
  /** Parse the timezone offset from the input, returning its millisecond value. */
  private static Integer parseTzOffsetMs(Input s, boolean strict) {
    if (s.index < s.len) {
      char c = s.getChar();
      s.index++;
      int sign;
      if (c == 'Z') {
        return 0;
      } else if (c == '+') {
        sign = 1;
      } else if (c == '-') {
        sign = -1;
      } else {
        throw new DateFormatException("unexpected character, expected one of [Z+-]", s.str, s.index - 1);
      }
      int tzHours = parseField("timezone hours", s, TIME_SEP, 2, 2, strict);
      if (strict && tzHours > 14) throw new DateFormatException("timezone offset hours out of range [0..14]", s.str, s.index - 2);
      
      int tzMin = parseField("timezone minutes", s, null, 2, 2, strict);
      if (strict && tzMin > 59) throw new DateFormatException("timezone offset hours out of range [0..59]", s.str, s.index - 1);
      if (strict && tzHours == 14 && tzMin > 0) throw new DateFormatException("timezone offset may not be greater than 14 hours", s.str, s.index - 1);
      
      return sign * (tzHours * MINS_IN_HOUR + tzMin) * MS_IN_MIN;
    }
    // Reached the end of input with no timezone specified.
    return null;
  }
  
  /** Parse a field from input, validating its delimiter and length if requested. */
  private static int parseField(String field, Input s, Character delim, int minLen, int maxLen, boolean strict) {
    int startIndex = s.index;
    int result = parseInt(s);
    if (startIndex == s.index) throw new DateFormatException("missing value for field '" + field + "'", s.str, startIndex);
    if (strict) {
      int len = s.index - startIndex;
      if (len < minLen) throw new DateFormatException("field '" + field + "' must be at least " + minLen + " digits wide", s.str, startIndex);
      if (maxLen > 0 && len > maxLen) throw new DateFormatException("field '" + field + "' must be no more than " + minLen + " digits wide", s.str, startIndex);
    }
    if (delim != null) {
      if (s.index >= s.len) throw new DateFormatException("unexpected end of input", s.str, s.index);
      if (strict && s.getChar() != delim.charValue()) throw new DateFormatException("unexpected character, expected '" + delim + "'", s.str, s.index);
      s.index++;
    }
    return result;
  }
  
  /** Parse an integer from the input, reading up to the first non-numeric character. */
  private static int parseInt(Input s) {
    if (s.index >= s.len) throw new DateFormatException("unexpected end of input", s.str, s.index);
    int result = 0;
    while (s.index < s.len) {
      char c = s.getChar();
      if (c >= '0' && c <= '9') {
        if (result >= Integer.MAX_VALUE / 10) throw new ArithmeticException("Field too large.");
        result = result * 10 + ((int)c - (int)'0');
        s.index++;
      } else {
        break;
      }
    }
    return result;
  }
  
  /** Class to wrap the input string so that its index may be advanced by helper methods. */
  private static class Input {
    int index = 0;
    final String str;
    final int len;
    Input(String s) {
      this.str = s;
      this.len = s.length();
    }
    char getChar() { return str.charAt(index); }
  }
  
}
