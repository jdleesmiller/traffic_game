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
  private final MicroStreet street;
  private final OnRamp onRamp;
  
  // single mutable parameter at present
  protected double qIn = Q_INIT2 / 3600.;
  
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
  }
  
  @Override
  public void tick() {
    getStreet().update(TIMESTEP_S, density, qIn);
    getOnRamp().update(TIMESTEP_S, qRamp);
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
}
