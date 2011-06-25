package de.trafficsimulation.game;

import java.util.Random;

import de.trafficsimulation.core.Constants;

/**
 * Run the 'on ramp' simulation without the GUI for data collection.
 * 
 * Preliminary timings are about 1s real time per 30 minutes of simulated time
 * on my laptop on Linux under VMWare.
 */
public class NoGUITest implements Constants {
  
  private static final int MAX_Q_IN = 3000; // vehicles / hour
  
  public static int runForNumCarsOut(
      URoadSim sim, double statsStart, double statsDuration)
  {
    try {
      while (sim.getTime() < statsStart) {
        sim.tick();
      }
      sim.getStreet().resetNumCarsOut();
      while (sim.getTime() < statsStart + statsDuration) {
        sim.tick();
      }
      return sim.getStreet().getNumCarsOut();
    } catch(Throwable e) {
      e.printStackTrace(System.err);
      return -1;
    }
  }
  
  public static void sweepQIn(long seed, double statsStart,
      double statsDuration, int numTrials, int numPoints, double rampFlow,
      double speedLimit) {
    // lengths taken from URoadCanvas
    double uRoadLengthMeters = 1176.9911184307753;
    double rampLengthMeters = 270.0;
    
    String header = "seed statsStart statsDuration qIn rampFlow speedLimit"
        + " numCarsOut runMillis";
    System.out.println(header.replace(' ', '\t'));
    
    Random random;
    if (seed == -1L)
      random = new Random();
    else
      random = new Random(seed);
      
    for (int trial = 0; trial < numTrials; ++trial) {
      for (int point = 1; point <= numPoints; ++point) {
        // note: we want to collect the cumulative average, which means that
        // we have to disable the exponential moving average part, because it
        // clears the flow counter that we want; we just set its averaging
        // interval positive infinity.
        URoadSim sim = new URoadSim(random,
            uRoadLengthMeters, rampLengthMeters, Double.NaN,
            Double.POSITIVE_INFINITY);
        sim.qIn = ((double)point) * MAX_Q_IN / 3600.0 / numPoints;
        sim.qRamp = rampFlow / 3600.0;
        sim.setSpeedLimit(Utility.milesPerHourToMetersPerSecond(speedLimit));
        long startTime = System.currentTimeMillis();
        int numCarsOut = runForNumCarsOut(sim, statsStart, statsDuration);
        long endTime = System.currentTimeMillis();
        
        System.out.print(seed);
        System.out.print("\t" + statsStart);
        System.out.print("\t" + statsDuration);
        System.out.print("\t" + sim.qIn * 3600.0);
        System.out.print("\t" + rampFlow);
        System.out.print("\t" + speedLimit);
        System.out.print("\t" + numCarsOut);
        System.out.print("\t" + (endTime - startTime));
        System.out.println();
      }
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    long seed = -1L;
    double statsStart = 30*60;
    double statsDuration = 60*60;
    int numTrials = 10;
    int numPoints = 100;
    double rampFlow = 400; // vehicles/hour
    double speedLimit = 70; // mph
    
    if (args.length > 0)
      seed = Long.parseLong(args[0]);
    if (args.length > 1)
      statsStart = Double.parseDouble(args[1]);
    if (args.length > 2)
      statsDuration = Double.parseDouble(args[2]);
    if (args.length > 3)
      numTrials = Integer.parseInt(args[3]);
    if (args.length > 4)
      numPoints = Integer.parseInt(args[4]);
    if (args.length > 5)
      rampFlow = Double.parseDouble(args[5]);
    if (args.length > 6)
      speedLimit = Double.parseDouble(args[6]);
      
    sweepQIn(seed, statsStart, statsDuration, numTrials,
        numPoints, rampFlow, speedLimit);
  }
}
