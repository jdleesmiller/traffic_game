package de.trafficsimulation.game;

public class URoadFlowMonitor {
  private final URoadSim sim;
  private final double smoothingFactor;
  private final double sampleInterval;

  private double meanFlowOut;
  private int lastSampleCarsOut;
  private double lastSampleTime;

  public URoadFlowMonitor(URoadSim sim,
      double smoothingFactor, double sampleInterval) {
    this.sim = sim;
    this.smoothingFactor = smoothingFactor;
    this.sampleInterval = sampleInterval;
  }

  /**
   * Take a sample (updates the average).
   */
  public void sample() {
    double time = sim.getTime();
    double sinceLastSample = time - lastSampleTime;
    if (sinceLastSample > sampleInterval) {
      int totalCarsOut = sim.getStreet().getNumCarsOut();
      int carsOut = totalCarsOut - lastSampleCarsOut;
      double flowOut = carsOut / sinceLastSample;
      meanFlowOut = smoothingFactor * flowOut + (1 - smoothingFactor)
      * meanFlowOut;
      lastSampleTime = time;
      lastSampleCarsOut = totalCarsOut;
    }
  }
  
  /**
   * In cars per second.
   * 
   * @return non-negative
   */
  public double getMeanFlowOut() {
    return meanFlowOut;
  }
}
