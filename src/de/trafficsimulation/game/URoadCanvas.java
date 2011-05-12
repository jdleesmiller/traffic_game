package de.trafficsimulation.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JFrame;

import de.trafficsimulation.core.MicroStreet;
import de.trafficsimulation.core.OnRamp;
import de.trafficsimulation.road.RoadBase;
import de.trafficsimulation.road.StraightRoad;
import de.trafficsimulation.road.URoad;

public class URoadCanvas extends SimCanvas {
  
  protected MicroStreet street;
  protected OnRamp onRamp;
  
  // single mutable parameter at present
  protected double qIn = Q_INIT2 / 3600.;
  
  // TODO figure out something to do with these...
  protected final double density = 0.001 * DENS_INIT_INVKM; // avg. density closed s.
  protected final double p_factor = 0.; // lanechanging: politeness factor
  protected final double deltaB = 0.2; // lanechanging: changing threshold
  protected final int floatcar_nr = 0;
  protected final double p_factorRamp = 0.; // ramp Lanechange factor
  protected final double deltaBRamp = DELTABRAMP_INIT; // ramp Lanechange factor
  protected final double perTr = FRAC_TRUCK_INIT;
  protected final double qRamp = QRMP_INIT2 / 3600.;
    
  private static final long serialVersionUID = 1L;

  public URoadCanvas() {
    super(makeRoads());
  }
  
  /** For testing */
  public static void main(String [] args) {
    JFrame f = new JFrame("on ramp test");
    f.setSize(1000,500);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    SimCanvas c = new URoadCanvas();
    f.add(c);
    f.setVisible(true);
    c.start();
  }
  
  @Override
  public void start() {
    
    this.street = new MicroStreet(getURoad().getRoadLengthMeters(),
        density, p_factor, deltaB, MainFrame.SCENARIO_RING_ROAD);
    
    double mergingPos = Math.PI * RADIUS_M + STRAIGHT_RDLEN_M + 0.5 * L_RAMP_M;
    
    // the obstacle at the end is inserted at the end of the visible ramp
    this.onRamp = new OnRamp(this.street,
        getOnRampRoad().getRoadLengthMeters(), // the whole visible ramp
        L_RAMP_M, mergingPos, p_factorRamp, deltaBRamp);
    
    super.start();
  }

  @Override
  public void stop() {
    super.stop();
    this.street = null;
    this.onRamp = null;
  }

  @Override
  public void tick() {
    if (this.street == null)
      return;
    
    street.update(TIMESTEP_S, density, qIn, perTr, p_factor, deltaB);
    onRamp.update(TIMESTEP_S, qRamp, perTr, p_factorRamp, deltaBRamp);
  }

  @Override
  protected void paintVehicles(Graphics2D g2) {
    if (this.street == null)
      return;
    
    AffineTransform txCopy = g2.getTransform();
    
    // BOUNDING BOXES
    g2.setColor(Color.GREEN);
    g2.draw(((URoad)getURoad()).getInputStraightRoad().getBoundsMeters());
    g2.setColor(Color.BLUE);
    g2.draw(((URoad)getURoad()).getOutputStraightRoad().getBoundsMeters());
    g2.setColor(Color.CYAN);
    g2.draw(((URoad)getURoad()).getCurveRoad().getBoundsMeters());
    
    // MERGE POINT TODO somewhere else
    double mergePointPos = getOnRampRoad().getRoadLengthMeters() - L_RAMP_M;
    getOnRampRoad().transformForCarAt(g2, 0, mergePointPos);
    g2.setColor(Color.YELLOW);
    g2.fill(new Rectangle2D.Double(0, 0, 5, 2));
    g2.setTransform(txCopy);
    
    paintVehiclesOnStreet(g2, getURoad(), street);
    
    int numCars = onRamp.positions.size();
    for (int i = 0; i < numCars; ++i) {
      double position = onRamp.positions.get(i);
      if (getOnRampRoad().transformForCarAt(g2, 0, position))
        paintCar(g2);
      g2.setTransform(txCopy);
    }
  }
  
  private static ArrayList<RoadBase> makeRoads()
  {
    // the u section
    URoad uRoad = new URoad(2, LANEWIDTH_M, 0, 0, RADIUS_M, STRAIGHT_RDLEN_M);
    
    // the on ramp
    double rampY = uRoad.getBoundsMeters().getMaxY();
    double rampStartX = -50; // arbitrary
    double rampEndX = RADIUS_M + L_RAMP_M;
    StraightRoad onRampRoad = new StraightRoad(1, LANEWIDTH_M,
        rampStartX, rampY, rampEndX, rampY);
    
    ArrayList<RoadBase> roads = new ArrayList<RoadBase>();
    roads.add(uRoad);
    roads.add(onRampRoad);
    return roads;
  }
  
  private RoadBase getURoad() {
    return getRoads().get(0);
  }
  
  private RoadBase getOnRampRoad() {
    return getRoads().get(1);
  }
}
