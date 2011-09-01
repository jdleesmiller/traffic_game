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
 * Base class for simulation animation (with a timer); draws roads and
 * vehicles. Subclasses draw specific roads and vehicles as required.
 */
public abstract class SimCanvas extends JPanel implements Constants {
  
  private static final long serialVersionUID = 1L;
  
  private int timeStepsPerFrame;
  
  private final Timer timer;
    
  protected final List<RoadBase> roads;
  
  protected final List<Stroke> roadStrokes;
  
  /**
   * Target display frame rate, in frames per (real) second.
   */
  public static final int DEFAULT_TARGET_FPS = 30;
  
  /**
   * Car with center at the origin, driving north (negative y-axis, in the
   * display's coordinates).
   */
  protected final Shape carTemplate;
  protected final Shape carBumperTemplate;
  
  protected final Shape truckTemplate;
  protected final Shape truckBumperTemplate;
  
  protected static final Color ROAD_COLOR = Color.BLACK;
  protected static final Color LANE_MARKER_COLOR = Color.WHITE;
  
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
    this(roads, DEFAULT_TARGET_FPS);
  }
  
  public SimCanvas(List<RoadBase> roads, int targetFPS) {
    super(true); // double buffer
    
    this.timeStepsPerFrame = 1;
    
    this.timer = new Timer(1000 / targetFPS, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < timeStepsPerFrame; ++i)
          tick();
        repaint();
      }
    });
    
    this.roads = Collections.unmodifiableList(roads);
    
    // create strokes used to draw the road surfaces
    roadStrokes = new ArrayList<Stroke>(this.roads.size());
    for (RoadBase road : this.roads) {
      roadStrokes.add(new BasicStroke((float)(
        road.getNumLanes() * road.getLaneWidthMeters()),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
    }
    
    // build car templates; we just rotate and translate it to draw
    carTemplate = new Rectangle2D.Double(
        -VEH_WIDTH_M / 2.0, -PKW_LENGTH_M/2.0, VEH_WIDTH_M, PKW_LENGTH_M);
    carBumperTemplate = new Rectangle2D.Double(
        -VEH_WIDTH_M / 2.0, PKW_LENGTH_M/2.0-1, VEH_WIDTH_M, 1);
    
    truckTemplate = new Rectangle2D.Double(
        -VEH_WIDTH_M / 2.0, -LKW_LENGTH_M/2.0, VEH_WIDTH_M, LKW_LENGTH_M);
    truckBumperTemplate = new Rectangle2D.Double(
        -VEH_WIDTH_M / 2.0, LKW_LENGTH_M/2.0-1, VEH_WIDTH_M, 1);
    
    // need to rescale when we're resized
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        handleResize();
      }
    });
    
    // call once to initialise
    handleResize();
  }
  
  /**
   * Start simulation with given seed.
   * 
   * @param seed -1 for time-dependent seed
   */
  public void start(long seed) {
    timer.start();
  }
  
  /**
   * Start with a time-dependent seed.
   */
  public void start() {
    this.start(-1L);
  }

  /**
   * Stop the simulation timer.
   */
  public void stop() {
    timer.stop();
  }
  
  /**
   * Stop the paint timer, but don't destroy the sim. It is an error to call
   * this before the sim has been started, or after it has been stopped.   */
  public void pause() {
    timer.stop();
  }
  
  /**
   * Start the paint timer, but don't create a new sim. It is an error to call
   * this before the sim has been started, or after it has been stopped.
   */
  public void resume() {
    timer.start();
  }
  
  /**
   * Advance simulation by one time step.
   */
  public abstract void tick();
  
  /**
   * The roads that are drawn on this canvas.
   * 
   * @return not null; not empty
   */
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
    metersToPixels = new AffineTransform();
    double scale = Utility.transformToFit(this,
        this.getBoundsMeters(), metersToPixels);
    
    // the lane marker widths are set in pixels, not meters
    laneMarkerStroke = new BasicStroke((float)(LANE_MARKER_WIDTH_PX/scale),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
        (float) LINELENGTH_M, laneMarkerDash, 0.0f);
    solidLaneMarkerStroke = new BasicStroke((float)(LANE_MARKER_WIDTH_PX/scale),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    
    repaint();
  }
  
  /**
   * Subclasses override this to paint the vehicles on their particular roads.
   * 
   * @param g2
   */
  protected abstract void paintVehicles(Graphics2D g2);

  /**
   * @param g2
   */
  protected void paintVehiclesOnStreet(Graphics2D g2,
      RoadBase road, MicroStreet street) {
    AffineTransform txCopy = g2.getTransform();
    
    for (Moveable vehicle : street.getStreet()) {
      // vehicle position is measured to the rear bumper in the sim, but we get
      // better results visually if we use the vehicle center 
      if (road.transformForCar(g2,
          vehicle.position() + vehicle.length() / 2.0, vehicle.lane()))
        paintVehicle(vehicle, g2);
      g2.setTransform(txCopy);
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    // if we're paused, don't paint anything
    if (!timer.isRunning())
      return;
    
    // note: we have to do our drawing on a copy of the original graphics
    // object, in order to preserve the transform etc. for other components
    // that need to draw themselves after us (that is, child components).
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    
    paintAnnotations(g2);
    
    g2.transform(metersToPixels);
    
    paintRoads(g2);
    paintVehicles(g2);
  }
  
  /**
   * Called to paint things before the metersToPixels transform is applied.
   */
  protected void paintAnnotations(Graphics2D g2) {
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
  
  /**
   * Number of simulation time steps to simulate before drawing one frame to
   * the screen. This gives a simple way of making the sims appear to run
   * faster.
   * 
   * @return positive
   */
  public int getTimeStepsPerFrame() {
    return timeStepsPerFrame;
  }

  /**
   * See getTimeStepsPerFrame.
   * 
   * @param timeStepsPerFrame positive
   */
  public void setTimeStepsPerFrame(int timeStepsPerFrame) {
    this.timeStepsPerFrame = timeStepsPerFrame;
  }
}
