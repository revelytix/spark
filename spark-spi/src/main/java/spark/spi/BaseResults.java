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

import java.io.IOException;

import spark.api.Command;
import spark.api.Result;
import spark.api.Solutions;
import spark.api.Triples;

/**
 * Base class for SPIs implementing {@link Solutions}, {@link Triples}, etc.  This 
 * just manages a closed flag and a Command.
 */
public abstract class BaseResults implements Result {
  
  private final Command command;
  private volatile boolean closed;
  
  /**
   * Create a base results that manages a Command
   * @param command The command
   */
  public BaseResults(Command command) {
    this.command = command;
  }
  
  @Override
  public Command getCommand() {
    return this.command;
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  @Override
  public void close() throws IOException {
    this.closed = true;
  }

}
