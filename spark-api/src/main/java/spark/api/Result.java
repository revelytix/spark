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

import java.io.Closeable;

/**
 * When a Command is executed, a Result is returned.
 */
public interface Result extends Closeable {

  /**
   * Is this result closed?
   * @return True if closed
   */
  boolean isClosed();
  
  /**
   * Get the command that was executed to create this result.
   * @return The command that created this result
   */
  Command getCommand();
  
}
