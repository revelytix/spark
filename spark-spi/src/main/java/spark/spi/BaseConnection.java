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

import spark.api.Connection;
import spark.api.DataSource;

/**
 * Base class for implementors of {@link Connection}.  This class manages a source DataSource
 * and a closed flag. 
 */
public abstract class BaseConnection implements Connection {

  private final DataSource dataSource;
  private volatile boolean closed = false;

  /**
   * Construct a BaseConnection with the source dataSource.
   * @param dataSource The creator of this connection
   */
  public BaseConnection(DataSource dataSource) {
    this.dataSource = dataSource;
  }
  
  @Override
  public DataSource getDataSource() {
    return this.dataSource;
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public void close() throws IOException {
    this.closed = true;
  }

}
