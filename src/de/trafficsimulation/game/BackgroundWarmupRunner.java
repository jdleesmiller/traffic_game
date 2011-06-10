package de.trafficsimulation.game;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

/**
 * Run a given list of simulations in the background using multiple threads,
 * for a given length of time. It is assumed that the simulations will not be 
 * accessed (read or written) until all of the simulations have finished.
 * 
 * The simulations can be terminated at any time by calling stop().
 * 
 * This class handles the threading. Its interface can be safely used from the
 * AWT event dispatch thread (the GUI) without additional synchronization.
 */
public class BackgroundWarmupRunner {
  private final ExecutorService pool;
  private final CountDownLatch simsLeft;
  private final Timer doneTimer;
  
  private final static int INTERRUPT_CHECK_TICKS = 30;
  
  private final static int DONE_CHECK_MS = 100;
  
  /**
   * Create instance with no jobs; isDone will return true.
   */
  public BackgroundWarmupRunner() {
    this(new ArrayList<SimBase>(), 0, null);
  }
  
  /**
   * Start given sims running on background threads.
   * 
   * @param sims not null
   * @param endTime positive, in seconds
   * @param whenDone run this on the event dispatch thread when all of the
   * simulations are done (see isDone); may be null
   */
  public BackgroundWarmupRunner(List<SimBase> sims, final double endTime,
      final Runnable whenDone) {
    simsLeft = new CountDownLatch(sims.size());
    
    // queue up the sims
    pool = Executors.newCachedThreadPool();
    for (final SimBase sim : sims) {
      pool.execute(new Runnable() {
        @Override
        public void run() {
          int ticks = 0;
          while (sim.getTime() < endTime) {
            // if we are interrupted, do NOT count down the latch
            if (++ticks % INTERRUPT_CHECK_TICKS == 0 && Thread.interrupted())
              return;
            
            sim.tick();
          }
          simsLeft.countDown();
        }
      });
    }
    
    // poll for completion
    if (whenDone == null) {
      doneTimer = null;
    } else {
      doneTimer = new Timer(DONE_CHECK_MS, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // note: this method runs on the event dispatch thread
          boolean done = isDone();
          if (done || isTerminated())
            doneTimer.stop();
          if (done)
            whenDone.run();
        }
      });
      doneTimer.start();
    }
  }
  
  /**
   * Stop any currently running sims. This method does not block; to check
   * whether the sims have really stopped, you have to call isDone().
   */
  public void stop() {
    pool.shutdownNow();
  }
  
  /**
   * True iff the pool was terminated by calling stop(), and it's now finished. 
   */
  public boolean isTerminated() {
    return pool.isTerminated();
  }
  
  /**
   * True iff all simulations finished normally; this will return false if the
   * pool was terminated before some simulations could finish.
   */
  public boolean isDone() {
    try {
      // await with timeout 0 just checks the latch; it doesn't actually block
      return simsLeft.await(0, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // we shouldn't get here if this method is called from the event dispatch
      // thread, which doesn't usually get interrupted, but we have to handle
      // it somehow
      return false;
    }
  }
}
