package de.trafficsimulation.game;

import java.util.Random;

import de.trafficsimulation.core.CarTruckFactory;
import de.trafficsimulation.core.MicroStreet;
import de.trafficsimulation.core.OnRamp;

/**
 * The 'on ramp' simulation.
 * 
 * This is a wrapper around MicroStreet (and OnRamp) that allows them to more
 * easily interface with the GUI; it also handles the setting of parameters.
 */
public class URoadSim extends SimBase 
{
  /**
   * Smoothing factor for the mean flow out exponential moving average.
   */
  private static final double FLOW_OUT_SMOOTHING_FACTOR = 0.01;
  
  /**
   * Measure flow for meanFlowOut at this interval, in seconds.
   */
  private static final double FLOW_OUT_INTERVAL = 15;
  
  private final MicroStreet street;
  private final OnRamp onRamp;
  private final FlowMovingAverage meanFlowOut;
  
  // mutable parameters
  /**
   * Target flow into the main road, in vehicles per second. 
   */
  protected double qIn = Q_INIT2 / 3600.;
  
  /**
   * Target flow into the on ramp, in vehicles per second. 
   */
  protected double qRamp = QRMP_INIT2 / 3600.;
    
  protected final double density = 0.001 * DENS_INIT_INVKM; // avg. density closed s.
  protected final double p_factor = 0.; // lanechanging: politeness factor
  protected final double deltaB = 0.2; // lanechanging: changing threshold
  protected final int floatcar_nr = 0;
  protected final double p_factorRamp = 0.; // ramp Lanechange factor
  protected final double deltaBRamp = DELTABRAMP_INIT; // ramp Lanechange factor
  protected final double perTr = FRAC_TRUCK_INIT;
  
  /**
   * 
   * @param uRoadLengthMeters
   * @param rampLengthMeters the whole visible length of the ramp
   */
  public URoadSim(Random random, double uRoadLengthMeters, double rampLengthMeters) {
    super(random);
    
    CarTruckFactory mainVehicleFactory = new CarTruckFactory();
    mainVehicleFactory.setTruckProbability(perTr);
    mainVehicleFactory.getInconsiderateLaneChange().set_p(p_factor);
    mainVehicleFactory.getInconsiderateLaneChange().set_db(deltaB);
    
    this.street = new MicroStreet(random, mainVehicleFactory,
        uRoadLengthMeters, density, 2);
    
    CarTruckFactory rampVehicleFactory = new CarTruckFactory();
    rampVehicleFactory.setTruckProbability(perTr);
    rampVehicleFactory.getInconsiderateLaneChange().set_p(p_factorRamp);
    rampVehicleFactory.getInconsiderateLaneChange().set_db(deltaBRamp);
    
    double mergingPos = Math.PI * RADIUS_M + STRAIGHT_RDLEN_M + 0.5 * L_RAMP_M;
    
    // the obstacle at the end is inserted at the end of the visible ramp
    this.onRamp = new OnRamp(random, rampVehicleFactory, this.getStreet(),
        rampLengthMeters, L_RAMP_M, mergingPos);
    
    this.meanFlowOut = new FlowMovingAverage(FLOW_OUT_SMOOTHING_FACTOR);
  }
  
  @Override
  public synchronized void tick() {
    getStreet().update(TIMESTEP_S, density, qIn);
    getOnRamp().update(TIMESTEP_S, qRamp);
    
    if (getTime() - meanFlowOut.getLastUpdateTime() > FLOW_OUT_INTERVAL) {
      meanFlowOut.update(getTime(), getStreet().getNumCarsOut());
      getStreet().resetNumCarsOut();
    }
    
    super.tick();
  }

  /**
   * @return the street
   */
  public MicroStreet getStreet() {
    return street;
  }

  /**
   * @return the onRamp
   */
  public OnRamp getOnRamp() {
    return onRamp;
  }
  
  /**
   * Exponential moving average estimate of the flow out of the main road, in
   * cars per second.
   * 
   * @return non-negative; in cars per second
   */
  public double getMeanFlowOut() {
    return meanFlowOut.getEstimate();
  }
  
  /**
   * Current speed limit. The speed limits are actually set separately for
   * every street and vehicle class; this method just returns the one on the
   * main road for cars.
   *  
   * @return positive; in meters per second
   */
  public double getSpeedLimit() {
    return getStreet().getVehicleFactory().getCarIDM().v0;
  }
  
  /**
   * Set speed limit on all of the vehicle factories; this affects only cars
   * that will be created in the future.
   * 
   * @param speedLimit in meters per second
   */
  public void setSpeedLimit(double speedLimit) {
    getStreet().getVehicleFactory().getCarIDM().v0 = speedLimit;
    getStreet().getVehicleFactory().getTruckIDM().v0 = speedLimit;
    getOnRamp().getVehicleFactory().getCarIDM().v0 = speedLimit;
    getOnRamp().getVehicleFactory().getTruckIDM().v0 = speedLimit;
  }
}
