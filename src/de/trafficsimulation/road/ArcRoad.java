package de.trafficsimulation.road;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;


import static de.trafficsimulation.game.Utility.*;

public class ArcRoad extends RoadBase {
  
  private final Arc2D innerArc;
  private final Arc2D centerArc;
  private final List<Shape> laneMarkers;
  
  /**
   * Constructor
   * 
   * @param numLanes
   * @param laneWidthMeters
   * @param innerArc
   */
  public ArcRoad(int numLanes, double laneWidthMeters, Arc2D innerArc) {
    super(numLanes, laneWidthMeters);
    
    this.innerArc = innerArc;
    
    centerArc = expandArcRadius(innerArc,
        getNumLanes() * getLaneWidthMeters() / 2.0);
    
    laneMarkers = new ArrayList<Shape>(getNumLanes() - 1);
    for (int lane = 1; lane < getNumLanes(); ++lane) {
      laneMarkers.add(expandArcRadius(innerArc, lane * getLaneWidthMeters()));
    }
  }
  
  /**
   * A partial arc with given center, radius and angles.
   * 
   * @param numLanes
   * @param laneWidthMeters
   * @param centerXMeters
   * @param centerYMeters
   * @param radiusMeters
   * @param angleStart
   * @param angleExtent
   */
  public ArcRoad(int numLanes, double laneWidthMeters,
      double centerXMeters, double centerYMeters,
      double radiusMeters,
      double angleStart, double angleExtent)
  {
    this(numLanes, laneWidthMeters, makeArcFromCenter(centerXMeters,
        centerYMeters, radiusMeters, angleStart, angleExtent, Arc2D.OPEN));
  }
  
  /**
   * A full ring.
   * 
   * @param numLanes
   * @param laneWidthMeters
   * @param centerXMeters
   * @param centerYMeters
   * @param radiusMeters
   */
  public ArcRoad(int numLanes, double laneWidthMeters,
      double centerXMeters, double centerYMeters,
      double radiusMeters)
  {
    this(numLanes, laneWidthMeters, makeArcFromCenter(centerXMeters,
        centerYMeters, radiusMeters, 0, 360, Arc2D.CHORD));
  }
  
  private static Arc2D makeArcFromCenter(double centerXMeters,
      double centerYMeters, double radiusMeters, double angleStart,
      double angleExtent, int arcType) {
    Arc2D arc = new Arc2D.Double();
    arc.setArcByCenter(centerXMeters, centerYMeters, radiusMeters,
        angleStart, angleExtent, arcType);
    return arc;
  }

  @Override
  public Rectangle2D getBoundsMeters() {
    // note: getBounds2D includes only the points subtended by the angle
    Rectangle2D bounds = innerArc.getBounds2D();
    bounds.add(expandArcRadius(innerArc,
        getNumLanes() * getLaneWidthMeters()).getBounds2D());
    return bounds;
  }

  @Override
  public List<Shape> getLaneMarkers() {
    return laneMarkers;
  }

  @Override
  public Shape getRoadCenter() {
    return centerArc;
  }

  @Override
  public double getRoadLengthMeters() {
    // note: getWidth is the diameter; angle extent is in degrees
    double fraction = Math.abs(innerArc.getAngleExtent()) / 360.0;
    return Math.PI * innerArc.getWidth() * fraction;
  }

  @Override
  public boolean transformForCar(Graphics2D g2, double centerPosition, int lane) {
    double thetaStart = -innerArc.getAngleStart() / 180 * Math.PI;
    
    double theta = -centerPosition / getRadiusMeters();
    if (isClockwise())
      theta *= -1;
    
    g2.translate(innerArc.getCenterX(), innerArc.getCenterY());
    g2.rotate(thetaStart + theta);
    g2.translate(getRadiusMeters() + (lane + 0.5) * getLaneWidthMeters(), 0);
    
    return true;
  }

  public double getStartXMeters() {
    return innerArc.getStartPoint().getX();
  }

  public double getStartYMeters() {
    return innerArc.getStartPoint().getY();
  }

  public double getEndXMeters() {
    return innerArc.getEndPoint().getX();
  }

  public double getEndYMeters() {
    return innerArc.getEndPoint().getY();
  }
  
  /**
   * Radius of the inside of the inside lane, in meters.
   * 
   * @return positive; in meters
   */
  public double getRadiusMeters() {
    return innerArc.getWidth() / 2;
  }

  public boolean isClockwise() {
    return innerArc.getAngleExtent() < 0;
  }
}
