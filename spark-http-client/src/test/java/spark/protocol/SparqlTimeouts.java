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
package spark.protocol;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import spark.api.Command;
import spark.api.Connection;
import spark.api.DataSource;
import spark.api.Solutions;
import spark.api.credentials.NoCredentials;

/**
 * Test cases for timeouts and cancellation. These were run against a modified Mulgara server
 * running on localhost with a built-in 10 second delay when answering queries.
 * 
 * @author Alex Hall
 * @created Aug 5, 2011
 */
public class SparqlTimeouts {

  private static final URL url;
  static {
    try {
      url = new URL("http://localhost:8080/sparql");
    } catch (MalformedURLException e) {
      throw new Error(e);
    }
  }
  
  private static final String query = "SELECT ?class FROM <test:beer> WHERE { ?class a <http://www.w3.org/2002/07/owl#Class> }";
  
  static void log(String s) {
    System.out.println(new Date() + ": " + s + " in thread: " + Thread.currentThread().getName());
  }
  
  static void exec(Command cmd) {
    try {
      log("submitting query");
      Solutions s = cmd.executeQuery();
      log("got response");
      try {
        int count = 0;
        while (s.next()) count++;
        log("found " + count + " solutions");
      } finally {
        s.close();
      }
    } catch (Exception e) {
      log("Exception (" + e.getMessage() + ")");
    }
  }
  
  static void doQuery(DataSource ds, long timeout) {
    log("getting connection");
    Connection c = ds.getConnection(NoCredentials.INSTANCE);
    try {
      Command cmd = c.createCommand(query);
      cmd.setTimeout(timeout);
      exec(cmd);
    } catch (Exception e) {
      log("Exception (" + e.getMessage() + ")");
    } finally {
      try {
        c.close();
      } catch (IOException e) {
        log("IOException (" + e.getMessage() + ")");
      }
    }
  }
  
  public static void testTimeout() {
    ProtocolDataSource ds = new ProtocolDataSource(url);
    ds.setConnectionPoolSize(1);
    doQuery(ds, Command.NO_TIMEOUT);
    doQuery(ds, 5);
    doQuery(ds, Command.NO_TIMEOUT);
    ds.close();
  }

  public static void testAcquireTimeout() throws Exception {
    final ProtocolDataSource ds = new ProtocolDataSource(url);
    ds.setConnectionPoolSize(3);
    ds.setAcquireTimeout(5);
    int threads = 5;
    final CountDownLatch latch = new CountDownLatch(threads);
    for (int i = 0; i < threads; i++) {
      new Thread(new Runnable() {
        public void run() {
          doQuery(ds, Command.NO_TIMEOUT);
          latch.countDown();
        }
      }).start();
    }
    latch.await();
    ds.close();
  }
  
  public static void testCancel() throws Exception {
    final ProtocolDataSource ds = new ProtocolDataSource(url);
    ds.setConnectionPoolSize(1);
    Connection conn = ds.getConnection(NoCredentials.INSTANCE);
    final Command c = conn.createCommand(query);
    final Command c2 = conn.createCommand(query);
    
    final CountDownLatch done = new CountDownLatch(2);
    new Thread(new Runnable() {
      public void run() {
        exec(c);
        done.countDown();
      }
    }).start();
    new Thread(new Runnable() {
      public void run() {
        exec(c2);
        done.countDown();
      }
    }).start();
    
    Thread.sleep(5000);
    log("canceling query 1");
    c.cancel();
    c2.cancel();
    done.await();
    log("query 1 thread done");
    
    final CountDownLatch query = new CountDownLatch(1);
    final CountDownLatch cancel = new CountDownLatch(1);
    final CountDownLatch done2 = new CountDownLatch(1);
    new Thread(new Runnable() {
      public void run() {
        try {
          log("executing query 2");
          Solutions s = c.executeQuery();
          log("query 2 done");
          try {
            query.countDown();
            cancel.await();
            log("getting query 2 result");
            int count = 0;
            while (s.next()) count++;
            log("query 2 found " + count + "results");
          } catch (InterruptedException e) {
            log("interrupted!");
          } finally {
            try {
              s.close();
            } catch (IOException e) {
              log("IOException (" + e + ")");
            }
          }
        } finally {
          done2.countDown();
        }
      }
    }).start();
    
    query.await();
    log("canceling query 2");
    c.cancel();
    cancel.countDown();
    done2.await();
    log("query 2 thread done");
    
    exec(c);
    ds.close();
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    testTimeout();
    testAcquireTimeout();
    testCancel();
  }

}
