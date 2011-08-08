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
   * @param data Put the data into the slot. data should never be null.
   */ 
  public void add(T data) {
    assert data != null;
    writeSlot(data, null);
  }

  /** 
   * Interrupt a waiting reader and put the slot into an error state. 
   * @param t The error, should never be null
   */
  public void addError(Throwable t) {
    assert t != null;
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
   * @return The data or null if no data or error exists
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
   * Blocking read and remove. If the thread was interrupted by 
   * the producer due to an error, the producer's error will be thrown.
   * @return The data, should never be null
   * @throws Throwable If producer encountered an error
   */
  public T take() throws Throwable {
    dataLock.lock();      
    try {
      while(data == null && error == null) {
        try {
          availableCondition.await();
        } catch(InterruptedException e) {
          // ignore and re-loop in case of spurious wake-ups
        }
      }
      
      // should only get to here if data or error is non-null
      return getAndClearUnderLock();
    } finally {
      dataLock.unlock();
    }
  }
}