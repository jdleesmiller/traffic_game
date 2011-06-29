package de.trafficsimulation.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
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

  private URoadSim sim;

  private static final long serialVersionUID = 1L;

  private final Path2D.Double onRampEnd;
  private final Rectangle2D.Double onRampBarrier;
  private final Line2D.Double onRampEndLine;
  private final Line2D.Double onRampLine;
  // private final Ellipse2D.Double speedLimitCircle;
  /**
   * Arrow at the origin with the tip at x = 0 and y = -1, pointing up.
   */
  private final GeneralPath arrow;

  private final JPanel floatPanel;

  public URoadCanvas() {
    super(makeRoads());

    Rectangle2D rampBounds = getOnRampRoad().getBoundsMeters();

    // triangle at the end of the on ramp
    double rampEndLength = 2 * LANEWIDTH_M;
    onRampEnd = new Path2D.Double();
    onRampEnd.moveTo(rampBounds.getMaxX() - 0.5, rampBounds.getMaxY());
    onRampEnd
        .lineTo(rampBounds.getMaxX() + rampEndLength, rampBounds.getMinY());
    onRampEnd.lineTo(rampBounds.getMaxX() - 0.5, rampBounds.getMinY());
    onRampEnd.closePath();

    // barrier at the end of the on ramp
    onRampBarrier = new Rectangle2D.Double(rampBounds.getMaxX() + 2,
        rampBounds.getMinY() + 3, 1, LANEWIDTH_M - 6);

    // dashed line for the on ramp
    double mergePointPos = getOnRampRoad().getRoadLengthMeters() - L_RAMP_M;
    onRampLine = new Line2D.Double(rampBounds.getMinX() + mergePointPos,
        rampBounds.getMinY(), rampBounds.getMaxX(), rampBounds.getMinY());

    // solid line at the end of the on ramp
    onRampEndLine = new Line2D.Double(rampBounds.getMaxX(),
        rampBounds.getMinY(), rampBounds.getMaxX() + rampEndLength - 4,
        rampBounds.getMinY());

    // arrow template for flow indicators
    arrow = new GeneralPath();
    arrow.moveTo(0.0f, -1.0f);
    arrow.lineTo(1.0f, 0.0f);
    arrow.lineTo(0.5f, 0.0f);
    arrow.lineTo(0.5f, 1.0f);
    arrow.lineTo(-0.5f, 1.0f);
    arrow.lineTo(-0.5f, 0.0f);
    arrow.lineTo(-1.0f, 0.0f);
    arrow.closePath();

    // speed limit sign
    // Rectangle2D uBounds = getURoad().getBoundsMeters();
    // double r = 20; // m TODO
    // double speedLimitSignX = uBounds.getMaxX() - r - LANEWIDTH_M;
    // double speedLimitSignY = uBounds.getMinY() + 3 * LANEWIDTH_M - r;
    // speedLimitCircle = new Ellipse2D.Double(speedLimitSignX - r,
    // speedLimitSignY + r, 2*r, 2*r);

    // float a panel in the center for display purposes
    setLayout(new GridBagLayout());
    floatPanel = new JPanel();
    add(floatPanel);
  }

  /** For testing */
  public static void main(String[] args) {
    JFrame f = new JFrame("on ramp test");
    f.setSize(1000, 500);
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
    sim = new URoadSim(random, getURoad().getRoadLengthMeters(),
        getOnRampRoad().getRoadLengthMeters());
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

  /**
   * 
   * @param flow
   *          in vehicles per second
   * @return
   */
  private String getFlowString(double flow) {
    return String.format(" %.0f cars/hour ", flow * 3600);
  }

  private static final double ARROW_PAD = 4;

  /**
   * Height of the head of the arrow as a multiple of the height of its body.
   */
  private static final double ARROW_HEAD_FACTOR = 1.8;

  /**
   * Left- or right-pointing arrow for drawing; the origin is taken to be the
   * base line from the textRect.
   */
  private static class Arrow {
    final GeneralPath path;
    final double wHead;
    final double wBody;
    final double yBot;
    final double yTop;

    public Arrow(Rectangle2D textRect, boolean left) {
      double p = ARROW_PAD;
      double h = textRect.getHeight();
      double hBody = h + 2 * p;
      double hHead = hBody * ARROW_HEAD_FACTOR;
      wHead = hHead / 2;
      wBody = textRect.getWidth() + p;
      
      double xTip, xEnd, xBody;
      if (left) {
        xTip = -wHead;
        xBody = 0;
        xEnd = wBody;
      } else { // right
        xBody = textRect.getWidth();
        xTip = xBody + wHead;
        xEnd = -p;
      }

      double yTip = textRect.getCenterY();
      yTop = yTip - hHead / 2;
      yBot = yTip + hHead / 2;
      double yBodyTop = yTip - hBody / 2;
      double yBodyBot = yBodyTop + hBody;
      
      path = new GeneralPath();
      path.moveTo(xBody, yBodyBot);
      path.lineTo(xBody, yBot);
      path.lineTo(xTip, yTip);
      path.lineTo(xBody, yTop);
      path.lineTo(xBody, yBodyTop);
      path.lineTo(xEnd, yBodyTop);
      path.lineTo(xEnd, yBodyBot);
      path.closePath();
    }
  }

  @Override
  protected void paintAnnotations(Graphics2D g2) {
    if (sim == null)
      return;
    double minSpeed = sim.getMinSpeedInInsideLane();
    g2.drawString(String.format("min speed: %.1fm/s", minSpeed), 30, 50);

    Rectangle2D uBounds = getURoad().getBoundsMeters();
    g2.setFont(UI.BODY_FONT.deriveFont(24f));
    String template = getFlowString(99999.0 / 3600.0);
    Rectangle2D textRect = g2.getFontMetrics().getStringBounds(template, g2);

    AffineTransform tx = g2.getTransform();

    // main road flow in arrow (top right)
    Point2D.Double ptIn = new Point2D.Double(uBounds.getMaxX(),
        uBounds.getMinY() + 2*LANEWIDTH_M);
    metersToPixels.transform(ptIn, ptIn);
    Arrow leftArrow = new Arrow(textRect, true);
    ptIn.x -= leftArrow.wBody;
    ptIn.y -= leftArrow.yTop - ARROW_PAD;
    g2.translate(ptIn.x, ptIn.y);
    g2.setColor(UI.PURPLE_HIGHLIGHT);
    g2.fill(leftArrow.path);
    g2.setColor(Color.BLACK);
    g2.drawString(getFlowString(sim.qIn), 0f, 0f);
    g2.setTransform(tx);
    
    // on ramp flow in arrow (bottom left)
    Point2D.Double ptRamp = new Point2D.Double(uBounds.getMinX(),
        uBounds.getMaxY() + LANEWIDTH_M);
    String qRampString = getFlowString(sim.qRamp);
    Rectangle2D qRampRect = g2.getFontMetrics().getStringBounds(qRampString, g2);
    Arrow rightArrow = new Arrow(textRect, false);
    metersToPixels.transform(ptRamp, ptRamp);
    ptRamp.y -= rightArrow.yTop;
    g2.translate(ptRamp.x, ptRamp.y);
    g2.setColor(UI.PURPLE_HIGHLIGHT);
    g2.fill(rightArrow.path);
    g2.translate(textRect.getWidth() - qRampRect.getWidth(), 0);
    g2.setColor(Color.BLACK);
    g2.drawString(qRampString, 0f, 0f);
    g2.setTransform(tx);

    // flow out arrow (bottom right)
    Point2D.Double ptOut = new Point2D.Double(uBounds.getMaxX(),
        uBounds.getMaxY() - 2*LANEWIDTH_M);
    String qOutString = getFlowString(sim.getMeanFlowOut());
    Rectangle2D qOutRect = g2.getFontMetrics().getStringBounds(qOutString, g2);
    metersToPixels.transform(ptOut, ptOut);
    ptOut.x -= rightArrow.wBody + rightArrow.wHead;
    ptOut.y -= rightArrow.yBot + ARROW_PAD;
    g2.translate(ptOut.x, ptOut.y);
    g2.setColor(UI.PURPLE_HIGHLIGHT);
    g2.fill(rightArrow.path);
    g2.translate(textRect.getWidth() - qOutRect.getWidth(), 0);
    g2.setColor(Color.BLACK);
    g2.drawString(qOutString, 0f, 0f);
    g2.setTransform(tx);
  }

  @Override
  protected void paintRoads(Graphics2D g2) {
    super.paintRoads(g2);

    // speed limit sign
    // g2.setColor(Color.WHITE);
    // g2.fill(speedLimitCircle);
    // g2.setColor(Color.BLACK);
    // double mph =
    // Utility.metersPerSecondToMilesPerHour(getSim().getSpeedLimit());
    // double cx = speedLimitCircle.getCenterX() - speedLimitCircle.getWidth() /
    // 5;
    // double cy = speedLimitCircle.getCenterY() + speedLimitCircle.getWidth() /
    // 5;
    // g2.drawString(String.format("%.0f", mph), (float)cx, (float)cy);
    // g2.setColor(Color.RED);
    // g2.setStroke(new BasicStroke(5));
    // g2.draw(speedLimitCircle);

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
    synchronized (sim) {
      paintVehiclesOnStreet(g2, getURoad(), sim.getStreet());
      paintVehiclesOnStreet(g2, getOnRampRoad(), sim.getOnRamp());
    }
  }

  private static ArrayList<RoadBase> makeRoads() {
    // the u section
    URoad uRoad = new URoad(2, LANEWIDTH_M, 0, 0, RADIUS_M, STRAIGHT_RDLEN_M);

    // the on ramp; overlap it slightly with the main road to avoid a gap
    double rampY = uRoad.getBoundsMeters().getMaxY() - 0.5;
    double rampStartX = -30; // arbitrary
    double rampEndX = RADIUS_M + L_RAMP_M;
    StraightRoad onRampRoad = new StraightRoad(1, LANEWIDTH_M, rampStartX,
        rampY, rampEndX, rampY);

    ArrayList<RoadBase> roads = new ArrayList<RoadBase>();
    roads.add(uRoad);
    roads.add(onRampRoad);
    return roads;
  }

  /**
   * The simulation behind the animation.
   * 
   * @return null unless running (i.e. unless start has been called and end has
   *         not)
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

  /**
   * Panel floating in the middle of the canvas.
   * 
   * @return not null
   */
  public JPanel getFloatPanel() {
    return floatPanel;
  }
}
