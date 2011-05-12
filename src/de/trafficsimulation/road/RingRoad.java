package de.trafficsimulation.road;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

/**
 * Helper for drawing a ring road. 
 */
public class RingRoad extends RoadBase {
  protected final Vector<Shape> laneMarkers;
  private final double radiusMeters;
  private final Shape roadCenter;
  
  /**
   * Constructor.
   * 
   * @param numLanes
   * @param laneWidthMeters
   * @param radiusMeters
   */
  public RingRoad(int numLanes, double laneWidthMeters, double radiusMeters) {
    super(numLanes, laneWidthMeters);
    this.radiusMeters = radiusMeters;
    
    this.roadCenter = makeRoadEllipse(numLanes / 2.0);
    this.laneMarkers = new Vector<Shape>(numLanes - 1);
    for (int lane = 1; lane < numLanes; ++lane) {
      laneMarkers.add(makeRoadEllipse(lane));
    }
  }
  
  @Override
  public Rectangle2D getBoundsMeters() {
    // expand around the bounding box of the road center
    Rectangle2D rect = getRoadCenter().getBounds2D();
    double roadWidth = numLanes * laneWidthMeters;
    rect.setFrame(
        rect.getMinX() - roadWidth / 2,
        rect.getMinY() - roadWidth / 2,
        rect.getWidth() + roadWidth,
        rect.getHeight() + roadWidth);
    return rect;
  }
  
  @Override
  public Vector<Shape> getLaneMarkers() {
    return laneMarkers;
  }
  
  /**
   * Radius of the ring road, measured from the center to the inside of the
   * inside lane, in meters.
   * 
   * @return positive
   */
  public double getRadiusMeters() {
    return radiusMeters;
  }

  @Override
  public Shape getRoadCenter() {
    return roadCenter;
  }
  
  @Override
  public double getRoadLengthMeters() {
    return 2 * Math.PI * radiusMeters;
  }
  
  /**
   * An ellipse centered at the origin.
   * 
   * @param offset
   * @return
   */
  private Ellipse2D.Double makeRoadEllipse(double offset) {
    double radius = getRadiusMeters() + getLaneWidthMeters()*offset;
    return new Ellipse2D.Double(-radius, -radius, 2*radius, 2*radius);
  }
  
  
  @Override
  public boolean transformForCarAt(Graphics2D g2, int lane, double position) {
    g2.rotate(2 * Math.PI * -position / getRoadLengthMeters());
    g2.translate(getRadiusMeters() + (lane + 0.5) * getLaneWidthMeters(), 0);
    return true;
  }
}
