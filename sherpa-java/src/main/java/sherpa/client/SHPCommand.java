/*
 * Copyright 2011 Revelytix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sherpa.client;

import java.util.HashMap;
import java.util.Map;

import spark.api.Command;
import spark.api.Connection;
import spark.api.Result;
import spark.api.Solutions;
import spark.spi.BaseCommand;

public class SHPCommand extends BaseCommand implements Command {
  
  private QueryManager queryMgr;
  private int batchSize;
  
  public SHPCommand(Connection connection, String command, QueryManager queryMgr) {
    super(connection, command);
    this.queryMgr = queryMgr;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }
  
  @Override
  public void cancel() {
    this.queryMgr.cancel();
  }

  @Override
  public Result execute() {    
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean executeAsk() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Solutions executeQuery() {
    Map<String,String> params = new HashMap<String,String>();
    Map<String,String> props = new HashMap<String,String>();
    props.put(QueryManager.BATCH_SIZE, "" + batchSize);
    queryMgr.query(getCommand(), params, props);
    return new SHPSolutions(this, queryMgr);
  }

}
