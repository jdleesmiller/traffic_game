package de.trafficsimulation.test;

import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;

import org.junit.Test;

import static org.junit.Assert.*;
import static de.trafficsimulation.game.Utility.*;
import static java.lang.Math.*;

public class TestUtility {
  /// tolerance for floating point comparison
  private static final double DELTA = 1e-6;
  
  @Test
  public void testTranslatePerpendicularly() {
    Line2D.Double l = new Line2D.Double();
    Line2D lt;
    
    //
    // line pointing along the positive x axis
    //
    l.setLine(0, 0, 1, 0);
    
    // translate up two units
    lt = translatePerpendicularly(l, 2);
    assertEquals( 0, lt.getX1(), DELTA);
    assertEquals( 2, lt.getY1(), DELTA);
    assertEquals( 1, lt.getX2(), DELTA);
    assertEquals( 2, lt.getY2(), DELTA);
    
    // translate down two units
    lt = translatePerpendicularly(l, -2);
    assertEquals( 0, lt.getX1(), DELTA);
    assertEquals(-2, lt.getY1(), DELTA);
    assertEquals( 1, lt.getX2(), DELTA);
    assertEquals(-2, lt.getY2(), DELTA);
    
    //
    // line pointing along the negative x axis
    //
    l.setLine(0, 0, -1, 0);
    
    // translate down two units
    lt = translatePerpendicularly(l, 2);
    assertEquals( 0, lt.getX1(), DELTA);
    assertEquals(-2, lt.getY1(), DELTA);
    assertEquals(-1, lt.getX2(), DELTA);
    assertEquals(-2, lt.getY2(), DELTA);
    
    // translate up two units
    lt = translatePerpendicularly(l, -2);
    assertEquals( 0, lt.getX1(), DELTA);
    assertEquals( 2, lt.getY1(), DELTA);
    assertEquals(-1, lt.getX2(), DELTA);
    assertEquals( 2, lt.getY2(), DELTA);
    
    //
    // line pointing along the positive y axis
    //
    l.setLine(0, 0, 0, 1);
    
    // translate left two units
    lt = translatePerpendicularly(l, 2);
    assertEquals(-2, lt.getX1(), DELTA);
    assertEquals( 0, lt.getY1(), DELTA);
    assertEquals(-2, lt.getX2(), DELTA);
    assertEquals( 1, lt.getY2(), DELTA);
    
    // translate right two units
    lt = translatePerpendicularly(l, -2);
    assertEquals( 2, lt.getX1(), DELTA);
    assertEquals( 0, lt.getY1(), DELTA);
    assertEquals( 2, lt.getX2(), DELTA);
    assertEquals( 1, lt.getY2(), DELTA);
    
    //
    // line pointing along the negative y axis
    //
    l.setLine(0, 0, 0, -1);
    
    // translate right two units
    lt = translatePerpendicularly(l, 2);
    assertEquals( 2, lt.getX1(), DELTA);
    assertEquals( 0, lt.getY1(), DELTA);
    assertEquals( 2, lt.getX2(), DELTA);
    assertEquals(-1, lt.getY2(), DELTA);
    
    // translate left two units
    lt = translatePerpendicularly(l, -2);
    assertEquals(-2, lt.getX1(), DELTA);
    assertEquals( 0, lt.getY1(), DELTA);
    assertEquals(-2, lt.getX2(), DELTA);
    assertEquals(-1, lt.getY2(), DELTA);
    
    //
    // diagonal line pointing NE
    //
    l.setLine(1, 1, 2, 2);
    
    // translate up and left two units
    lt = translatePerpendicularly(l, 2);
    assertEquals(1 - sqrt(2), lt.getX1(), DELTA);
    assertEquals(1 + sqrt(2), lt.getY1(), DELTA);
    assertEquals(2 - sqrt(2), lt.getX2(), DELTA);
    assertEquals(2 + sqrt(2), lt.getY2(), DELTA);
    
    // translate down and right two units
    lt = translatePerpendicularly(l, -2);
    assertEquals(1 + sqrt(2), lt.getX1(), DELTA);
    assertEquals(1 - sqrt(2), lt.getY1(), DELTA);
    assertEquals(2 + sqrt(2), lt.getX2(), DELTA);
    assertEquals(2 - sqrt(2), lt.getY2(), DELTA);
  }
  
  @Test
  public void testExpandArc() {
    Arc2D.Double a = new Arc2D.Double();
    Arc2D ae;
    
    //
    // 90-degree arc in quadrant 1
    //
    a.setArc(-1, -1, 2, 2, 0, 90, Arc2D.OPEN);
    assertEquals(0, a.getCenterX(), DELTA);
    assertEquals(0, a.getCenterY(), DELTA);
    assertEquals(2, a.getWidth(), DELTA);
    assertEquals(2, a.getHeight(), DELTA);
    assertEquals(0, a.getAngleStart(), DELTA);
    assertEquals(90, a.getAngleExtent(), DELTA);
    
    // identity
    ae = expandArcRadius(a, 0.0);
    assertEquals(0, ae.getCenterX(), DELTA);
    assertEquals(0, ae.getCenterY(), DELTA);
    assertEquals(2, ae.getWidth(), DELTA);
    assertEquals(2, ae.getHeight(), DELTA);
    assertEquals(0, ae.getAngleStart(), DELTA);
    assertEquals(90, ae.getAngleExtent(), DELTA);
    
    // expand radius by 0.5
    ae = expandArcRadius(a, 0.5);
    assertEquals(0, ae.getCenterX(), DELTA);
    assertEquals(0, ae.getCenterY(), DELTA);
    assertEquals(3, ae.getWidth(), DELTA);
    assertEquals(3, ae.getHeight(), DELTA);
    assertEquals(0, ae.getAngleStart(), DELTA);
    assertEquals(90, ae.getAngleExtent(), DELTA);
  }
  
  @Test
  public void testArcOrientation() {
    Arc2D.Double a = new Arc2D.Double();
    Arc2D ar;
    
    //
    // 90-degree arc in quadrant 1, counter-clocwise
    //
    a.setArc(-1, -1, 2, 2, 0, 90, Arc2D.OPEN);
    assertEquals(0, a.getCenterX(), DELTA);
    assertEquals(0, a.getCenterY(), DELTA);
    assertEquals(2, a.getWidth(), DELTA);
    assertEquals(2, a.getHeight(), DELTA);
    assertEquals(  0, a.getAngleStart(), DELTA);
    assertEquals( 90, a.getAngleExtent(), DELTA);
    
    // reverse
    ar = reverseArc(a);
    assertEquals(0, ar.getCenterX(), DELTA);
    assertEquals(0, ar.getCenterY(), DELTA);
    assertEquals(2, ar.getWidth(), DELTA);
    assertEquals(2, ar.getHeight(), DELTA);
    assertEquals( 90, ar.getAngleStart(), DELTA);
    assertEquals(-90, ar.getAngleExtent(), DELTA);
    
    // reverse again; should recover original
    ar = reverseArc(ar);
    assertEquals(0, ar.getCenterX(), DELTA);
    assertEquals(0, ar.getCenterY(), DELTA);
    assertEquals(2, ar.getWidth(), DELTA);
    assertEquals(2, ar.getHeight(), DELTA);
    assertEquals(  0, ar.getAngleStart(), DELTA);
    assertEquals( 90, ar.getAngleExtent(), DELTA);
  }
}
