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

import javax.swing.JPanel;
import javax.swing.Timer;

import de.trafficsimulation.core.Constants;
import de.trafficsimulation.core.MicroStreet;
import de.trafficsimulation.road.RoadBase;

/**
 * Handles the clock for 
 * 
 */
public class NewSimCanvas extends JPanel implements Constants {
  
  private static final long serialVersionUID = 1L;
  
  private final MicroStreet street;
  
  protected final RoadBase road;
  
  /**
   * Car with rear bumper at the origin, driving north (negative y-axis,
   * in the display's coordinates).
   */
  protected final Shape carTemplate;
  
  /**
   * Target frame rate, in frames per (real) second.
   */
  private static final double TARGET_FPS = 30;

  /**
   * Error tolerance for dealing with times.
   */
  //private static final double EPSILON = 0.0001;
  
  private Timer timer;
  
  private double simTime = 0.0;

  protected final Stroke roadStroke;
  
  /// road marking pattern
  final static float laneMarkerDash[] = {(float)LINELENGTH_M};
  
  /// how thick to make lane markers
  final static float LANE_MARKER_WIDTH_PX = 1f;
  
  /// road marking stroke
  Stroke laneMarkerStroke; 
  
  /// graphics transform
  AffineTransform metersToPixels;
  
  public NewSimCanvas(MicroStreet street, RoadBase road) {
    this.street = street;
    this.road = road;
    
    // TODO end style should be round
    roadStroke = new BasicStroke((float)(
        road.getNumLanes() * road.getLaneWidthMeters()));
    
    timer = new Timer((int) (1000 / TARGET_FPS), new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        tick();
        repaint();
      }
    });
    timer.setCoalesce(true);
    
    carTemplate = new Rectangle2D.Double(
        -VEH_WIDTH_M / 2.0, -PKW_LENGTH_M, VEH_WIDTH_M, PKW_LENGTH_M);
    
    // need to rescale when we're resized
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        handleResize();
      }
    });
  }
  
  public MicroStreet getStreet() {
    return street;
  }

  public void start() {
    // TODO need to reset the MicroStreet somehow, if we're going to allow
    // multiple sims with a single instance of this canvas
    
    simTime = 0.0;
    timer.start();
  }
  
  public void stop() {
    timer.stop();
  }
  
  private void tick() {
    // TODO figure out how to handle these parameters 
    street.update(simTime, TIMESTEP_S, MainFrame.SCENARIO_RING_ROAD,
        0.001 * DENS_INIT_INVKM, 0, 0, 0, 0.2);
    
    simTime += TIMESTEP_S;
  }

  protected void handleResize() {
    int width = getWidth();
    int height = getHeight();
    
    metersToPixels = new AffineTransform();
    
    // special case: called before display, or resized to zero
    if (width == 0 || height == 0) {
      return;
    }
    
    // set the metersToPixel transform so the road fits into the window
    int pixelSize = Math.min(width, height);
    Rectangle2D bounds = road.getBoundsMeters();
    double meterSize = Math.max(bounds.getWidth(), bounds.getHeight());
    
    double scale = pixelSize / meterSize;
    metersToPixels.translate(width / 2, height / 2);
    metersToPixels.scale(scale, scale);
    metersToPixels.translate(-bounds.getCenterX(), -bounds.getCenterY());
    
    // the lane marker width is set in pixels, not meters
    laneMarkerStroke = new BasicStroke((float)(LANE_MARKER_WIDTH_PX/scale),
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
        (float) LINELENGTH_M, laneMarkerDash, 0.0f);
    
    repaint();
  }

  protected void paintVehicles(Graphics2D g2) {
    AffineTransform txCopy = g2.getTransform();
    
    MicroStreet street = getStreet();
    int numCars = street.positions.size();
    for (int i = 0; i < numCars; ++i) {
      road.transformForCarAt(g2,
          street.lanes.elementAt(i), street.positions.elementAt(i));
      g2.setColor(Color.RED);
      g2.fill(carTemplate);
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
    
    g2.setColor(Color.GRAY);
    g2.setStroke(roadStroke);
    g2.draw(road.getRoadCenter());
    
    g2.setColor(Color.WHITE);
    g2.setStroke(laneMarkerStroke);
    for (Shape marker : road.getLaneMarkers()) {
      g2.draw(marker);
    }
    
    paintVehicles(g2);
  }
}
