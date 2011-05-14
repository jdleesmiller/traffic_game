package de.trafficsimulation.game;

import java.util.Random;

import de.trafficsimulation.core.CarTruckFactory;
import de.trafficsimulation.core.LaneChange;
import de.trafficsimulation.core.MicroStreet;

/**
 * The ring road simulation.
 * 
 * This is a wrapper around MicroStreet that allows it to more easily interface
 * with the GUI; it also handles the setting of sim parameters.
 */
public class RingRoadSim extends SimBase {
  
  private MicroStreet street;
  
  private double density = 0.001 * DENS_INIT_INVKM; // avg. density closed s.
  
  protected final double p_factor = 0.; // lanechanging: politeness factor
  protected final double deltaB = 0.2; // lanechanging: changing threshold
  protected final int floatcar_nr = 0;
  protected final double p_factorRamp = 0.; // ramp Lanechange factor
  protected final double deltaBRamp = DELTABRAMP_INIT; // ramp Lanechange factor
  protected final double perTr = FRAC_TRUCK_INIT;
  protected final double qRamp = QRMP_INIT2 / 3600.;
  protected final double qIn = Q_INIT2;

  public RingRoadSim(Random random, double roadLengthMeters) {
    super(random);
    
    // bias toward the left lane if flow is clockwise
    CarTruckFactory vehicleFactory = new CarTruckFactory();
    LaneChange lcPolite = vehicleFactory.getPoliteLaneChange();
    LaneChange lcIncons = vehicleFactory.getInconsiderateLaneChange();
    if (CLOCKWISE) {
      lcPolite.setBiasRight(-lcPolite.getBiasRight());
      lcIncons.setBiasRight(-lcIncons.getBiasRight());
    }
    
    lcIncons.set_p(p_factor);
    lcIncons.set_db(deltaB);
    
    this.street = new MicroStreet(new Random(42),
        vehicleFactory, roadLengthMeters, density, 1);
  }
  
  @Override
  public void tick() {
    getStreet().update(TIMESTEP_S, density, qIn, perTr);
    super.tick();
  }

  /**
   * @return the street
   */
  public MicroStreet getStreet() {
    return street;
  }
  
  public double getDensity() {
    return density;
  }

  public void setDensity(double density) {
    this.density = density;
  }
}
