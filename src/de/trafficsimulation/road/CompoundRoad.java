package de.trafficsimulation.road;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

/**
 * A road built up from several other connected roads. The order of the roads
 * is important.
 */
public class CompoundRoad extends RoadBase {
  private final Vector<RoadBase> roads;
  
  /**
   * 
   * @param roads non-empty
   */
  public CompoundRoad(Vector<RoadBase> roads) {
    super(roads.firstElement().getNumLanes(),
        roads.firstElement().getLaneWidthMeters());
    this.roads = roads;
  }

  @Override
  public Rectangle2D getBoundsMeters() {
    Rectangle2D bounds = roads.firstElement().getBoundsMeters();
    for (int i = 1; i < roads.size(); ++i) {
      bounds.add(roads.elementAt(i).getBoundsMeters());
    }
    return bounds;
  }

  @Override
  public Vector<Shape> getLaneMarkers() {
    Vector<Shape> laneMarkers = new Vector<Shape>(this.roads.size());
    for (RoadBase road : this.roads) {
      laneMarkers.addAll(road.getLaneMarkers());
    }
    return laneMarkers;
  }

  @Override
  public Shape getRoadCenter() {
    GeneralPath path = new GeneralPath();
    for (RoadBase road : this.roads) {
      path.append(road.getRoadCenter(), true);
    }
    return path;
  }

  @Override
  public double getRoadLengthMeters() {
    double length = 0;
    for (RoadBase road : this.roads) {
      length += road.getRoadLengthMeters();
    }
    return length;
  }

  @Override
  public void transformForCarAt(Graphics2D g2, int lane, double position) {
    // TODO Auto-generated method stub
  }
}
