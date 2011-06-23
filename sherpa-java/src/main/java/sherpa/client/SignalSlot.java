/**
 * 
 */
package sherpa.client;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Reusable single-slot queue with the ability of a producer to interrupt 
 * a waiting consumer.
 * 
 * @param <T> The type of item in the slot
 */
class SignalSlot <T> {
  private final Lock dataLock = new ReentrantLock();
  private final Condition availableCondition = dataLock.newCondition();
  
  // dataLock protects read/write of data and error
  private T data;
  private Throwable error;
  
  /** 
   * Write data into the slot.  There is an assumption that 
   * the old data has already been retrieved as it will be 
   * clobbered.
   *  
   * @param data Put the data into the slot
   */ 
  public void add(T data) {
    writeSlot(data, null);
  }

  /** 
   * Interrupt a waiting reader and put the slot into an error state. 
   */
  public void addError(Throwable t) {
    writeSlot(null, t);
  }
  
  // either data or error should be non-null
  private void writeSlot(T data, Throwable error) {
    dataLock.lock();
    try {
      this.error = error;
      this.data = data;
      availableCondition.signalAll();
    } finally {
      dataLock.unlock();
    }
  }

  /**
   * Get and clear the slot - MUST be called while holding the lock!! 
   * @return The data
   * @throws Throwable If producer encountered an error
   */
  private T getAndClearUnderLock() throws Throwable {
    if(error != null) {
      throw error;
    } else {    
      // Return and clear current
      T retValue = data;
      data = null;
      return retValue;
    }
  }
  
  /** 
   * Non-blocking read and remove.
   * @return The data or null if none exists
   * @throws Throwable If producer encountered an error
   */
  public T poll() throws Throwable {
    dataLock.lock();    
    try {
      return getAndClearUnderLock();
    } finally {
      dataLock.unlock();
    }
  }
  
  /** 
   * Blocking read and remove. If return is null, the thread
   * was interrupted by the producer.
   * @return The data or null if interrupted 
   * @throws Throwable If producer encountered an error
   */
  public T take() throws Throwable {
    dataLock.lock();      
    try {
      while(data == null && error == null) {   // loop in case of spurious wake-ups
        try {
          availableCondition.await();
        } catch(InterruptedException e) {
          // ignore and re-loop
        }
      }
      return getAndClearUnderLock();
    } finally {
      dataLock.unlock();
    }
  }
}