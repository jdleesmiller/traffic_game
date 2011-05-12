package de.trafficsimulation.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.Timer;

import de.trafficsimulation.core.Constants;
import de.trafficsimulation.core.MicroStreet;
import de.trafficsimulation.road.RoadBase;

/**
 * Handles the clock for 
 * 
 */
public abstract class NewSimCanvas extends JPanel implements Constants {
  
  private static final long serialVersionUID = 1L;
  
  protected final List<RoadBase> roads;
  
  protected final List<Stroke> roadStrokes;
  
  /**
   * Car with rear bumper at the origin, driving north (negative y-axis,
   * in the display's coordinates).
   */
  protected final Shape carTemplate;
  protected final Shape carBumperTemplate;
  
  /**
   * Target frame rate, in frames per (real) second.
   */
  private static final double TARGET_FPS = 20;

  /**
   * Error tolerance for dealing with times.
   */
  //private static final double EPSILON = 0.0001;
  
  private Timer timer;
  
  private double simTime = 0.0;

  /// road marking pattern
  final static float laneMarkerDash[] = {(float)LINELENGTH_M};
  
  /// how thick to make lane markers
  final static float LANE_MARKER_WIDTH_PX = 1f;
  
  /// road marking stroke
  Stroke laneMarkerStroke; 
  
  /// graphics transform
  AffineTransform metersToPixels;
  
  public NewSimCanvas(List<RoadBase> roads) {
    this.roads = Collections.unmodifiableList(roads);
    
    // create strokes used to draw the road surfaces
    roadStrokes = new Vector<Stroke>(this.roads.size());
    for (RoadBase road : this.roads) {
      roadStrokes.add(new BasicStroke((float)(
        road.getNumLanes() * road.getLaneWidthMeters()),
        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }
    
    // set up timer for animation
    timer = new Timer((int) (1000 / TARGET_FPS), new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        tick();
        simTime += TIMESTEP_S;
        repaint();
      }
    });
    timer.setCoalesce(true);
    
    // build car templates; we just rotate and translate it to draw
    carTemplate = new Rectangle2D.Double(
        -VEH_WIDTH_M / 2.0, -PKW_LENGTH_M, VEH_WIDTH_M, PKW_LENGTH_M);
    carBumperTemplate = new Rectangle2D.Double(
        -VEH_WIDTH_M / 2.0, -PKW_LENGTH_M / 5, VEH_WIDTH_M, PKW_LENGTH_M / 5);
    
    // need to rescale when we're resized
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        handleResize();
      }
    });
  }
  
  public void start() {
    simTime = 0.0;
    timer.start();
  }
  
  public void stop() {
    timer.stop();
  }
  
  public abstract void tick();
  
  public List<RoadBase> getRoads() {
    return roads;
  }

  /**
   * Bounding box of all of the roads.
   * 
   * @return not null
   */
  public Rectangle2D getBoundsMeters() {
    Rectangle2D.Double bounds = new Rectangle2D.Double();
    for (RoadBase road : roads) {
      bounds.add(road.getBoundsMeters());
    }
    return bounds;
  }

  protected void handleResize() {
    int width = getWidth();
    int height = getHeight();
    
    metersToPixels = new AffineTransform();
    Rectangle2D bounds = this.getBoundsMeters();
    
    double scale = 1;
    
    // special case: may get zero or negative widths when component is hidden;
    if (width > 0 && height > 0) {
      double scaleX = width / bounds.getWidth();
      double scaleY = height / bounds.getHeight();
      scale = Math.min(scaleX, scaleY);
    }
    
    // set the metersToPixel transform so the road fits into the window
    metersToPixels.translate(width / 2, height / 2);
    metersToPixels.scale(scale, scale);
    metersToPixels.translate(-bounds.getCenterX(), -bounds.getCenterY());
    
    // the lane marker width is set in pixels, not meters
    laneMarkerStroke = new BasicStroke((float)(LANE_MARKER_WIDTH_PX/scale),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
        (float) LINELENGTH_M, laneMarkerDash, 0.0f);
    
    repaint();
  }
  
  protected abstract void paintVehicles(Graphics2D g2);

  /**
   * TODO find somewhere to put this
   * Note that it doesn't work for OnRamp
   * 
   * @param g2
   */
  protected void paintVehiclesOnStreet(Graphics2D g2,
      RoadBase road, MicroStreet street) {
    AffineTransform txCopy = g2.getTransform();
    
    int numCars = street.positions.size();
    for (int i = 0; i < numCars; ++i) {
      int lane = street.lanes.elementAt(i);
      double position = street.positions.elementAt(i);
      if (road.transformForCarAt(g2, lane, position))
        paintCar(g2);
      g2.setTransform(txCopy);
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    
    g2.transform(metersToPixels);
    
    for (int i = 0; i < roads.size(); ++i) {
      RoadBase road = roads.get(i);
      Stroke roadStroke = roadStrokes.get(i);
      
      g2.setColor(Color.GRAY);
      g2.setStroke(roadStroke);
      g2.draw(road.getRoadCenter());
      
      g2.setColor(Color.WHITE);
      g2.setStroke(laneMarkerStroke);
      for (Shape marker : road.getLaneMarkers()) {
        g2.draw(marker);
      }
    }
    
    paintVehicles(g2);
  }
  
  /**
   * Paint
   * 
   * TODO need to know color and whether it's a car or truck
   * 
   * @param g2 not null
   */
  public void paintCar(Graphics2D g2) {
    g2.setColor(Color.WHITE);
    g2.fill(carTemplate);
    
    // draw "bumper" at rear
    g2.setColor(Color.RED);
    g2.fill(carBumperTemplate);
  }

  /**
   * @return the simTime
   */
  public double getSimTime() {
    return simTime;
  }
}
