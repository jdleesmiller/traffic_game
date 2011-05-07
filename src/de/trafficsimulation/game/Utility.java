package de.trafficsimulation.game;

import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import static java.lang.Math.*;

public class Utility {
  
  /**
   * Translate line perpendicularly by given distance. 
   * 
   * The sign convention for distance comes from the fact that we add PI/2 to
   * obtain the perpendicular to translate along.
   * 
   * @param line modified in place and returned
   * @param distance
   * 
   * @return line
   */
  public static Line2D translatePerpendicularlyInPlace(
      Line2D line, double distance)
  {
    // take P1 as the origin and compute the angle formed by the vector to P2
    // and the positive x axis; add PI/2 to get the perpendicular angle;
    // translate both start and end point along this angle
    double delX = line.getX2() - line.getX1();
    double delY = line.getY2() - line.getY1();
    double theta = atan2(delY, delX) + PI / 2;
    double dx = distance * cos(theta);
    double dy = distance * sin(theta);
    line.setLine(
        line.getX1() + dx, line.getY1() + dy,
        line.getX2() + dx, line.getY2() + dy);
    return line;
  }
  
  /**
   * Return a new line that is translated perpendicularly from the given 
   * line by the given distance; see translatePerpendicularlyInPlace.
   * 
   * @param line not modified
   * @param distance
   * @return a new line
   */
  public static Line2D translatePerpendicularly(Line2D line,
      double distance)
  {
    return translatePerpendicularlyInPlace((Line2D)line.clone(),
        distance);
  }
  
  /**
   * Expand arc radius by given amount.
   * 
   * @param arc modified in place and returned
   * @param dr larger than negative of current radius
   * @return arc
   */
  public static Arc2D expandArcRadiusInPlace(Arc2D arc, double dr) {
    arc.setArcByCenter(arc.getCenterX(), arc.getCenterY(),
        arc.getWidth() / 2 + dr,
        arc.getAngleStart(), arc.getAngleExtent(), arc.getArcType());
    return arc;
  }
  
  /**
   * Return a new arc with the same center and angles but a radius changed by
   * the given amount.
   * 
   * @param arc not modified
   * @param dr larger than negative of current radius
   * @return a new arc of the same type as arc
   */
  public static Arc2D expandArcRadius(Arc2D arc, double dr) {
    return expandArcRadiusInPlace((Arc2D)arc.clone(), dr);
  }
  
  /**
   * Reverse the start and end point of the given arc.
   * 
   * @param arc modified in place and returned
   * @return arc
   */
  public static Arc2D reverseArcInPlace(Arc2D arc) {
    arc.setAngleStart(arc.getAngleStart() + arc.getAngleExtent());
    arc.setAngleExtent(-arc.getAngleExtent());
    return arc;
  }
  
  /**
   * Return a new arc with the same center and radius but with the start point
   * swapped with the end point.
   * 
   * @param arc not modified
   * @return a new arc with the start and end points reversed
   */
  public static Arc2D reverseArc(Arc2D arc) {
    return reverseArcInPlace((Arc2D)arc.clone());
  }
}
