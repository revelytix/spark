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
package spark.api.exception;

/**
 * A generic exception indicating an error in the Spark api. 
 */
public class SparqlException extends RuntimeException {

  private static final long serialVersionUID = -7174855352655879684L;

  /**
   * Construct a default exception
   */
  public SparqlException() {
    super();
  }

  /**
   * Construct an exception with a message and a chained Throwable
   * @param message Message
   * @param throwable Chained throwable
   */
  public SparqlException(String message, Throwable throwable) {
    super(message, throwable);
  }

  /**
   * Construct an exception with a message.
   * @param message Message
   */
  public SparqlException(String message) {
    super(message);
  }

  /**
   * Construct an exception with a chained Throwable.
   * @param throwable Chained throwable
   */
  public SparqlException(Throwable throwable) {
    super(throwable);
  }  
}