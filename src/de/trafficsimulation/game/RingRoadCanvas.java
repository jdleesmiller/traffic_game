package de.trafficsimulation.game;


import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

import de.trafficsimulation.road.ArcRoad;
import de.trafficsimulation.road.RoadBase;

/**
 * Animation for the 'on ramp' simulation.
 */
public class RingRoadCanvas extends SimCanvas {
  
  private static final long serialVersionUID = 1L;
  
  private RingRoadSim sim; 
  
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
    c.start(42);
  }
  
  @Override
  public void start(long seed) {
    Random random = (seed == -1L) ? new Random() : new Random(seed);
    sim = new RingRoadSim(random, getRingRoad().getRoadLengthMeters());
    super.start(seed);
  }

  @Override
  public void stop() {
    super.stop();
    sim = null;
  }

  @Override
  public void tick() {
    if (sim == null)
      return;
    sim.tick();
  }

  @Override
  protected void paintVehicles(Graphics2D g2) {
    if (sim == null)
      return;
    paintVehiclesOnStreet(g2, getRingRoad(), sim.getStreet());
  }
  
  private static ArrayList<RoadBase> makeRoads() {
    ArrayList<RoadBase> roads = new ArrayList<RoadBase>();
    roads.add(new ArcRoad(2, LANEWIDTH_M, 0, 0, RADIUS_M));
    return roads;
  }
  
  public ArcRoad getRingRoad() {
    return (ArcRoad)this.roads.get(0);
  }
  
  /**
   * The simulation behind the animation. 
   * 
   * @return null unless running (i.e. unless start has been called and end
   * has not)
   */
  public RingRoadSim getSim() {
    return sim;
  }
}
