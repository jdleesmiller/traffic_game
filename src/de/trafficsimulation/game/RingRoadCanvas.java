package de.trafficsimulation.game;


import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JFrame;

import de.trafficsimulation.core.MicroStreet;
import de.trafficsimulation.road.ArcRoad;
import de.trafficsimulation.road.RoadBase;

/**
 * Draw ring road and vehicles.
 */
public class RingRoadCanvas extends SimCanvas {

  private MicroStreet street;
  
  private static final long serialVersionUID = 1L;
  
  // TODO figure out something to do with these...
  protected final double density = 0.001 * DENS_INIT_INVKM; // avg. density closed s.
  protected final double p_factor = 0.; // lanechanging: politeness factor
  protected final double deltaB = 0.2; // lanechanging: changing threshold
  protected final int floatcar_nr = 0;
  protected final double p_factorRamp = 0.; // ramp Lanechange factor
  protected final double deltaBRamp = DELTABRAMP_INIT; // ramp Lanechange factor
  protected final double perTr = FRAC_TRUCK_INIT;
  protected final double qRamp = QRMP_INIT2 / 3600.;
  protected final double qIn = Q_INIT2;
  
  /**
   * Constructor.
   * 
   * @param road
   */
  public RingRoadCanvas()
  {
    super(makeRoads());
  }
  
  /** For testing */
  public static void main(String [] args) {
    JFrame f = new JFrame("ring test");
    f.setSize(640,480);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    SimCanvas c = new RingRoadCanvas();
    f.add(c);
    f.setVisible(true);
    c.start();
  }
  
  @Override
  public void start() {
    
    this.street = new MicroStreet(
        getRingRoad().getRoadLengthMeters(),
        density, p_factor, deltaB, floatcar_nr, MainFrame.SCENARIO_RING_ROAD);
    
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
    this.street = null;
  }

  @Override
  public void tick() {
    if (street == null)
      return;
    
    street.update(getSimTime(), TIMESTEP_S, MainFrame.SCENARIO_RING_ROAD,
        density, qIn, perTr, p_factor, deltaB);
  }

  @Override
  protected void paintVehicles(Graphics2D g2) {
    if (street == null)
      return;
    
    paintVehiclesOnStreet(g2, getRingRoad(), street);
  }
  
  private static ArrayList<RoadBase> makeRoads() {
    ArrayList<RoadBase> roads = new ArrayList<RoadBase>();
    roads.add(new ArcRoad(2, LANEWIDTH_M, 0, 0, RADIUS_M));
    return roads;
  }
  
  public ArcRoad getRingRoad() {
    return (ArcRoad)this.roads.get(0);
  }
}
