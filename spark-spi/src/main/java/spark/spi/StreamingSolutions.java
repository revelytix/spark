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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import spark.api.Command;
import spark.api.Solutions;
import spark.api.rdf.RDFNode;

/**
 * Abstract base class for streaming implementations of the {@link Solutions} interface. Fetches
 * result rows one at a time as needed to satisfy client requests.
 * 
 * @author Alex Hall
 * @date Jul 28, 2011
 */
public abstract class StreamingSolutions extends BaseSolutions implements Solutions {

  protected static final int BEFORE_FIRST = 0;
  protected static final int FIRST = 1;
  
  private int cursor = BEFORE_FIRST;
  private Map<String,RDFNode> currentRow = null;
  
  /**
   * @param command The command which created these solutions.
   * @param vars The variables contained in the solutions.
   */
  public StreamingSolutions(Command command, List<String> vars) {
    super(command, vars);
  }

  @Override
  public boolean next() {
    currentRow = fetchNext();
    cursor++;
    return currentRow != null;
  }

  @Override
  public int getRow() {
    return cursor;
  }

  @Override
  public Map<String, RDFNode> getResult() {
    return currentRow;
  }

  @Override
  public boolean isBeforeFirst() {
    return cursor == BEFORE_FIRST;
  }

  @Override
  public boolean isFirst() {
    return cursor == FIRST && currentRow != null;
  }

  @Override
  public boolean isAfterLast() {
    return currentRow == null && cursor > BEFORE_FIRST;
  }

  @Override
  public Iterator<Map<String, RDFNode>> iterator() {
    return new StreamingIterator();
  }

  /** Iterator over the solutions. */
  private class StreamingIterator implements Iterator<Map<String,RDFNode>> {
    
    private boolean incremented = false;

    @Override
    public boolean hasNext() {
      try {
        return incremented ? currentRow != null : StreamingSolutions.this.next();
      } finally {
        incremented = true;
      }
    }

    @Override
    public Map<String, RDFNode> next() {
      try {
        if (!incremented) StreamingSolutions.this.next();
        return currentRow;
      } finally {
        incremented = false;
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Cannot remove from a streaming iterator.");
    }
    
  }
  
  /**
   * Fetch the next row in the solution set from the query processor.
   * @return The next row of results, or null if we're past the end of the results.
   */
  protected abstract Map<String, RDFNode> fetchNext();
}
