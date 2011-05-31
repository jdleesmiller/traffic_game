package de.trafficsimulation.game;

import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;

import javax.swing.JComponent;

import static java.lang.Math.*;

public class Utility {
  /**
   * Support for debug and info logging. By default, this logs to stderr.
   */
  public final static Logger log = Logger.getAnonymousLogger();
  
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
  
  /**
   * Modify the given transform so that the given bounds fit inside the given
   * component. If one direction is not tight, the bounds are centered in that
   * direction.
   * 
   * @param c not null
   * @param bounds not null
   * @param transform not null
   * @return positive
   */
  public static double transformToFit(JComponent c,
      Rectangle2D bounds, AffineTransform transform)
  {
    int width = c.getWidth();
    int height = c.getHeight();
    
    // subtract off border, if we have one
    Insets insets = c.getInsets();
    width -= insets.left + insets.right;
    height -= insets.top + insets.bottom;
    
    // special case: may get zero or negative widths when component is hidden;
    double scale = 1;
    if (width > 0 && height > 0) {
      double scaleX = width / bounds.getWidth();
      double scaleY = height / bounds.getHeight();
      scale = Math.min(scaleX, scaleY);
    }
    
    // set the metersToPixel transform so the road fits into the window
    transform.translate(insets.left + width / 2, insets.top + height / 2);
    transform.scale(scale, scale);
    transform.translate(-bounds.getCenterX(), -bounds.getCenterY());
    
    return scale;
  }
}
