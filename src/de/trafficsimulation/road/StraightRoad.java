package de.trafficsimulation.road;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import static de.trafficsimulation.game.Utility.*;

/**
 * A straight section of road.
 */
public class StraightRoad extends RoadBase {
  private final Line2D insideLine;
  private final Line2D roadCenter;
  private final List<Shape> laneMarkers;
  
  public StraightRoad(int numLanes, double laneWidthMeters,
      double startXMeters, double startYMeters,
      double endXMeters, double endYMeters)
  {
    super(numLanes, laneWidthMeters);
    
    insideLine = new Line2D.Double(
        startXMeters, startYMeters,
        endXMeters, endYMeters);
    
    roadCenter = translatePerpendicularly(insideLine,
        numLanes * laneWidthMeters / 2.0);
    
    laneMarkers = new ArrayList<Shape>(numLanes - 1);
    for (int lane = 1; lane < numLanes; ++lane) {
      laneMarkers.add(translatePerpendicularly(insideLine,
          lane * laneWidthMeters));
    }
  }

  @Override
  public Rectangle2D getBoundsMeters() {
    Rectangle2D bounds = insideLine.getBounds2D();
    bounds.add(translatePerpendicularly(insideLine,
        numLanes * laneWidthMeters).getBounds2D());
    return bounds;
  }

  @Override
  public List<Shape> getLaneMarkers() {
    return laneMarkers;
  }

  @Override
  public Shape getRoadCenter() {
    return roadCenter;
  }

  @Override
  public double getRoadLengthMeters() {
    return insideLine.getP1().distance(insideLine.getP2());
  }

  @Override
  public boolean transformForCarAt(Graphics2D g2, int lane, double position) {
    double dy = insideLine.getY2() - insideLine.getY1();
    double dx = insideLine.getX2() - insideLine.getX1();
    double theta = Math.atan2(dy, dx);
    
    g2.translate(insideLine.getX1(), insideLine.getY1());
    g2.rotate(theta + Math.PI/2);
    g2.translate((lane + 0.5) * getLaneWidthMeters(), -position);
    
    return true;
  }
  
  public double getStartXMeters() {
    return insideLine.getX1();
  }

  public double getStartYMeters() {
    return insideLine.getY1();
  }

  public double getEndXMeters() {
    return insideLine.getX2();
  }

  public double getEndYMeters() {
    return insideLine.getY2();
  }
}
