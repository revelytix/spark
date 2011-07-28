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
package spark.api;

/**
 * A result that provides a cursored view of a result set.  The cursor is 
 * traversed using {@link #next()}.  The cursor result index can be retrieved with 
 * {@link #getRow()} and is 1-based.  The instance is initialized at result 0,
 * before the first result.
 * @param <R> Each result is of type R
 */
public interface CursoredResult <R> extends Result, Iterable<R> {

  /**
   * Move to the next result and return true if the cursor is at a valid result. 
   * @return True if result, false if walked off the end of the results
   */
  boolean next();
  
  /**
   * Get the current result index.  The index is 1-based, similar to JDBC's ResultSet interface, 
   * and starts at index=0 before the first result.
   * @return 1-based result index
   */
  int getRow();
  
  /**
   * Gets the current result. Calling this method when the cursor is not on a valid row (i.e. before
   * the first result or after the last result) yields unspecified results.
   * @return The current result.
   */
  R getResult();
  
  /**
   * True when result is initialized and result index is at 0.  
   * @return True if before first result, false otherwise
   */
  boolean isBeforeFirst();
  
  /**
   * True when result index is 1 and there is a current result.
   * @return True when result index is 1 and there is a current result.
   */
  boolean isFirst();
  
  /**
   * True when result index is on the last result.
   * @return True when cursor is on last result
   */
  boolean isLast();
  
  /**
   * True when result index has walked past the last result.
   * @return True when cursor is past the last result
   */
  boolean isAfterLast();
  
}