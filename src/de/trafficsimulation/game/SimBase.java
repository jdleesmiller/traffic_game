package de.trafficsimulation.game;

import java.util.Random;

import de.trafficsimulation.core.Constants;

/**
 * Common functionality for RingRoadSim and URoadSim.
 */
public class SimBase implements Constants {
  
  private final Random random;
  private double time;

  public SimBase(Random random) {
    this.random = random;
    this.time = 0;
  }
  
  /**
   * Override this to do work.
   */
  public void tick() {
    time += TIMESTEP_S;
  }
  
  /**
   * @return not null; the random number generator
   */
  public Random getRandom() {
    return random;
  }

  /**
   * @return time at the end of the last tick, in simulated seconds
   */
  public double getTime() {
    return time;
  }
}
