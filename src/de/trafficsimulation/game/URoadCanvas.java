package de.trafficsimulation.game;

import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.trafficsimulation.road.RoadBase;
import de.trafficsimulation.road.StraightRoad;
import de.trafficsimulation.road.URoad;

/**
 * Animation for the 'on ramp' simulation.
 */
public class URoadCanvas extends SimCanvas {

  /**
   * Measure flow for meanFlowOut at this interval, in seconds.
   */
  public static final double MEAN_FLOW_OUT_INTERVAL = 30;

  /**
   * Smoothing factor for meanFlowOut.
   */
  private static final double MEAN_FLOW_OUT_SMOOTHING_FACTOR = 0.15;
  
  private URoadSim sim;
  
  private static final long serialVersionUID = 1L;

  private final Path2D.Double onRampEnd;
  private final Rectangle2D.Double onRampBarrier;
  private final Line2D.Double onRampEndLine;
  private final Line2D.Double onRampLine;
  
  private final JPanel floatPanel;
  
  private URoadFlowMonitor flowMonitor;
  
  public URoadCanvas() {
    super(makeRoads());
    
    Rectangle2D rampBounds = getOnRampRoad().getBoundsMeters();
    
    // triangle at the end of the on ramp
    double rampEndLength = 2*LANEWIDTH_M;
    onRampEnd = new Path2D.Double();
    onRampEnd.moveTo(rampBounds.getMaxX() - 0.5, rampBounds.getMaxY());
    onRampEnd.lineTo(rampBounds.getMaxX() + rampEndLength,
        rampBounds.getMinY());
    onRampEnd.lineTo(rampBounds.getMaxX() - 0.5, rampBounds.getMinY());
    onRampEnd.closePath();
    
    // barrier at the end of the on ramp
    onRampBarrier = new Rectangle2D.Double(
        rampBounds.getMaxX() + 2,
        rampBounds.getMinY() + 3,
        1, LANEWIDTH_M - 6);
    
    // dashed line for the on ramp
    double mergePointPos = getOnRampRoad().getRoadLengthMeters() - L_RAMP_M;
    onRampLine = new Line2D.Double(
        rampBounds.getMinX() + mergePointPos,
        rampBounds.getMinY(),
        rampBounds.getMaxX(),
        rampBounds.getMinY());
    
    // solid line at the end of the on ramp
    onRampEndLine = new Line2D.Double(
        rampBounds.getMaxX(),
        rampBounds.getMinY(),
        rampBounds.getMaxX() + rampEndLength - 4,
        rampBounds.getMinY());
    
    // float a panel in the center for display purposes
    setLayout(new GridBagLayout());
    floatPanel = new JPanel();
    add(floatPanel);
  }
  
  /** For testing */
  public static void main(String [] args) {
    JFrame f = new JFrame("on ramp test");
    f.setSize(1000,500);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    SimCanvas c = new URoadCanvas();
    f.add(c);
    f.setVisible(true);
    c.start(42);
  }
  
  /**
   * Start the simulation.
   */
  @Override
  public void start(long seed) {
    Random random = (seed == -1L) ? new Random() : new Random(seed);
    sim = new URoadSim(random,
        getURoad().getRoadLengthMeters(),
        getOnRampRoad().getRoadLengthMeters());
    flowMonitor = new URoadFlowMonitor(sim,
        MEAN_FLOW_OUT_SMOOTHING_FACTOR, MEAN_FLOW_OUT_INTERVAL);
    super.start(seed);
  }
  
  @Override
  public void stop() {
    super.stop();
    sim = null;
    flowMonitor = null;
  }

  @Override
  public void tick() {
    if (sim == null)
      return;
    sim.tick();
    flowMonitor.sample();
  }
  
  @Override
  protected void paintRoads(Graphics2D g2) {
    super.paintRoads(g2);
    
    // triangle at the end of the on ramp
    g2.setColor(ROAD_COLOR);
    g2.fill(onRampEnd);
    
    // barrier at end of the on ramp
    g2.setColor(LANE_MARKER_COLOR);
    g2.fill(onRampBarrier);
    
    // dashed line for the on ramp
    g2.setStroke(laneMarkerStroke);
    g2.draw(onRampLine);
    
    // solid line at the end of the on ramp
    g2.setStroke(solidLaneMarkerStroke);
    g2.draw(onRampEndLine);
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
    double rampStartX = -30; // arbitrary
    double rampEndX = RADIUS_M + L_RAMP_M;
    StraightRoad onRampRoad = new StraightRoad(1, LANEWIDTH_M,
        rampStartX, rampY, rampEndX, rampY);
    
    ArrayList<RoadBase> roads = new ArrayList<RoadBase>();
    roads.add(uRoad);
    roads.add(onRampRoad);
    return roads;
  }
  
  /**
   * The simulation behind the animation. 
   * 
   * @return null unless running (i.e. unless start has been called and end
   * has not)
   */
  public URoadSim getSim() {
    return sim;
  }
  
  /**
   * Flow of cars out of the simulation.
   * 
   * @return null unless running (i.e. unless start has been called and end
   * has not)
   */
  public URoadFlowMonitor getFlowMonitor() {
    return flowMonitor;
  }
  
  public RoadBase getURoad() {
    return getRoads().get(0);
  }
  
  public RoadBase getOnRampRoad() {
    return getRoads().get(1);
  }

  /**
   * Panel floating in the middle of the canvas.
   * 
   * @return not null
   */
  public JPanel getFloatPanel() {
    return floatPanel;
  }
}
