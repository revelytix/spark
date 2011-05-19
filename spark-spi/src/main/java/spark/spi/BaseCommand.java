package spark.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import spark.api.Command;
import spark.api.Connection;
import spark.api.rdf.RDFNode;

public abstract class BaseCommand implements Command {

  // immutable
  private final Connection connection;
  private final String command;
  
  // volatile
  private volatile boolean closed = false;
  private volatile long timeout = NO_TIMEOUT;
  
  // mutable, protected by "this" lock 
  private List<Map<String, RDFNode>> bindings;
  
  public BaseCommand(Connection connection, String command) {
    this.connection = connection;
    this.command = command;
    clearBindings();
  }
  
  @Override
  public String getCommand() {
    return this.command;
  }

  @Override
  public Connection getConnection() {
    return this.connection;
  }
  
  @Override
  public synchronized void addParameterBindings(Map<String, RDFNode> binding) {
    this.bindings.add(binding);
  }

  @Override
  public synchronized List<Map<String, RDFNode>> getBindings() {
    return this.bindings;
  }
  
  @Override
  public synchronized void clearBindings() {
    this.bindings = new ArrayList<Map<String, RDFNode>>();
  }
  
  @Override
  public void close() {
    this.closed = true;
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  @Override
  public long getTimeout() {
    return this.timeout;
  }
  
  @Override
  public void setTimeout(long seconds) {
    if(seconds < 0) {
      throw new IllegalArgumentException("Timeout must be >= 0: " + seconds);
    }
    this.timeout = seconds;
  }


}

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
