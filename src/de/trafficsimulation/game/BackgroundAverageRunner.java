package de.trafficsimulation.game;

import java.util.ArrayList;
import java.util.List;

/**
 * - the idea of the warmup is to give jams a chance to form
 * - but really we don't need long to measure the rate; it's pretty steady
 * - could instead do a moving average for each sim
 * - then average the moving averages?
 * - if each sim only runs for an hour or so, we could just wait until the end
 *   of the sim to report the moving average - easy
 * 
 * GUI experience:
 * - do something immediately: start running sims; say "calculating" initially
 * - user can change qIn at any time, and it just says "calculating..." again.
 * - when to stop saying calculating? could just do a time window?
 *   or could try to look at the stability of the estimate -- e.g. 5 estimates
 *   less than 10 vehicles / hour different mean we display the result
 * - just keep running the sims forever; change the messages
 * 
 * The background threads run with priority set to one less than the thread
 * used to construct the object.
 */
public abstract class BackgroundAverageRunner {
  
  private final static double TOTAL_SIM_SECONDS = 30*60;
  
  private final static int INTERVAL_TICKS = 4*60;
  
  private final static double DEFAULT_SMOOTHING_FACTOR = 0.2;
  
  private final List<Thread> threads;
  
  private int flowCount;
  private double flowMean;
  
  public BackgroundAverageRunner() {
    this(DEFAULT_SMOOTHING_FACTOR);
  }
  
  public BackgroundAverageRunner(double smoothingFactor) {
    this(Math.max(1, Runtime.getRuntime().availableProcessors() - 1),
        smoothingFactor);
  }
  
  public BackgroundAverageRunner(final int numThreads,
      final double smoothingFactor)
  {
    threads = new ArrayList<Thread>();
    for (int i = 0; i < numThreads; ++i) {
      threads.add(new Thread(new Runnable() {
        @Override
        public void run() {
          while (true) {
            URoadSim sim;
            synchronized (BackgroundAverageRunner.this) {
              sim = getNewSim();
            }
            
            double flow = 0;
            double lastIntervalTime = 0;
            while (sim.getTime() < TOTAL_SIM_SECONDS) {
              // run simulation for one averaging interval
              for (int i = 0; i < INTERVAL_TICKS; ++i) {
                sim.tick();
              }
              
              // measure the flow over the interval
              int carsOut = sim.getStreet().getNumCarsOut();
              double interval = sim.getTime() - lastIntervalTime;
              double intervalFlow = carsOut / interval;
              sim.getStreet().resetNumCarsOut();
              lastIntervalTime = sim.getTime();
              
              // update moving average for the current sim
              flow = smoothingFactor * intervalFlow +
                (1 - smoothingFactor) * flow;
              
              // check for interrupt
              if (Thread.currentThread().isInterrupted())
                return;
            }
            
            // sim is done; report the flow
            recordFlow(flow);
          }
        }
      }));
    }
    
    for (Thread thread : threads) {
      thread.setPriority(Thread.currentThread().getPriority() - 1);
      thread.start();
    }
  }
  
  public void stop() {
    for (Thread thread : threads) {
      thread.interrupt();
    }
  }
  
  /**
   * 
   * @param flow
   */
  private synchronized void recordFlow(double flow) {
    // just a straight cumulative average
    flowCount += 1;
    flowMean += (flow - flowMean) / flowCount;
  }
  
  /**
   * Called when a background thread needs a new sim to run.
   * 
   * NB: this is called from a background thread; the implementation should
   * NOT touch any GUI elements. If you need to access the GUI, you should use
   * SwingUtilities.invokeAndWait. 
   * 
   * It will only be called from one background thread at a time.
   * 
   * @return not null
   */
  protected abstract URoadSim getNewSim();

  /**
   * Number of completed simulations that contribute to the average returned
   * by getFlowMean
   * 
   * @return non-negative; initially zero
   */
  public int getFlowCount() {
    return flowCount;
  }

  /**
   * The current estimate of the flow, based on all simulations completed so
   * far, in cars per second.
   * 
   * @return non-negative; initially zero
   */
  public double getFlowMean() {
    return flowMean;
  }
}
