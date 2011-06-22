package sherpa.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;


public class TestSignalSlot {

  @Test
  public void testRepeatedNonBlockingUse() {
    SignalSlot<String> slot = new SignalSlot<String>();
    
    // nothing here yet, but don't block
    assertEquals(null, slot.poll());
    
    slot.add("abc");
    assertEquals("abc", slot.poll());
    assertEquals(null, slot.poll());
    
    slot.add("def");
    assertEquals("def", slot.poll());
    assertEquals(null, slot.poll());    
  }
  
  @Test
  public void testBlocking_produceBeforeConsume() {
    final SignalSlot<String> slot = new SignalSlot<String>();    
    slot.add("abc");
    
    assertEquals("abc", slot.take());
    assertEquals(null, slot.poll());
  }
  
  @Test
  public void testBlocking_consumeBlockingOnProduce() throws Throwable {
    final SignalSlot<String> slot = new SignalSlot<String>();
    final CountDownLatch latchEnd = new CountDownLatch(1);    
    final List<String> results = new ArrayList<String>();    

    // CONSUMER
    new Thread(new Runnable() {
      public void run() {
        // Block for data
        results.add(slot.take());

        // Notify main thread that we're done
        latchEnd.countDown();        
      }
    }).start();

    // Wait for consumer thread to start and block
    Thread.sleep(500);

    // Put data in the slot
    slot.add("abc");

    latchEnd.await();
    assertEquals(1, results.size());
    assertEquals("abc", results.get(0));
  }
  
  @Test
  public void testBlocking_interruptBlocking() throws Throwable {
    final SignalSlot<String> slot = new SignalSlot<String>();
    final CountDownLatch latchEnd = new CountDownLatch(1);    
    final List<Object> results = new ArrayList<Object>();    

    // CONSUMER
    new Thread(new Runnable() {
      public void run() {
        // Block for data
        try {
          results.add(slot.take());
        } catch(Throwable e) {
          results.add(e);
        }

        // Notify main thread that we're done
        latchEnd.countDown();        
      }
    }).start();

    // Wait for consumer thread to start and block
    Thread.sleep(500);

    // Put data in the slot
    RuntimeException err = new RuntimeException("foo");
    slot.addError(err);

    latchEnd.await();
    assertEquals(1, results.size());
    assertEquals(err, results.get(0));
  }
}
