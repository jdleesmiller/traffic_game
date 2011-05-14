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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

import de.trafficsimulation.core.Constants;
import de.trafficsimulation.core.MicroStreet;
import de.trafficsimulation.core.Moveable;
import de.trafficsimulation.road.RoadBase;

/**
 * Handles the clock for 
 * 
 */
public abstract class SimCanvas extends JPanel implements Constants {
  
  private static final long serialVersionUID = 1L;
  
  protected final List<RoadBase> roads;
  
  protected final List<Stroke> roadStrokes;
  
  /**
   * Car with rear bumper at the origin, driving north (negative y-axis,
   * in the display's coordinates).
   */
  protected final Shape carTemplate;
  protected final Shape carBumperTemplate;
  
  protected final Shape truckTemplate;
  protected final Shape truckBumperTemplate;
  
  protected static final Color ROAD_COLOR = Color.GRAY;
  protected static final Color LANE_MARKER_COLOR = Color.WHITE;
  
  /**
   * Target frame rate, in frames per (real) second.
   */
  private static final double TARGET_FPS = 20;

  /**
   * Error tolerance for dealing with times.
   */
  //private static final double EPSILON = 0.0001;
  
  private Timer timer;
  
  /// road marking pattern
  final static float laneMarkerDash[] = {(float)LINELENGTH_M};
  
  /// how thick to make lane markers
  final static float LANE_MARKER_WIDTH_PX = 1f;
  
  /// road marking stroke for dashed lines
  Stroke laneMarkerStroke; 
  
  /// road marking stroke for solid lines
  Stroke solidLaneMarkerStroke; 
  
  /// graphics transform
  AffineTransform metersToPixels;
  
  public SimCanvas(List<RoadBase> roads) {
    this.roads = Collections.unmodifiableList(roads);
    
    // create strokes used to draw the road surfaces
    roadStrokes = new ArrayList<Stroke>(this.roads.size());
    for (RoadBase road : this.roads) {
      roadStrokes.add(new BasicStroke((float)(
        road.getNumLanes() * road.getLaneWidthMeters()),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
    }
    
    // set up timer for animation
    timer = new Timer((int) (1000 / TARGET_FPS), new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        tick();
        repaint();
      }
    });
    timer.setCoalesce(true);
    
    // build car templates; we just rotate and translate it to draw
    carTemplate = new Rectangle2D.Double(
        -VEH_WIDTH_M / 2.0, -PKW_LENGTH_M, VEH_WIDTH_M, PKW_LENGTH_M);
    carBumperTemplate = new Rectangle2D.Double(
        -VEH_WIDTH_M / 2.0, -1, VEH_WIDTH_M, 1);
    
    truckTemplate = new Rectangle2D.Double(
        -VEH_WIDTH_M / 2.0, -LKW_LENGTH_M, VEH_WIDTH_M, LKW_LENGTH_M);
    truckBumperTemplate = new Rectangle2D.Double(
        -VEH_WIDTH_M / 2.0, -1, VEH_WIDTH_M, 1);
    
    // need to rescale when we're resized
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        handleResize();
      }
    });
  }
  
  public void start() {
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
    
    // the lane marker widths are set in pixels, not meters
    laneMarkerStroke = new BasicStroke((float)(LANE_MARKER_WIDTH_PX/scale),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
        (float) LINELENGTH_M, laneMarkerDash, 0.0f);
    solidLaneMarkerStroke = new BasicStroke((float)(LANE_MARKER_WIDTH_PX/scale),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    
    repaint();
  }
  
  protected abstract void paintVehicles(Graphics2D g2);

  /**
   * TODO find somewhere to put this
   * 
   * @param g2
   */
  protected void paintVehiclesOnStreet(Graphics2D g2,
      RoadBase road, MicroStreet street) {
    AffineTransform txCopy = g2.getTransform();
    
    for (Moveable vehicle : street.getStreet()) {
      if (road.transformForCarAt(g2, vehicle.lane(), vehicle.position()))
        paintVehicle(vehicle, g2);
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
    
    paintRoads(g2);
    paintVehicles(g2);
  }
  
  /**
   * Paint roads and lane markers.
   * 
   * @param g2
   */
  protected void paintRoads(Graphics2D g2) {
    for (int i = 0; i < roads.size(); ++i) {
      RoadBase road = roads.get(i);
      Stroke roadStroke = roadStrokes.get(i);
      
      g2.setColor(ROAD_COLOR);
      g2.setStroke(roadStroke);
      g2.draw(road.getRoadCenter());
      
      g2.setColor(LANE_MARKER_COLOR);
      g2.setStroke(laneMarkerStroke);
      for (Shape marker : road.getLaneMarkers()) {
        g2.draw(marker);
      }
    }
  }
  
  /**
   * Paint a car or truck.
   * 
   * @param vehicle 
   * 
   * @param g2 not null
   */
  public void paintVehicle(Moveable vehicle, Graphics2D g2) {
    if (vehicle.length() < PKW_LENGTH_M) {
      // don't draw obstacles
    } else if (vehicle.length() == LKW_LENGTH_M) {
      g2.setColor(vehicle.color());
      g2.fill(truckTemplate);
      // draw "bumper" at rear
      g2.setColor(Color.RED);
      g2.fill(truckBumperTemplate);
    } else {
      g2.setColor(vehicle.color());
      g2.fill(carTemplate);
      // draw "bumper" at rear
      g2.setColor(Color.RED);
      g2.fill(carBumperTemplate);
    }
  }
}
