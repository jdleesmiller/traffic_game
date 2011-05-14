package de.trafficsimulation.game;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

import de.trafficsimulation.road.RoadBase;
import de.trafficsimulation.road.StraightRoad;
import de.trafficsimulation.road.URoad;

public class URoadCanvas extends SimCanvas {
  
  private URoadSim sim;
  
  private static final long serialVersionUID = 1L;

  private final Path2D.Double onRampEnd;
  private final Line2D.Double onRampLine;
  
  public URoadCanvas() {
    super(makeRoads());
    Rectangle2D rampBounds = getOnRampRoad().getBoundsMeters();
    
    onRampEnd = new Path2D.Double();
    onRampEnd.moveTo(rampBounds.getMaxX() - 0.5, rampBounds.getMaxY());
    onRampEnd.lineTo(rampBounds.getMaxX() + 2*LANEWIDTH_M, rampBounds.getMinY());
    onRampEnd.lineTo(rampBounds.getMaxX() - 0.5, rampBounds.getMinY());
    onRampEnd.closePath();
    
    double mergePointPos = getOnRampRoad().getRoadLengthMeters() - L_RAMP_M;
    onRampLine = new Line2D.Double(
        rampBounds.getMinX() + mergePointPos,
        rampBounds.getMinY(),
        rampBounds.getMaxX(),
        rampBounds.getMinY());
  }
  
  /** For testing */
  public static void main(String [] args) {
    JFrame f = new JFrame("on ramp test");
    f.setSize(1000,500);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    SimCanvas c = new URoadCanvas();
    f.add(c);
    f.setVisible(true);
    c.start();
  }
  
  @Override
  public void start() {
    sim = new URoadSim(new Random(42),
        getURoad().getRoadLengthMeters(),
        getOnRampRoad().getRoadLengthMeters());
    super.start();
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
  protected void paintRoads(Graphics2D g2) {
    super.paintRoads(g2);
    
    // triangle at the end of the on ramp
    g2.setColor(ROAD_COLOR);
    g2.fill(onRampEnd);
    
    // dashed line for the on ramp
    g2.setColor(LANE_MARKER_COLOR);
    g2.setStroke(laneMarkerStroke);
    g2.draw(onRampLine);
  }

  @Override
  protected void paintVehicles(Graphics2D g2) {
    if (sim == null)
      return;
    paintVehiclesOnStreet(g2, getURoad(), sim.getStreet());
    paintVehiclesOnStreet(g2, getOnRampRoad(), sim.getOnRamp());
  }
  
  private static ArrayList<RoadBase> makeRoads()
  {
    // the u section
    URoad uRoad = new URoad(2, LANEWIDTH_M, 0, 0, RADIUS_M, STRAIGHT_RDLEN_M);
    
    // the on ramp; overlap it slightly with the main road to avoid a gap
    double rampY = uRoad.getBoundsMeters().getMaxY() - 0.5;
    double rampStartX = -50; // arbitrary
    double rampEndX = RADIUS_M + L_RAMP_M;
    StraightRoad onRampRoad = new StraightRoad(1, LANEWIDTH_M,
        rampStartX, rampY, rampEndX, rampY);
    
    ArrayList<RoadBase> roads = new ArrayList<RoadBase>();
    roads.add(uRoad);
    roads.add(onRampRoad);
    return roads;
  }
  
  /**
   * 
   * @return null unless running (i.e. unless start has been called and end
   * has not)
   */
  public URoadSim getSim() {
    return sim;
  }
  
  public RoadBase getURoad() {
    return getRoads().get(0);
  }
  
  public RoadBase getOnRampRoad() {
    return getRoads().get(1);
  }
}
