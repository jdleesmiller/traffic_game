package de.trafficsimulation.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Run simulations in the background using multiple threads.
 * 
 */
public class BackgroundRunner {
  
  /**
   * Each background sim publishes its progress after this many seconds, so
   * we can tell the user how much longer they have to wait.
   */
  private static final int SIM_UPDATE_TICKS = 500;
  
  /**
   * Thread pool used to run sims in the background for scoring.
   */
  private final ExecutorService pool;

  /**
   * Struct used to save results for a single sim.
   */
  public static class SimResult {
    public int carsOut;
    public double totalTime;
    public boolean dead;
  }

  private final java.util.List<SimResult> simResults;

  private final int simWarmupSeconds;

  private final int simTotalSeconds;

  public BackgroundRunner(int simWarmupSeconds, int simTotalSeconds) {
    //
    // set up background threads and results collection for sims
    //
    int numThreads = Runtime.getRuntime().availableProcessors() - 1;
    if (numThreads < 1)
      numThreads = 1;
    pool = Executors.newFixedThreadPool(numThreads);
    simResults = new ArrayList<SimResult>();

    this.simWarmupSeconds = simWarmupSeconds;
    this.simTotalSeconds = simTotalSeconds;
  }

  public void start(List<URoadSim> sims) {
    simResults.clear();
    for (final URoadSim sim : sims) {
      // give the sim somewhere to put its results; note that each sim only
      // writes its results to its own sim object, but the GUI may be reading
      // from the simResults list, and we don't want dirty reads; so, each
      // update to the simResult synchronises on the whole simResults list
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
            // we don't want to hang the GUI, which waits patiently until all
            // of the background sims have finished running, so make sure
            // that we flag this one as dead
            synchronized (simResults) {
              simResult.dead = true;
            }
          }
        }

        private void collectResults(URoadSim sim, boolean done) {
          // publish results occasionally so we can update the GUI
          ++ticksSinceUpdate;
          if (done || ticksSinceUpdate > SIM_UPDATE_TICKS) {
            synchronized (simResults) {
              simResult.carsOut = sim.getStreet().getNumCarsOut();
              simResult.totalTime = sim.getTime();
            }
            ticksSinceUpdate = 0;
          }
        }
      });
    }
  }
}