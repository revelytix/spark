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

import spark.api.BooleanResult;
import spark.api.Command;

/**
 * Default implementation class for the {@link BooleanResult} interface.
 * 
 * @author Alex Hall
 * @created Aug 9, 2011
 */
public class BooleanResultImpl extends BaseResults implements BooleanResult {

  private final boolean result;
  
  /**
   * Initializes a boolean result with the given value.
   * @param command The command that created the result.
   * @param result The result value.
   */
  public BooleanResultImpl(Command command, boolean result) {
    super(command);
    this.result = result;
  }

  /* (non-Javadoc)
   * @see spark.api.BooleanResult#getResult()
   */
  @Override
  public boolean getResult() {
    return result;
  }

}
