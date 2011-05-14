package de.trafficsimulation.game;

import java.util.Random;

import de.trafficsimulation.core.CarTruckFactory;
import de.trafficsimulation.core.Constants;
import de.trafficsimulation.core.MicroStreet;
import de.trafficsimulation.core.OnRamp;

public class URoadSim implements Constants {
  private final Random random;
  private final MicroStreet street;
  private final OnRamp onRamp;
  
  private double time = 0.0;
  
  // single mutable parameter at present
  protected double qIn = Q_INIT2 / 3600.;
  
  // TODO figure out something to do with these...
  protected final double density = 0.001 * DENS_INIT_INVKM; // avg. density closed s.
  protected final double p_factor = 0.; // lanechanging: politeness factor
  protected final double deltaB = 0.2; // lanechanging: changing threshold
  protected final int floatcar_nr = 0;
  protected final double p_factorRamp = 0.; // ramp Lanechange factor
  protected final double deltaBRamp = DELTABRAMP_INIT; // ramp Lanechange factor
  protected final double perTr = FRAC_TRUCK_INIT;
  protected final double qRamp = QRMP_INIT2 / 3600.;
    
  /**
   * 
   * @param uRoadLengthMeters
   * @param rampLengthMeters the whole visible length of the ramp
   */
  public URoadSim(Random random, double uRoadLengthMeters, double rampLengthMeters) {
    this.random = random;
    this.time = 0;
    
    CarTruckFactory mainVehicleFactory = new CarTruckFactory();
    mainVehicleFactory.setTruckProbability(perTr);
    mainVehicleFactory.getInconsiderateLaneChange().set_p(p_factor);
    mainVehicleFactory.getInconsiderateLaneChange().set_db(deltaB);
    
    this.street = new MicroStreet(random, mainVehicleFactory,
        uRoadLengthMeters, density, MainFrame.SCENARIO_ON_RAMP);
    
    CarTruckFactory rampVehicleFactory = new CarTruckFactory();
    rampVehicleFactory.setTruckProbability(perTr);
    rampVehicleFactory.getInconsiderateLaneChange().set_p(p_factorRamp);
    rampVehicleFactory.getInconsiderateLaneChange().set_db(deltaBRamp);
    
    double mergingPos = Math.PI * RADIUS_M + STRAIGHT_RDLEN_M + 0.5 * L_RAMP_M;
    
    // the obstacle at the end is inserted at the end of the visible ramp
    this.onRamp = new OnRamp(random, rampVehicleFactory, this.getStreet(),
        rampLengthMeters, L_RAMP_M, mergingPos);
  }
  
  public void tick() {
    getStreet().update(TIMESTEP_S, density, qIn, perTr, p_factor, deltaB);
    getOnRamp().update(TIMESTEP_S, qRamp, perTr);
    time += TIMESTEP_S;
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
   * @return time at the end of the last tick, in simulated seconds
   */
  public double getTime() {
    return time;
  }

  /**
   * @return not null; the random number generator
   */
  public Random getRandom() {
    return random;
  }
}
