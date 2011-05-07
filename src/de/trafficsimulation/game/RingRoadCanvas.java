package de.trafficsimulation.game;


import javax.swing.JFrame;

import de.trafficsimulation.core.MicroStreet;
import de.trafficsimulation.road.ArcRoad;
import de.trafficsimulation.road.RoadBase;

/**
 * Draw ring road and vehicles.
 */
public class RingRoadCanvas extends NewSimCanvas {

  private static final long serialVersionUID = 1L;
  
  /**
   * Constructor.
   * 
   * @param road
   */
  public RingRoadCanvas(MicroStreet street, RoadBase road)
  {
    super(street, road);
  }
  
  /** For testing */
  public static void main(String [] args) {
    JFrame f = new JFrame("ring test");
    f.setSize(640,480);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    RoadBase road = new ArcRoad(2, LANEWIDTH_M, 0, 0, RADIUS_M);
    
    double density = 0.001 * DENS_INIT_INVKM; // avg. density closed s.
    double p_factor = 0.; // lanechanging: politeness factor
    double deltaB = 0.2; // lanechanging: changing threshold
    int floatcar_nr = 0;
    
    MicroStreet street = new MicroStreet(road.getRoadLengthMeters(),
        density, p_factor, deltaB, floatcar_nr, MainFrame.SCENARIO_RING_ROAD);
    
    NewSimCanvas c = new RingRoadCanvas(street, road);
    
    c.start();
        
    f.add(c);
    f.setVisible(true);
  }
}
