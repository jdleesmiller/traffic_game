package de.trafficsimulation.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Run a given list of simulations in the background using multiple threads.
 * 
 * This class handles the threading. Its interface can be safely used from the
 * AWT event dispatch thread (the GUI) without additional synchronization.
 */
public class BackgroundBatchRunner {
  
  /**
   * Each background sim publishes its progress after this many seconds, so
   * we can tell the user how much longer they have to wait.
   */
  private static final int SIM_UPDATE_TICKS = 500;
  
  /**
   * Structure used to save results for a single simulation.
   */
  private static class SimResult {
    public int carsOut;
    public double totalTime;
    public boolean dead;
  }
  
  /**
   * Structure used to return results for all simulations that have run or are
   * now running.
   */
  public static class AggregateResults {
    public boolean allFinished;
    public int carsOut;
    public double totalStatsTime;
    public double totalTime;
  }
  
  private final ExecutorService pool;
  
  private final double simWarmupSeconds;
  private final double simTotalSeconds;

  /**
   * The results for all simulations; this includes those that are finished,
   * running, dead (due to shutdown or internal error), or waiting to run.
   */
  private final java.util.List<SimResult> simResults;
  
  /**
   * Create runner and start jobs.
   * 
   * @param simWarmupSeconds
   * @param simTotalSeconds
   * @param sims
   */
  public BackgroundBatchRunner(final double simWarmupSeconds,
      final double simTotalSeconds,
      List<URoadSim> sims)
  {
    this.simWarmupSeconds = simWarmupSeconds;
    this.simTotalSeconds = simTotalSeconds;
    
    //
    // set up background threads and results collection for sims
    //
    int numThreads = Runtime.getRuntime().availableProcessors() - 1;
    if (numThreads < 1)
      numThreads = 1;
    pool = Executors.newFixedThreadPool(numThreads);
    simResults = new ArrayList<SimResult>();
    
    //
    // enqueue jobs
    //
    for (final URoadSim sim : sims) {
      // give the sim somewhere to put its results; note that each sim only
      // writes its results to its own sim object
      final SimResult simResult = new SimResult();
      simResults.add(simResult);

      pool.execute(new Runnable() {
        private int ticksSinceUpdate;

        @Override
        public void run() {
          try {
            // run the warmup
            ticksSinceUpdate = 0;
            while (sim.getTime() < simWarmupSeconds) {
              sim.tick();
              collectResults(sim, false);
            }

            // reset the output counter and start collecting results
            sim.getStreet().resetNumCarsOut();
            while (sim.getTime() < simTotalSeconds) {
              sim.tick();
              collectResults(sim, false);
            }

            // collect final results, and we're done
            collectResults(sim, true);

          } catch (Throwable e) {
            // we may have been interrupted; record this fact in the results
            synchronized (BackgroundBatchRunner.this) {
              simResult.dead = true;
            }
          }
        }

        private void collectResults(URoadSim sim, boolean done) {
          // publish results occasionally so we can update the GUI
          ++ticksSinceUpdate;
          if (done || ticksSinceUpdate > SIM_UPDATE_TICKS) {
            synchronized (BackgroundBatchRunner.this) {
              simResult.carsOut = sim.getStreet().getNumCarsOut();
              simResult.totalTime = sim.getTime();
            }
            ticksSinceUpdate = 0;
          }
        }
      });
    }
  }
  
  /**
   * Tell the runner to shut down. This kills the current jobs.
   */
  public void shutdown() {
    // it's unfortunate that there isn't a method that allows us to wait for
    // currently-running jobs to finish but ignores all future jobs; this one
    // will interrupt the currently running jobs
    pool.shutdownNow();
  }
  
  /**
   * True iff all jobs have terminated, following a call to shutdown().
   * 
   * @return true iff all jobs have finished
   */
  public boolean isTerminated() {
    return pool.isTerminated();
  }
  
  /**
   * Aggregate results from simulations.
   * 
   * @return not null
   */
  public synchronized AggregateResults getAggregateResults() {
    AggregateResults results = new AggregateResults();
    
    results.allFinished = true;
    for (SimResult simResult : simResults) {
      if (!(simResult.dead || simResult.totalTime >= simTotalSeconds))
        results.allFinished = false;
      
      results.totalTime += simResult.totalTime;
      
      double statsTime = simResult.totalTime - simWarmupSeconds;
      if (statsTime > 0) {
        results.carsOut += simResult.carsOut;
        results.totalStatsTime += statsTime;
      }
    }
    
    return results;
  }
}