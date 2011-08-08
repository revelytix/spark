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
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.api.Command;
import spark.api.Connection;
import spark.api.DataSource;
import spark.api.Solutions;
import spark.api.credentials.NoCredentials;

/**
 * @author Alex Hall
 * @created Aug 2, 2011
 */
public class SparqlConnections {
  
  private static final Logger logger = LoggerFactory.getLogger(SparqlConnections.class);
  
  private static final URL TEST_URL;
  static {
    try {
      TEST_URL = new URL("http://DBpedia.org/sparql");
    } catch (MalformedURLException e) {
      throw new Error("Invalid test URL", e);
    }
  }
  
  private static final int REPETITIONS = 10;
  private static final int NUM_THREADS = 5;
  private static final Random RANDOM = new Random();
  private static final int MAX_SLEEP = 500;

  static void sleep() {
    try {
      Thread.sleep(RANDOM.nextInt(MAX_SLEEP));
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException("Interrupted!", e);
    }
  }
  
  static class TestWorker implements Runnable {
    private final Runnable task;
    private final boolean[] flags;
    private final int index;
    private final AtomicLong timer;
    private final AtomicInteger counter;
    
    public TestWorker(Runnable task, boolean[] flags, int index, AtomicLong timer, AtomicInteger counter) {
      this.task = task;
      this.flags = flags;
      this.index = index;
      this.timer = timer;
      this.counter = counter;
      flags[index] = false;
    }

    public void run() {
      try {
        for (int i = 0; i < REPETITIONS; i++) {
          if (i > 0) sleep();
          long begin = System.currentTimeMillis();
          task.run();
          long elapsed = System.currentTimeMillis() - begin;
          timer.addAndGet(elapsed);
          counter.incrementAndGet();
        }
      } catch (Throwable t) {
        t.printStackTrace();
      } finally {
        synchronized(flags) {
          flags[index] = true;
          flags.notify();
        }
      }
    }
  }
  
  static interface DataSourceFactory {
    DataSource getSource();
  }
  
  private static boolean allTrue(boolean[] bb) {
    for (boolean b : bb) if (!b) return false;
    return true;
  }
  
  static void doTest(String test, final DataSourceFactory f) {
    boolean[] done = new boolean[NUM_THREADS];
    final AtomicLong timer = new AtomicLong();
    final AtomicInteger counter = new AtomicInteger();

    Runnable r = new Runnable() {
      public void run() {
        Connection c = f.getSource().getConnection(NoCredentials.INSTANCE);
        Command cmd = c.createCommand("SELECT ?p ?o WHERE { <http://dbpedia.org/resource/Terry_Gilliam> ?p ?o }");
        cmd.setTimeout(30);
        try {
          Solutions s = cmd.executeQuery();
          Assert.assertNotNull(s);
          try {
            Assert.assertEquals(Arrays.asList("p", "o"), s.getVariables());
            int count = 0;
            while (s.next()) {
              count++;
              Assert.assertNotNull(s.getBinding("p"));
              Assert.assertNotNull(s.getBinding("o"));
            }
            Assert.assertEquals(121, count);
          } finally {
            try {
              s.close();
            } catch (IOException e) {
              throw new RuntimeException("Error closing solutions.", e);
            }
          }
        } finally {
          try {
            c.close();
          } catch (IOException e) {
            throw new RuntimeException("Error closing connection.", e);
          }
        }
      }
    };
    
    for (int i = 0; i < NUM_THREADS; i++) {
      new Thread(new TestWorker(r, done, i, timer, counter)).start();
    }
    
    synchronized(done) {
      while (!allTrue(done)) {
        try {
          done.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
          throw new RuntimeException("Interrupted!", e);
        }
      }
    }
    
    Assert.assertTrue(allTrue(done));
    int count = counter.get();
    Assert.assertEquals(NUM_THREADS * REPETITIONS, count);
    long time = timer.longValue();
    logger.debug("{} total time (ms):   {}", test,  time);
    logger.debug("{} average time (ms): {}\n", test, (double)time / count);
  }
  
  static final DataSourceFactory SIMPLE_FACTORY = new DataSourceFactory() {
    public DataSource getSource() {
      return new ProtocolDataSource(TEST_URL);
    }
  };
  
  static final ThreadLocal<DataSource> THREAD_SOURCE = new ThreadLocal<DataSource>() {
    protected DataSource initialValue() {
      return new ProtocolDataSource(TEST_URL);
    }
  };
  static final DataSourceFactory PER_THREAD_FACTORY = new DataSourceFactory() {
    public DataSource getSource() {
      return THREAD_SOURCE.get();
    }
  };
  
  static final DataSource POOLED_SOURCE = new ProtocolDataSource(TEST_URL);
  static final DataSourceFactory POOLED_FACTORY = new DataSourceFactory() {
    public DataSource getSource() {
      return POOLED_SOURCE;
    }
  };
  
  public static void main(String[] args) {
    doTest("Global pooling", POOLED_FACTORY);
    doTest("Per-thread pooling", PER_THREAD_FACTORY);
    doTest("No pooling", SIMPLE_FACTORY);
    doTest("Global pooling", POOLED_FACTORY);
    doTest("Per-thread pooling", PER_THREAD_FACTORY);
    doTest("No pooling", SIMPLE_FACTORY);
  }
}
