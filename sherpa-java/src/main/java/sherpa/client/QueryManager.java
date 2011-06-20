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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.avro.AvroRemoteException;

import sherpa.protocol.CancelRequest;
import sherpa.protocol.CloseRequest;
import sherpa.protocol.DataRequest;
import sherpa.protocol.DataResponse;
import sherpa.protocol.ErrorResponse;
import sherpa.protocol.Query;
import sherpa.protocol.QueryRequest;
import sherpa.protocol.QueryResponse;
import spark.api.exception.SparqlException;

/**
 * Manages the state associated with cursoring through a single query.  Each instance is 
 * designed for a single execution and should not be reused.
 */
public class QueryManager implements Iterable<List<Object>> {

  // Query properties
  public final static String BATCH_SIZE = "batchSize"; 
  
  // Resources
  private final Query queryApi;

  // query metadata - doesn't change after the query starts
  private CharSequence queryId;
  private List<String> vars;
  private int maxBatchSize = 1000;

  // query state as cursor evolves, protected by "this" lock
  private int cursor = 0; // overall result set, 1-based
  private Window currentData = Window.EMPTY;
  private final BlockingQueue<Window> nextQueue = new LinkedBlockingQueue<Window>();

  public QueryManager(Query clientInterface) {
    this.queryApi = clientInterface;    
  }

  public void query(String command, Map<String, String> params,
      Map<String, String> props) {
    
    if(props != null && props.containsKey(BATCH_SIZE)) {
      this.maxBatchSize = Integer.parseInt(props.get(BATCH_SIZE));
    }
    
    QueryRequest request = new QueryRequest();
    request.sparql = command;
    request.parameters = new HashMap<CharSequence, CharSequence>();
    request.properties = new HashMap<CharSequence, CharSequence>();
    try {
      System.out.println("Client sending query request to server.");
      QueryResponse response = queryApi.query(request);
      System.out.println("Client received query response from server.");
      queryId = response.queryId;
      vars = new ArrayList<String>();
      for (CharSequence cs : response.vars) {
        vars.add(cs.toString());
      }
    } catch (ErrorResponse e) {
      throw new SparqlException("Got error querying server.", e);
    } catch (AvroRemoteException e) {
      throw new SparqlException("Got error communicating with server.", e);
    }

    asyncMore(1);
  }

  /**
   * Fire request for a batch (async) or block on the existing one.
   * 
   * @param startRow
   *          Start row needed in return batch
   */
  private void more(int startRow) {
    try {
      DataRequest moreRequest = new DataRequest();
      moreRequest.queryId = queryId;
      moreRequest.startRow = startRow;
      moreRequest.maxSize = maxBatchSize;
      System.out.println("Client requesting " + startRow + " .. "
          + (startRow + maxBatchSize - 1));

      DataResponse response = queryApi.data(moreRequest);
      System.out.println("Client got response " + response.startRow + " .. "
          + (response.startRow + response.data.size() - 1) + ", more="
          + response.more);
      try {
        nextQueue.put(new Window(response.data, response.more));
      } catch (InterruptedException e) {
        // Reset interrupted state but don't try to handle it
        Thread.currentThread().interrupt();
      }

    } catch (ErrorResponse e) {
      System.err.println(e.message);
      e.printStackTrace();
      // TODO - manage error
    } catch (AvroRemoteException e) {
      e.printStackTrace();
      // TODO - manage error
    }
  }

  private final Executor executor = Executors.newFixedThreadPool(1);

  
  private synchronized void asyncMore(final int startRow) {
    if(currentData.more) {
      executor.execute(new Runnable() {
        public void run() {
          more(startRow);
        }
      });
    }
  }

  private synchronized void blockForData() {
    try {
      currentData = nextQueue.take(); // blocking dequeue
    } catch (InterruptedException e) {
      throw new SparqlException("Unexpected interruption");
    }
  }

  public synchronized boolean incrementCursor() {
    System.out.println("..incrementCursor(), cursor=" + cursor);
    while (true) {
      if (currentData.inc()) {  // Stay in the current batch
        System.out.println("....incrementing in currentData");
        cursor++;
        return true;
      } else {
        Window nextData = nextQueue.poll(); // non-blocking take, null if empty        
        if (nextData != null) { // Switch to next batch
          System.out.println("....switching to next batch, cursor=" + cursor + ", nextData size=" + currentData.data.size());
          this.currentData = nextData;      
          asyncMore(cursor + currentData.data.size() + 1);
          
        } else { // Don't have data
          if (!currentData.more) { // Because we're done
            System.out.println("....no current data, but we're all done.");
            cursor++;
            return false;
          } else { // Or we just haven't waited long enough for it
            System.out.println("....no current data, no next data, but not done, just wait.");
            blockForData();            
            System.out.println("....switching to next batch, cursor=" + cursor + ", nextData size=" + currentData.data.size());
            asyncMore(cursor + currentData.data.size() + 1);
          }
        }
      }
    }
  }

  public List<String> getVars() {
    return this.vars;
  }

  public void cancel() {
    CancelRequest cancelRequest = new CancelRequest();
    cancelRequest.queryId = queryId;

    try {
      queryApi.cancel(cancelRequest);
    } catch (ErrorResponse e) {
      System.err.println(e.message);
      e.printStackTrace();
      // TODO - manage error
    } catch (AvroRemoteException e) {
      e.printStackTrace();
      // TODO - manage error
    }
  }

  public void close() {
    CloseRequest closeRequest = new CloseRequest();
    closeRequest.queryId = queryId;

    try {
      queryApi.close(closeRequest);
    } catch (ErrorResponse e) {
      System.err.println(e.message);
      e.printStackTrace();
      // TODO - manage error
    } catch (AvroRemoteException e) {
      e.printStackTrace();
      // TODO - manage error
    }
  }

  public synchronized List<Object> getRow() {
    return this.currentData.getData();
  }

  public synchronized int getCursor() {
    return this.cursor;
  }

  @Override
  public Iterator<List<Object>> iterator() {
    return new QueryIterator();
  }

  private class QueryIterator implements Iterator<List<Object>> {
    private boolean incremented = false;
    private boolean hasNext = true;
        
    @Override
    public boolean hasNext() {         
      if(! incremented) {
        hasNext = incrementCursor();
        incremented = true;
      }
      return hasNext;
    }

    @Override
    public List<Object> next() {
      if(! incremented) {
        hasNext = incrementCursor();
      }
      
      incremented = false;  // reset for next 
      if(hasNext) {        
        return getRow();
      } else {
        return null;
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Cannot remove from a QueryIterator.");
    }
  }
  
  private static class Window {
    final List<List<Object>> data;
    final boolean more;
    private int index = -1;

    Window(List<List<Object>> data, boolean more) {
      this.data = data;
      this.more = more;
    }

    /**
     * If there is still data in the window, increment the row and return true,
     * otherwise false.
     * 
     * @return True if data is available at the current index, false if this
     *         window is fully read
     */
    boolean inc() {
      index++;
      return index < data.size();
    }

    public List<Object> getData() {
      System.out.println("..get row at " + index);
      return data.get(index);
    }

    private static List<List<Object>> EMPTY_LIST = Collections.emptyList();
    static Window EMPTY = new Window(EMPTY_LIST, true);
  }

}
