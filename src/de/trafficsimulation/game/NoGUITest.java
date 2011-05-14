package de.trafficsimulation.game;

import de.trafficsimulation.core.Constants;
import de.trafficsimulation.core.MicroStreet;
import de.trafficsimulation.core.OnRamp;

/**
 * Run the OnRamp scenario without the GUI.
 * 
 * Preliminary timings are about 1s real time per 30 minutes of simulated time
 * on my laptop on Linux under VMWare.
 */
public class NoGUITest implements Constants {
  
  protected MicroStreet street;
  protected OnRamp onRamp;
  
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
  
  public NoGUITest() {
    double uRoadLengthMeters = 1176.9911184307753;
    double onRampLengthMeters = 270.0;
    
    this.street = new MicroStreet(uRoadLengthMeters,
        density, p_factor, deltaB, MainFrame.SCENARIO_RING_ROAD);
    
    double mergingPos = Math.PI * RADIUS_M + STRAIGHT_RDLEN_M + 0.5 * L_RAMP_M;
    
    // the obstacle at the end is inserted at the end of the visible ramp
    this.onRamp = new OnRamp(this.street,
        onRampLengthMeters, // the whole visible ramp
        L_RAMP_M, mergingPos, p_factorRamp, deltaBRamp);
  }
  
  public void run(int mins) {
    double time = 0;
    while (time < mins*60) {
      street.update(TIMESTEP_S, density, qIn, perTr, p_factor, deltaB);
      onRamp.update(TIMESTEP_S, qRamp, perTr, p_factorRamp, deltaBRamp);
      time += TIMESTEP_S;
    }
  }
  
  public void sweepQIn(int mins) {
    for (int i = 0; i < 100; ++i) {
      
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    int mins = 60;
    
    if (args.length > 0)
      mins = Integer.parseInt(args[0]);
    
    NoGUITest test = new NoGUITest();
    long startTime = System.currentTimeMillis();
    test.run(mins);
    long stopTime = System.currentTimeMillis();
    long runTime = stopTime - startTime;
    System.out.println("Run time: " + runTime + "ms");
  }
}
