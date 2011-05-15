package de.trafficsimulation.road;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;


/**
 * Base class for a road or collection of roads.
 */
public abstract class RoadBase {

  protected final double laneWidthMeters;
  protected final int numLanes;

  public RoadBase(int numLanes, double laneWidthMeters) {
    this.numLanes = numLanes;
    this.laneWidthMeters = laneWidthMeters;
  }

  /**
   * Bounding box that contains the road; does not include any padding.
   * 
   * @return coordinates in meters
   */
  public abstract Rectangle2D getBoundsMeters();

  /**
   * Lane markers, from left (inner lane) to right (outer lane); coordinates
   * in meters.
   */
  public abstract List<Shape> getLaneMarkers();

  /**
   * Width of one lane, in meters.
   * 
   * @return positive
   */
  public double getLaneWidthMeters() {
    return laneWidthMeters;
  }
  
  /**
   * Number of lanes.
   * 
   * @return positive
   */
  public int getNumLanes() {
    return numLanes;
  }
  
  /**
   * Road center line; coordinates in meters.
   */
  public abstract Shape getRoadCenter();

  /**
   * Road length in meters.
   * 
   * This is the length of the inside of the inside lane, which is how it was
   * computed in the original SimCanvas (no correction for curvature with
   * multiple lanes). 
   * 
   * @return positive
   */
  public abstract double getRoadLengthMeters();

  /**
   * Change the transform on the given graphics object so that a car drawn
   * with its rear bumper at the origin and pointing north (toward the negative
   * y axis, in display coordinates) shows up in the right position.
   * 
   * @param g2 alters the transform on this object
   * @param centerPosition position of the center of the car along the road
   * @param lane which lane the car is in
   * @return true iff the car is on this road
   */
  public abstract boolean transformForCar(Graphics2D g2,
      double centerPosition, int lane);
}