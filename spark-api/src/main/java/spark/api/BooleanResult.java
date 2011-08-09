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
 * <p>{@link Result} interface to wrap the result of executing a SPARQL ASK query so that it can
 * be returned from a call to {@link Command#execute()}. An ASK query takes a query pattern and
 * tests whether a solution for the pattern exists in the dataset. It always returns a single
 * boolean result: <tt>true</tt> if a solution exists for the pattern, or <tt>false</tt> if no
 * solution is known to exist.</p>
 * 
 * <p>This interface is designed to be returned from the {@link Command#execute()} method for
 * executing general SPARQL commands. If you have prior knowledge that a command is an ASK query,
 * then you can access the boolean result directy by calling the {@link Command#executeAsk()}
 * method.</p>
 * 
 * @author Alex Hall
 * @created Aug 9, 2011
 */
public interface BooleanResult extends Result {

  /**
   * Gets the boolean result value that was produced by executing the ASK query.
   * 
   * @return <tt>true</tt> if a solution exists for the query pattern, or <tt>false</tt> if no
   * solution is known to exist.</p>
   */
  boolean getResult();
}
