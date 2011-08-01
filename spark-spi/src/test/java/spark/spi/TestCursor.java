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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import spark.api.CursoredResult;

/**
 * Utility for testing cursor positions for instances of {@link CursoredResult}.
 * 
 * @author Alex Hall
 * @created Aug 1, 2011
 */
public class TestCursor {

  /** Return an empty test suite so JUnit doesn't complain. */
  public static Test suite() {
    return new TestSuite();
  }
  
  /** State when none of <tt>isBeforeFirst</tt>, <tt>isFirst</tt>, <tt>isLast</tt>, or <tt>isAfterLast</tt> is true. */
  public static final int NONE = 0;
  
  /** Flag to indicate that {@link CursoredResult#isBeforeFirst()} should be true. */
  public static final int BEFORE_FIRST = 1;
  
  /** Flag to indicate that {@link CursoredResult#isFirst()} should be true. */
  public static final int FIRST = 2;
  
  /** Flag to indicate that {@link CursoredResult#isLast()} should be true. */
  public static final int LAST = 4;
  
  /** Flag to indicate that {@link CursoredResult#isAfterLast()} should be true. */
  public static final int AFTER_LAST = 8;
  
  /**
   * Asserts that the current state of the result object matches the given state.
   * @param result The result.
   * @param state The expected state; a bitwise-or combination of the appropriate flags
   * ({@link #BEFORE_FIRST}, {@link #FIRST}, {@link #LAST}, and {@link #AFTER_LAST}), or {@link #NONE}.
   * The absence of a flag is taken to mean that the corresponding method should return false.
   */
  public static void assertCursor(CursoredResult<?> result, int state) {
    Assert.assertEquals((state & BEFORE_FIRST) > 0, result.isBeforeFirst());
    Assert.assertEquals((state & FIRST) > 0, result.isFirst());
    Assert.assertEquals((state & LAST) > 0, result.isLast());
    Assert.assertEquals((state & AFTER_LAST) > 0, result.isAfterLast());
  }
}
