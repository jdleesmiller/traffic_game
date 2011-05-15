package de.trafficsimulation.road;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;


/**
 * A road built up from several other connected roads. The order of the roads
 * is important.
 */
public class CompoundRoad extends RoadBase {
  private final List<RoadBase> roads;
  
  /**
   * 
   * @return non-empty
   */
  public List<RoadBase> getRoads() {
    return roads;
  }

  /**
   * 
   * @param roads non-empty
   */
  public CompoundRoad(List<RoadBase> roads) {
    super(roads.get(0).getNumLanes(), roads.get(0).getLaneWidthMeters());
    this.roads = roads;
  }

  @Override
  public Rectangle2D getBoundsMeters() {
    Rectangle2D bounds = roads.get(0).getBoundsMeters();
    for (int i = 1; i < roads.size(); ++i) {
      bounds.add(roads.get(i).getBoundsMeters());
    }
    return bounds;
  }

  @Override
  public List<Shape> getLaneMarkers() {
    List<Shape> laneMarkers = new ArrayList<Shape>(this.roads.size());
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
  public boolean transformForCar(Graphics2D g2,
      double centerPosition, int lane) {
    double length = 0;
    for (RoadBase road : this.roads) {
      double new_length = length + road.getRoadLengthMeters();
      if (centerPosition <= new_length) {
        road.transformForCar(g2, centerPosition - length, lane);
        return true;
      }
      length = new_length;
    }
    return false;
  }
}
