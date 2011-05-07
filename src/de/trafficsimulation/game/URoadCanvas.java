package de.trafficsimulation.game;

import javax.swing.JFrame;

import de.trafficsimulation.core.MicroStreet;
import de.trafficsimulation.road.RoadBase;
import de.trafficsimulation.road.URoad;

public class URoadCanvas extends NewSimCanvas {

  private static final long serialVersionUID = 1L;

  public URoadCanvas(MicroStreet street, RoadBase road) {
    super(street, road);
    // TODO Auto-generated constructor stub
  }
  
  /** For testing */
  public static void main(String [] args) {
    JFrame f = new JFrame("ring test");
    f.setSize(640,480);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    RoadBase road = new URoad(2, LANEWIDTH_M, 0, 0, RADIUS_M, STRAIGHT_RDLEN_M);
    
    double density = 0.001 * DENS_INIT_INVKM; // avg. density closed s.
    double p_factor = 0.; // lanechanging: politeness factor
    double deltaB = 0.2; // lanechanging: changing threshold
    int floatcar_nr = 0;
    
    MicroStreet street = new MicroStreet(1000, // TODO road.getRoadLengthMeters()
        density, p_factor, deltaB, floatcar_nr, MainFrame.SCENARIO_RING_ROAD);
    
    NewSimCanvas c = new RingRoadCanvas(street, road);
    
    // TODO c.start();
        
    f.add(c);
    f.setVisible(true);
  }

}
