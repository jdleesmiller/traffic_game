package de.trafficsimulation.game;

/**
 * An exponential moving average of a flow; it takes counts and time intervals,
 * but it just uses a fixed discount factor (not time-dependent), at present.
 */
public class FlowMovingAverage {
  private double mean;
  private double lastUpdateTime;
  
  private final double smoothingFactor;
  
  public FlowMovingAverage(double smoothingFactor) {
    this.smoothingFactor = smoothingFactor;
  }
  
  public void update(double time, int count) {
    double sinceLastSample = time - lastUpdateTime;
    double flow = count / sinceLastSample;
    mean = smoothingFactor * flow + (1 - smoothingFactor) * mean;
    lastUpdateTime = time;
  }
  
  public double getEstimate() {
    return mean;
  }
  
  public double getLastUpdateTime() {
    return lastUpdateTime;
  }
}
