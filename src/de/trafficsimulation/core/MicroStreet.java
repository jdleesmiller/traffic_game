package de.trafficsimulation.core;

import java.awt.Color;
import java.util.Random;
import java.util.Vector;

/**
 * Representation of a two-lane road section for one direction. The main
 * elements of MicroStreet are
 * <ul>
 * <li>street, a vector of Moveable's representiung the vehicles,
 * <li>The update method invoked in every time step. Ammong others, it calls all
 * emthods mentioned below.
 * <li>Methods for moving the vehicles (translate), accelerating them
 * (accelerate) and performing the lane changes (changeLanes).
 * <li>A sorting routine sort for rearranging the vehicle order in street in the
 * order of decreasing longitudinal positions
 * <li>The method ioFlow implementing the upstrea and downstream boundary
 * conditions (inflow and outflow).
 * </ul>
 * <br>
 * <br>
 * The realization of an on ramp, the class Onramp, is derived from this class.
 */

public class MicroStreet implements Constants {

  static final int LEFT = 0;
  static final int RIGHT = 1;

  // ######## colors <martin nov07>

  public static final Color colorCar = new Color(210, 0, 0);
  public static final Color colorTruck = new Color(40, 40, 60);
  public static final Color colorPerturb = new Color(255, 130, 0);

  // vector of Moveables
  // final int IMAXINIT = (int)(0.001*RADIUS_M * 4 * Math.PI * DENS_MAX_INVKM +
  // 10);
  final int IMAXINIT = 100;
  protected Vector<Moveable> street = new Vector<Moveable>(IMAXINIT);

  // vector of data of Moveables for output (only model is missing)

  public Vector<Double> positions = new Vector<Double>(IMAXINIT);
  public Vector<Double> velocities = new Vector<Double>(IMAXINIT);
  public Vector<Integer> numbers = new Vector<Integer>(IMAXINIT);
  public Vector<Integer> lanes = new Vector<Integer>(IMAXINIT);
  public Vector<Color> colors = new Vector<Color>(IMAXINIT);
  public Vector<Double> lengths = new Vector<Double>(IMAXINIT);

  // additional vectors for output

  public Vector<Double> distances = new Vector<Double>(IMAXINIT);
  public Vector<Double> old_pos = new Vector<Double>(IMAXINIT);
  public Vector<Integer> old_lanes = new Vector<Integer>(IMAXINIT);
  public Vector<Integer> old_numbers = new Vector<Integer>(IMAXINIT);

  // info if cars are removed in closed system

  public boolean circleCarsRemoved = false;

  // floating car data
  public double fcd = 0.0; // distance
  public double fcvd = 0.0; // approaching rate
  public double fcvel = 0.0; // v
  public double fcacc = 0.0; // acceleration
  public int fcnr = 0;
  public boolean red = false;

  // longitudinal models;
  private MicroModel idmCar = new IDMCar();
  private MicroModel idmTruck = new IDMTruck();

  // lane-change models (p, db, smin, bsave)

  // !!! truck (=> vw for better impl!)
  protected LaneChange polite = new LaneChange(P_FACTOR_TRUCK, DB_TRUCK,
      MAIN_SMIN, MAIN_BSAVE, BIAS_RIGHT_TRUCK);

  // !!! car (=> vw for better impl!)
  protected LaneChange inconsiderate = new LaneChange(P_FACTOR_CAR, DB_CAR,
      MAIN_SMIN, MAIN_BSAVE, BIAS_RIGHT_CAR);

  // neighbours

  protected Moveable vL, vR, hL, hR;

  // can be modified interactively

  // mt apr05: seed 42 eingefuehrt wie in 3dsim
  protected Random random = new Random(42); // for truck perc. and veh. numbers
  protected int choice_Szen;
  protected int choice_Geom; // circle or U
  private double roadLength; // length depends on choice_Szen
  protected double uppos; // location where flow-conserving bottleneck begins

  // time and simulation control

  protected double time;

  // Source: nin = integral of inflow mod 1
  protected double nin = 0.0;
  
  // see getNumCarsOut
  private int numCarsOut;

  public MicroStreet(double length, double density, double p_factor,
      double deltaB, int floatcar_nr, int choice_Szen) {
    time = 0;
    setRoadLength(length);
    uppos = 0.5 * getRoadLength();
    this.choice_Szen = choice_Szen;
    this.choice_Geom = ((choice_Szen == 1) || (choice_Szen == 6)) ? 0 : 1;
    inconsiderate.set_p(p_factor);
    inconsiderate.set_db(deltaB);
    double mult = ((choice_Geom == 0) && CLOCKWISE) ? (-1) : 1;
    double bias_right_truck = BIAS_RIGHT_TRUCK;
    double bias_right_car = BIAS_RIGHT_CAR;
    polite.set_biasRight(mult * bias_right_truck);
    inconsiderate.set_biasRight(mult * bias_right_car);

    fcnr = floatcar_nr;

    System.out.println("MicroStreet(args) cstr: roadLength=" + getRoadLength());

    // ### closed circle; free traffic or stop&go, depending on density

    if (choice_Szen == 1) {
      int nCars = (int) (density * getRoadLength() * 2.0);
      double distance = getRoadLength() / nCars;
      double vNew = 10.0;
      for (int i = 0; i < nCars; i++) {
        int lane = (i % 2 == 0) ? 1 : 0;
        street.insertElementAt(new Car(getRoadLength() - (i + 1) * distance,
            vNew, lane, getIdmCar(), polite, PKW_LENGTH_M, colorCar, i), i);
        Car temp_car = (Car) street.elementAt(i);
        double rand = random.nextDouble() * 1.0;
        temp_car.tdelay = rand;
        rand = random.nextDouble() * 1.0;
        if (rand <= FRAC_TRUCK_INIT_CIRCLE) {
          temp_car.setModel(getIdmTruck());
          vNew = getIdmTruck().Veq(2.0 * distance);
          temp_car.setVelocity(vNew);
          temp_car.setLength(LKW_LENGTH_M);
          temp_car.setColor(colorTruck);
        } else {
          vNew = getIdmCar().Veq(2.0 * distance);
          temp_car.setVelocity(vNew);
        }
      }
      ((Moveable) street.elementAt(0)).setVelocity(0.5 * vNew);
    }
  }

  // ################# end constructor ##########################

  // <martin nov07>
  public void applyLocalPerturbation() {
    int imax = street.size();
    int i = 5 * imax / 6;
    Moveable veh = ((Moveable) (street.elementAt(i)));
    double vel = 0.1 * veh.velocity();
    veh.setVelocity(vel);
    veh.setColor(colorPerturb);
    System.out.println("MicroStreet.applyLocalPert.: new velocity=" + vel);
  }

  public double length() {
    return getRoadLength();
  }

  public void setLength(double roadLength) {
    this.setRoadLength(roadLength);
  }

  // make actual state available in form of vectors over all vehicles;
  // protected methods set public vectors to be used for graphical output

  protected Vector<Double> setPos() {
    Vector<Double> temp = new Vector<Double>(IMAXINIT);
    int imax = street.size();
    for (int i = 0; i < imax; i++) {
      double pos = ((Moveable) (street.elementAt(i))).position();
      temp.insertElementAt(new Double(pos), i);
    }
    return temp;
  }

  protected Vector<Double> setDistances() { // neglect gaps in front of
                                            // first/last veh
    // on either lane (i<=iFrontCarsBoth)
    Vector<Double> temp = new Vector<Double>(IMAXINIT);
    int imax = street.size();
    int iFirstLeft = firstIndexOnLane(0);
    int iFirstRight = firstIndexOnLane(1);
    int iFrontCarsBoth = (iFirstLeft > iFirstRight) ? iFirstLeft : iFirstRight;
    for (int i = 0; i <= iFrontCarsBoth + 1; i++) { // placeholder
      temp.insertElementAt(new Double(-1.), i);
    }
    for (int i = iFrontCarsBoth + 1; i < imax; i++) {
      int lane = ((Integer) lanes.elementAt(i)).intValue();
      int iFront = nextIndexOnLane(lane, i);
      double distance = ((Double) positions.elementAt(iFront)).doubleValue()
          - ((Double) positions.elementAt(i)).doubleValue();
      temp.insertElementAt(new Double(distance), i);
    }
    return temp;
  }

  protected Vector<Integer> setNr() {
    Vector<Integer> temp = new Vector<Integer>(IMAXINIT);
    int imax = street.size();
    for (int i = 0; i < imax; i++) {
      int nr = ((Moveable) (street.elementAt(i))).NR();
      temp.insertElementAt(new Integer(nr), i);
    }
    return temp;
  }

  protected Vector<Double> setVel() {
    Vector<Double> temp = new Vector<Double>(IMAXINIT);
    int imax = street.size();
    for (int i = 0; i < imax; i++) {
      double vel = ((Moveable) (street.elementAt(i))).velocity();
      temp.insertElementAt(new Double(vel), i);
    }
    return temp;
  }

  protected Vector<Integer> setLanes() {
    Vector<Integer> temp = new Vector<Integer>(IMAXINIT);
    int imax = street.size();
    for (int i = 0; i < imax; i++) {
      int lane = ((Moveable) (street.elementAt(i))).lane();
      temp.insertElementAt(new Integer(lane), i);
    }
    return temp;
  }

  protected Vector<Color> setColors() {
    Vector<Color> temp = new Vector<Color>(IMAXINIT);
    int imax = street.size();
    for (int i = 0; i < imax; i++) {
      Color c = ((Moveable) (street.elementAt(i))).color();
      temp.insertElementAt(c, i);
    }
    return temp;
  }

  protected Vector<Double> setLengths() {
    Vector<Double> temp = new Vector<Double>(IMAXINIT);
    int imax = street.size();
    for (int i = 0; i < imax; i++) {
      Double Len = new Double(((Moveable) (street.elementAt(i))).length());
      temp.insertElementAt(Len, i);
    }
    return temp;
  }

  public void update(double time, double dt, int choice_Szen, double density,
      double qIn, double perTr, double p_factor, double deltaB) {

    this.time = time;
    // used in SimCanvas.java

    old_pos = setPos(); // need old info for detectors and drawing
    old_lanes = setLanes();
    old_numbers = setNr();
    inconsiderate.set_p(p_factor);
    polite.set_db(DB_TRUCK);
    inconsiderate.set_db(DB_CAR);
    
    // choice_BC=0: per. BC; otherwise open BC
    int choice_BC = ((choice_Szen == 1) || (choice_Szen == 6)) ? 0 : 1;

    // Main action: accelerate, changeLanes, translate, sort!
    // At least one vehicle must be on each lane

    if (true) {
      // if ((firstIndexOnLane(0)>=0)&&(firstIndexOnLane(1)>=0)){
      insertBCCars(choice_BC); // virtual boundary cars
      accelerate(dt);
      changeLanes(dt);
      clearBCCars();
    }
    translate(dt, choice_Szen);
    sort();

    positions = setPos();
    velocities = setVel();
    numbers = setNr();
    lanes = setLanes();
    colors = setColors();
    lengths = setLengths();
    distances = setDistances();

    // source terms !! export of positions etc before such that
    // old_positions and positions etc must
    // always have same vehicle number!! -> extra var circleCarsRemoved

    ioFlow(dt, qIn, perTr, choice_BC); // needs positions etc
    if (choice_Szen == 1) {
      adaptToNewDensity(density, perTr);
    }

    circleCarsRemoved = ((choice_Geom == 0) && (street.size() < old_pos.size()));

  }

  // HIER truck => car implementieren!!

  protected void adaptToNewDensity(double density, double perTr) {
    int nCars_wished = (int) (density * getRoadLength() * 2.0);
    int nCars = positions.size();
    if (nCars_wished > nCars) {
      System.out.println("nCars_wished=" + nCars_wished + " nCars=" + nCars);
      insertOneVehicle(perTr);
    }
    if (nCars_wished < nCars) {
      System.out.println("nCars_wished=" + nCars_wished + " nCars=" + nCars);
      removeOneVehicle();
    }
  }

  /*
   * <Treiber aug06> Methode veraendert, so dass auch bei 0 Fz auf beiden Spuren
   * neue Fz eingefuehrt werden koennen (Ringstrasse) </Treiber>
   * translation: Method changed, so that even at 0 Fz on both tracks
   * new Fz can be introduced (ring road)
   */

  private void insertOneVehicle(double perTr) {

    // determine position and index of front veh

    int nveh = positions.size();
    final double mingap = 10.;
    double maxgap = 0.;
    int i_maxgap = 0;
    double pos_maxgap; // position of vehicle which maxgap in front
    int lane_maxgap; // lane of vehicle which maxgap in front

    int nleft = 0;
    int nright = 0;
    for (int i = 0; i < nveh; i++) {
      Moveable me = (Moveable) (street.elementAt(i));
      if (me.lane() == LEFT) {
        nleft++;
      } else {
        nright++;
      }
      System.out.println("i=" + i + " lane=" + me.lane() + " pos="
          + (int) me.position());

      double gap = ((Double) distances.elementAt(i)).doubleValue();
      if (gap > maxgap) {
        maxgap = gap;
        i_maxgap = i;
      }
    }

    if (nleft < 2) {
      System.out.println("nleft<2!!!");
      maxgap = getRoadLength();
      pos_maxgap = 0;
      lane_maxgap = LEFT;
      i_maxgap = nveh;
    } else if (nright < 2) {
      System.out.println("nright<2!!!");
      maxgap = getRoadLength();
      pos_maxgap = 0;
      lane_maxgap = RIGHT;
      i_maxgap = nveh;
    } else {
      pos_maxgap = ((Double) positions.elementAt(i_maxgap)).doubleValue();
      lane_maxgap = ((Integer) lanes.elementAt(i_maxgap)).intValue();
    }
    System.out.println("MicroStreet.insertOneVehicle: maxgap=" + (int) maxgap
        + " index=" + i_maxgap + " pos=" + pos_maxgap + " lane=" + lane_maxgap);

    // insert vehicle if sufficient gap

    if (maxgap > mingap) {
      double rand = random.nextDouble() * 1.0;
      int randInt = Math.abs(random.nextInt());
      MicroModel modelNew = (rand < perTr) ? getIdmTruck() : getIdmCar();
      LaneChange changemodelNew = (rand < perTr) ? polite : inconsiderate;
      double posNew = pos_maxgap + 0.5 * maxgap;
      double vNew = modelNew.Veq(0.5 * maxgap);
      double lNew = (rand < perTr) ? LKW_LENGTH_M : PKW_LENGTH_M;
      Color colorNew = (rand < perTr) ? colorTruck : colorCar;
      street.insertElementAt((new Car(posNew, vNew, lane_maxgap, modelNew,
          changemodelNew, lNew, colorNew, randInt)), i_maxgap);
    }

  }

  private void removeOneVehicle() {
    int indexToRemove = Math.abs(random.nextInt()) % (positions.size());
    street.removeElementAt(indexToRemove);
  }

  protected void insertBCCars(int choice_BC) {

    // virtual cars so that acceleration, lanechange etc
    // always defined (they need in general all next neighbours)

    int i_rd = firstIndexOnLane(1); // index right downstream vehicle
    int i_ld = firstIndexOnLane(0); // ...

    int i_ru = lastIndexOnLane(1);
    int i_lu = lastIndexOnLane(0);
    // System.out.println("MicroStreet.insertBCCars: i_rd="+i_rd+" i_ru="+i_ru);

    double upLeftPos = (i_lu > -1) ? ((Moveable) (street.elementAt(i_lu)))
        .position() : 0;
    double upRightPos = (i_ru > -1) ? ((Moveable) (street.elementAt(i_ru)))
        .position() : 0;
    double upLeftVel = (i_lu > -1) ? ((Moveable) (street.elementAt(i_lu)))
        .velocity() : 0;
    double upRightVel = (i_ru > -1) ? ((Moveable) (street.elementAt(i_ru)))
        .velocity() : 0;

    double downLeftPos = (i_ld > -1) ? ((Moveable) (street.elementAt(i_ld)))
        .position() : getRoadLength();
    double downRightPos = (i_rd > -1) ? ((Moveable) (street.elementAt(i_rd)))
        .position() : getRoadLength();
    double downLeftVel = (i_ld > -1) ? ((Moveable) (street.elementAt(i_ld)))
        .velocity() : getRoadLength();
    double downRightVel = (i_rd > -1) ? ((Moveable) (street.elementAt(i_rd)))
        .velocity() : getRoadLength();

    if (choice_BC == 0) { // periodic BC

      street.insertElementAt(new BCCar(upLeftPos + getRoadLength(), upLeftVel,
          0, getIdmCar(), PKW_LENGTH_M), 0);
      street.insertElementAt(new BCCar(upRightPos + getRoadLength(),
          upRightVel, 1, getIdmCar(), PKW_LENGTH_M), 0);
      int imax = street.size();
      street.insertElementAt(new BCCar(downLeftPos - getRoadLength(),
          downLeftVel, 0, getIdmCar(), PKW_LENGTH_M), imax);
      imax++;
      street.insertElementAt(new BCCar(downRightPos - getRoadLength(),
          downRightVel, 1, getIdmCar(), PKW_LENGTH_M), imax);
    }
    if (choice_BC == 1) { // open BC
      double dx = 200.; // distance of the boundary cars
      street.insertElementAt(new BCCar(downLeftPos + dx, downLeftVel, 0,
          getIdmCar(), PKW_LENGTH_M), 0);
      street.insertElementAt(new BCCar(downRightPos + dx, downRightVel, 1,
          getIdmCar(), PKW_LENGTH_M), 0);
      int imax = street.size();
      street.insertElementAt(new BCCar(upLeftPos - dx, upLeftVel, 0,
          getIdmCar(), PKW_LENGTH_M), imax);
      imax = street.size();
      street.insertElementAt(new BCCar(upRightPos - dx, upRightVel, 1,
          getIdmCar(), PKW_LENGTH_M), imax);
    }
  }

  protected void changeLanes(double dt) {
    int imax = street.size() - 3;

    for (int i = 2; i < imax; i++) {
      Moveable me = (Moveable) (street.elementAt(i));

      if (me.timeToChange(dt)) {
        int lane = me.lane();
        int newLane = ((lane == 0) ? 1 : 0);
        setNeighbours(i); // -> vR, hR, vL, hL
        Moveable fOld = (lane == 0) ? vL : vR; // front vehicle own lane
        Moveable fNew = (lane == 0) ? vR : vL; // front vehicle new lane
        Moveable bNew = (lane == 0) ? hR : hL; // back vehicle new lane

        // do actual change if incentive criterion fulfilled
        // setLane method of Moveable; setLanes M. of MicroStreet!

        if (me.change(fOld, fNew, bNew)) {
          ((Moveable) (street.elementAt(i))).setLane(newLane);
        }
      }
    }
  }

  protected void accelerate(double dt) {

    int imax = street.size() - 2;

    // floating car data

    fcd = 0.0;
    fcvd = 0.0;
    fcvel = 0.0;
    fcacc = 0.0;

    // Counting loop goes backwards to implement parallel update!

    for (int i = imax - 1; i >= 2; i--) {
      Moveable me = (Moveable) street.elementAt(i);
      int lane = me.lane();
      int act_nr = me.NR();
      int next_ind = nextIndexOnLane(lane, i);
      Moveable frontVeh = (Moveable) street.elementAt(next_ind);

      // if actual car = floating car, gather "detector data"

      if (act_nr == fcnr) {
        fcd = frontVeh.position() - me.position();
        fcvd = frontVeh.velocity() - me.velocity();
        fcvel = me.velocity();
        me.accelerate(dt, frontVeh);
        double vNew = me.velocity();
        fcacc = (vNew - fcvel) / dt;
      }

      // Otherwise, do just the acceleration

      else {
        me.accelerate(dt, frontVeh);
      }
    }
  }

  protected void clearBCCars() {

    street.removeElementAt(0);
    street.removeElementAt(0);
    int imax = street.size();
    imax--;
    street.removeElementAt(imax);
    imax--;
    street.removeElementAt(imax);
  }

  protected int translate(double dt, int choice_Szen) {
    // without sorting
    int imax = street.size();
    for (int i = 0; i < imax; i++) {
      Moveable me = (Moveable) street.elementAt(i);
      me.translate(dt);
    }
    return street.size();
  }

  protected void ioFlow(double dt, double qIn, double perTr, int choice_BC) {

    // periodic BC

    if (choice_BC == 0) {
      int imax = street.size() - 1;
      if (imax >= 0) { // at least one vehicle present
        Moveable temp_car = (Moveable) street.elementAt(0);
        while (temp_car.position() > getRoadLength()) {
          double pos = temp_car.position();

          // remove first vehicle
          street.removeElementAt(0);

          // and insert it at the end with position reduced by roadLength
          temp_car.setPosition(pos - getRoadLength());
          imax = street.size();
          street.insertElementAt(temp_car, imax);
          temp_car = (Moveable) street.elementAt(0);
        }
      }
    }

    // open BC

    if (choice_BC == 1) {
      int imax = street.size() - 1;
      final double spaceFree = 200.; // v0=ve(spaceFree)
      final double spaceMin = 27; // minimum headway for new vehicle
      // System.out.println("MicroStreet.ioFlow: Anfang: imax="+imax);

      // outflow:

      // just remove;
      // insertBCCars always guruantees for virtual front vehicles

      if (imax >= 0) {
        // System.out.println("MicroStreet.ioFlow: removing vehicle...");
        while ((imax >= 0) && // (imax>=0) first to prevent indexOutOf...!
            (((Moveable) (street.elementAt(0))).position() > getRoadLength())) {
          street.removeElementAt(0);
          ++numCarsOut;
          imax--;
        }
      }

      // inflow

      // System.out.println("MicroStreet.ioFlow: idmCar.v0="+((IDMCar)(idmCar)).v0);
      // System.out.println("MicroStreet.ioFlow: beginn inflow: imax="+imax);

      nin = nin + qIn * dt;

      if (nin > 1.0) { // new vehicle imposed by the inflow BC
        nin = nin - 1.0;
        int i_lu = lastIndexOnLane(0); // lu = "left upper"
        int i_ru = lastIndexOnLane(1);

        // at least 1 vehicle on either lane

        if ((i_lu >= 0) && (i_ru >= 0)) {
          imax = street.size() - 1;
          int laneLastVeh = ((Moveable) (street.elementAt(imax))).lane();
          int lane = (laneLastVeh == 0) ? 1 : 0; // insert on other lane
          int iPrev = lastIndexOnLane(lane); // index of previous vehicle
          double space = ((Moveable) (street.elementAt(iPrev))).position();

          // enough space for new vehicle to enter? (!red)

          if (!(red = (space < spaceMin))) {
            MicroModel carmodel = getIdmCar();
            MicroModel truckmodel = getIdmTruck();
            double rand = random.nextDouble() * 1.0;
            int randInt = Math.abs(random.nextInt());
            MicroModel modelNew = (rand < perTr) ? truckmodel : carmodel;
            LaneChange changemodelNew = (rand < perTr) ? polite : inconsiderate;
            double vNew = modelNew.Veq(space);
            if (false) {
              System.out.println("MicroStreet.ioFlow: "
                  + ((rand < perTr) ? "Truck" : "Car") + ", vNew=" + vNew);
            }
            double lNew = (rand < perTr) ? LKW_LENGTH_M : PKW_LENGTH_M;
            Color colorNew = (rand < perTr) ? colorTruck : colorCar;

            imax = street.size();
            street.insertElementAt(new Car(0.0, vNew, lane, modelNew,
                changemodelNew, lNew, colorNew, randInt), imax);
          }
        }

        // at least one lane without vehicles

        else {
          MicroModel carmodel = getIdmCar();
          MicroModel truckmodel = getIdmTruck();
          double rand = random.nextDouble() * 1.0;
          int randInt = Math.abs(random.nextInt());
          MicroModel modelNew = (rand < perTr) ? truckmodel : carmodel;
          double vNew = modelNew.Veq(spaceFree);
          double lNew = (rand < perTr) ? LKW_LENGTH_M : PKW_LENGTH_M;
          Color colorNew = (rand < perTr) ? colorTruck : colorCar;
          int lane = (i_lu < 0) ? 0 : 1;
          imax = street.size();
          System.out.println("street.size()=" + street.size());
          street.insertElementAt(new Car(0.0, vNew, lane, modelNew,
              inconsiderate, lNew, colorNew, randInt), imax);
        }
      }
    }
    // System.out.println("ioFlow end: imax="+(street.size()-1));

  }

  // Sort to decreasing values of pos using the bubblesort algorithm;
  // Pairwise swaps running over all vehicles;
  // repeat loop over vehicles until
  // sorted; typically, only 2 runs over the loop are needed
  // (one to sort; one to check)

  protected void sort() {

    boolean sorted = false;

    while (!sorted) {

      sorted = true;
      int imax = street.size();
      for (int i = 1; i < imax; i++) {
        double p_back = ((Moveable) street.elementAt(i)).position();
        double p_front = ((Moveable) street.elementAt(i - 1)).position();
        if (p_back > p_front) {
          sorted = false;
          Moveable temp = (Moveable) street.elementAt(i - 1);
          street.setElementAt((Moveable) street.elementAt(i), i - 1);
          street.setElementAt(temp, (i));
        }
      }
    }
  }

  // returns index of first (most downstream) vehicle on given lane;
  // if no vehicles on this lane; -1 is returned

  protected int firstIndexOnLane(int lane) {
    int nr_max = (street.size()) - 1;
    int i = 0;
    boolean carFound = false;
    if (nr_max >= 0) {
      while ((i <= nr_max) && (!carFound)) {
        if (((Moveable) street.elementAt(i)).lane() == lane) {
          // if (((((Moveable)street.elementAt(i)).lane())==lane)&&(flag==0)){
          carFound = true;
        }
        // System.out.println
        // ("first-loop:"+(new Integer(i).toString()));
        i++;
      }
    }
    return ((carFound) ? i - 1 : -1);
  }

  // returns index of most upstream vehicle on given lane

  protected int lastIndexOnLane(int lane) {

    int nr_max = (street.size()) - 1;
    int i = nr_max;
    boolean carFound = false;
    if (nr_max >= 0) {
      while ((i >= 0) && (!carFound)) {
        if (((Moveable) street.elementAt(i)).lane() == lane) {
          // if (((((Moveable)street.elementAt(i)).lane())==lane)&&(flag==0)){
          carFound = true;
        }
        i--;
      }
    }
    return ((carFound) ? i + 1 : -1);
  }

  // !! bounds not checked
  protected int nextIndexOnLane(int lane, int ind) {
    // textarea.setText("In nextIndexOnLane");
    int next_ind = ind - 1;
    while ((((Moveable) street.elementAt(next_ind)).lane()) != lane) {
      next_ind--;
    }
    return next_ind;
  }

  // !! bounds not checked
  protected int prevIndexOnLane(int lane, int ind) {
    // textarea.setText("In nextIndexOnLane");
    int next_ind = ind + 1;
    while ((((Moveable) street.elementAt(next_ind)).lane()) != lane) {
      next_ind++;
    }
    return next_ind;
  }

  // !! assumed that neighbours are existent; otherwise OutOfBoundsException

  protected void setNeighbours(int ind) {
    int vl = nextIndexOnLane(0, ind);
    int vr = nextIndexOnLane(1, ind);
    int hl = prevIndexOnLane(0, ind);
    int hr = prevIndexOnLane(1, ind);

    vL = (Moveable) street.elementAt(vl);
    vR = (Moveable) street.elementAt(vr);
    hL = (Moveable) street.elementAt(hl);
    hR = (Moveable) street.elementAt(hr);
  }

  public void setRoadLength(double roadLength) {
    this.roadLength = roadLength;
  }

  public double getRoadLength() {
    return roadLength;
  }

  public void setIdmTruck(MicroModel idmTruck) {
    this.idmTruck = idmTruck;
  }

  public MicroModel getIdmTruck() {
    return idmTruck;
  }

  public void setIdmCar(MicroModel idmCar) {
    this.idmCar = idmCar;
  }

  public MicroModel getIdmCar() {
    return idmCar;
  }
  
  /**
   * For a street with open boundary conditions, the number of vehicles that
   * have exited the simulation.
   * 
   * @return non-negative
   */
  public int getNumCarsOut() {
    return numCarsOut;
  }
}
