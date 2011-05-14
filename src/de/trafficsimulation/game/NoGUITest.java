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
  
  public static int runForNumCarsOut(
      URoadSim sim, double statsStartTime, double statsDuration)
  {
    int numCarsOutInWarmup = -1;
    double statsEndTime = statsStartTime + statsDuration;
    while (sim.getTime() < statsEndTime) {
      if (numCarsOutInWarmup < 0 && sim.getTime() > statsStartTime)
        numCarsOutInWarmup = sim.getStreet().getNumCarsOut();
      sim.tick();
    }
    return sim.getStreet().getNumCarsOut() - numCarsOutInWarmup;
  }
  
  public static void sweepQIn(double statsStartTime, double statsDuration,
      int numTrials, int numPoints) {
    double uRoadLengthMeters = 1176.9911184307753;
    double rampLengthMeters = 270.0;
    
    System.out.println("qIn\tnumCarsOut");
    
    Random random = new Random();
    for (int trial = 0; trial < numTrials; ++trial) {
      for (int point = 0; point < numPoints; ++point) {
        URoadSim sim = new URoadSim(random,
            uRoadLengthMeters, rampLengthMeters);
        sim.qIn = ((double)point) * Q_MAX / 3600.0 / numPoints;
        int numCarsOut = runForNumCarsOut(sim, statsStartTime, statsDuration);
        System.out.println((sim.qIn * 3600.0) + "\t" + numCarsOut);
      }
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    double statsStartMins = 30;
    double statsDurationMins = 60;
    int numTrials = 10;
    int numPoints = 100;
    
    if (args.length > 0)
      statsStartMins = Double.parseDouble(args[0]);
    if (args.length > 1)
      statsDurationMins = Double.parseDouble(args[1]);
    if (args.length > 2)
      numTrials = Integer.parseInt(args[2]);
    if (args.length > 3)
      numPoints = Integer.parseInt(args[3]);
    
    long startTime = System.currentTimeMillis();
    sweepQIn(statsStartMins * 60, statsDurationMins * 60, numTrials, numPoints);
    long stopTime = System.currentTimeMillis();
    long runTime = stopTime - startTime;
    System.out.println("Run time: " + runTime + "ms");
  }
}
