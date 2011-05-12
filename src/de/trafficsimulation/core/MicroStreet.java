package de.trafficsimulation.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
  protected List<Moveable> street = new ArrayList<Moveable>(IMAXINIT);

  // vector of data of Moveables for output (only model is missing)

  public List<Double> positions = new ArrayList<Double>(IMAXINIT);
  public List<Integer> lanes = new ArrayList<Integer>(IMAXINIT);

  // additional vectors for output

  private List<Double> distances = new ArrayList<Double>(IMAXINIT);

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
  private double roadLength; // length depends on choice_Szen
  protected double uppos; // location where flow-conserving bottleneck begins

  // time and simulation control

  //protected double time;

  // Source: nin = integral of inflow mod 1
  protected double nin = 0.0;
  
  // see getNumCarsOut
  private int numCarsOut;

  public MicroStreet(double length, double density, double p_factor,
      double deltaB, int choice_Szen) {
    //time = 0;
    setRoadLength(length);
    uppos = 0.5 * getRoadLength();
    this.choice_Szen = choice_Szen;
    inconsiderate.set_p(p_factor);
    inconsiderate.set_db(deltaB);
    int choice_Geom = ((choice_Szen == 1) || (choice_Szen == 6)) ? 0 : 1;
    double mult = ((choice_Geom == 0) && CLOCKWISE) ? (-1) : 1;
    double bias_right_truck = BIAS_RIGHT_TRUCK;
    double bias_right_car = BIAS_RIGHT_CAR;
    polite.set_biasRight(mult * bias_right_truck);
    inconsiderate.set_biasRight(mult * bias_right_car);

    System.out.println("MicroStreet(args) cstr: roadLength=" + getRoadLength());

    // ### closed circle; free traffic or stop&go, depending on density

    if (choice_Szen == 1) {
      int nCars = (int) (density * getRoadLength() * 2.0);
      double distance = getRoadLength() / nCars;
      double vNew = 10.0;
      for (int i = 0; i < nCars; i++) {
        int lane = (i % 2 == 0) ? 1 : 0;
        street.add(i, new Car(getRoadLength() - (i + 1) * distance,
            vNew, lane, getIdmCar(), polite, PKW_LENGTH_M, colorCar, i));
        Car temp_car = (Car) street.get(i);
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
      street.get(0).setVelocity(0.5 * vNew);
    }
  }

  // ################# end constructor ##########################

  // <martin nov07>
  public void applyLocalPerturbation() {
    int imax = street.size();
    int i = 5 * imax / 6;
    Moveable veh = street.get(i);
    double vel = 0.1 * veh.velocity();
    veh.setVelocity(vel);
    veh.setColor(colorPerturb);
    System.out.println("MicroStreet.applyLocalPert.: new velocity=" + vel);
  }

  // make actual state available in form of vectors over all vehicles;
  // protected methods set public vectors to be used for graphical output

  protected List<Double> setPos() {
    List<Double> temp = new ArrayList<Double>(street.size());
    for (int i = 0; i < street.size(); i++) {
      temp.add(street.get(i).position());
    }
    return temp;
  }

  protected List<Double> setDistances() { // neglect gaps in front of
                                            // first/last veh
    // on either lane (i<=iFrontCarsBoth)
    List<Double> temp = new ArrayList<Double>(street.size());
    int iFirstLeft = firstIndexOnLane(0);
    int iFirstRight = firstIndexOnLane(1);
    int iFrontCarsBoth = (iFirstLeft > iFirstRight) ? iFirstLeft : iFirstRight;
    for (int i = 0; i <= iFrontCarsBoth + 1; i++) { // placeholder
      temp.add(i, new Double(-1.));
    }
    for (int i = iFrontCarsBoth + 1; i < street.size(); i++) {
      int lane = lanes.get(i);
      int iFront = nextIndexOnLane(lane, i);
      double distance = positions.get(iFront) - positions.get(i);
      temp.add(i, distance);
    }
    return temp;
  }

  protected List<Integer> setLanes() {
    List<Integer> temp = new ArrayList<Integer>(street.size());
    for (int i = 0; i < street.size(); i++) {
      temp.add(street.get(i).lane());
    }
    return temp;
  }

  public void update(double dt, double density, double qIn, double perTr,
      double p_factor, double deltaB) {

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
    translate(dt);
    sort();

    positions = setPos();
    lanes = setLanes();
    distances = setDistances();

    ioFlow(dt, qIn, perTr, choice_BC); // needs positions etc
    if (choice_Szen == 1) {
      adaptToNewDensity(density, perTr);
    }
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
      Moveable me = street.get(i);
      if (me.lane() == LEFT) {
        nleft++;
      } else {
        nright++;
      }
      System.out.println("i=" + i + " lane=" + me.lane() + " pos="
          + (int) me.position());

      double gap = distances.get(i);
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
      pos_maxgap = positions.get(i_maxgap);
      lane_maxgap = lanes.get(i_maxgap);
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
      street.add(i_maxgap, new Car(posNew, vNew, lane_maxgap, modelNew,
          changemodelNew, lNew, colorNew, randInt));
    }

  }

  private void removeOneVehicle() {
    int indexToRemove = Math.abs(random.nextInt()) % (positions.size());
    street.remove(indexToRemove);
  }

  protected void insertBCCars(int choice_BC) {

    // virtual cars so that acceleration, lanechange etc
    // always defined (they need in general all next neighbours)

    int i_rd = firstIndexOnLane(1); // index right downstream vehicle
    int i_ld = firstIndexOnLane(0); // ...

    int i_ru = lastIndexOnLane(1);
    int i_lu = lastIndexOnLane(0);
    // System.out.println("MicroStreet.insertBCCars: i_rd="+i_rd+" i_ru="+i_ru);

    double upLeftPos = (i_lu > -1) ? street.get(i_lu).position() : 0;
    double upRightPos = (i_ru > -1) ? street.get(i_ru).position() : 0;
    double upLeftVel = (i_lu > -1) ? street.get(i_lu).velocity() : 0;
    double upRightVel = (i_ru > -1) ? street.get(i_ru).velocity() : 0;

    double downLeftPos = (i_ld > -1) ? street.get(i_ld).position() : getRoadLength();
    double downRightPos = (i_rd > -1) ? street.get(i_rd).position() : getRoadLength();
    double downLeftVel = (i_ld > -1) ? street.get(i_ld).velocity() : getRoadLength();
    double downRightVel = (i_rd > -1) ? street.get(i_rd).velocity() : getRoadLength();

    if (choice_BC == 0) { // periodic BC

      street.add(0, new BCCar(upLeftPos + getRoadLength(), upLeftVel,
          0, getIdmCar(), PKW_LENGTH_M));
      street.add(0, new BCCar(upRightPos + getRoadLength(),
          upRightVel, 1, getIdmCar(), PKW_LENGTH_M));
      int imax = street.size();
      street.add(imax, new BCCar(downLeftPos - getRoadLength(),
          downLeftVel, 0, getIdmCar(), PKW_LENGTH_M));
      imax++;
      street.add(imax, new BCCar(downRightPos - getRoadLength(),
          downRightVel, 1, getIdmCar(), PKW_LENGTH_M));
    }
    if (choice_BC == 1) { // open BC
      double dx = 200.; // distance of the boundary cars
      street.add(0, new BCCar(downLeftPos + dx, downLeftVel, 0,
          getIdmCar(), PKW_LENGTH_M));
      street.add(0, new BCCar(downRightPos + dx, downRightVel, 1,
          getIdmCar(), PKW_LENGTH_M));
      int imax = street.size();
      street.add(imax, new BCCar(upLeftPos - dx, upLeftVel, 0,
          getIdmCar(), PKW_LENGTH_M));
      imax = street.size();
      street.add(imax, new BCCar(upRightPos - dx, upRightVel, 1,
          getIdmCar(), PKW_LENGTH_M));
    }
  }

  protected void changeLanes(double dt) {
    int imax = street.size() - 3;

    for (int i = 2; i < imax; i++) {
      Moveable me = street.get(i);

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
          street.get(i).setLane(newLane);
        }
      }
    }
  }

  protected void accelerate(double dt) {

    int imax = street.size() - 2;

    // Counting loop goes backwards to implement parallel update!

    for (int i = imax - 1; i >= 2; i--) {
      Moveable me = street.get(i);
      int lane = me.lane();
      int next_ind = nextIndexOnLane(lane, i);
      Moveable frontVeh = street.get(next_ind);
      me.accelerate(dt, frontVeh);
    }
  }

  protected void clearBCCars() {
    street.remove(0);
    street.remove(0);
    int imax = street.size();
    imax--;
    street.remove(imax);
    imax--;
    street.remove(imax);
  }

  protected int translate(double dt) {
    // without sorting
    int imax = street.size();
    for (int i = 0; i < imax; i++) {
      Moveable me = street.get(i);
      me.translate(dt);
    }
    return street.size();
  }

  protected void ioFlow(double dt, double qIn, double perTr, int choice_BC) {

    // periodic BC

    if (choice_BC == 0) {
      int imax = street.size() - 1;
      if (imax >= 0) { // at least one vehicle present
        Moveable temp_car = street.get(0);
        while (temp_car.position() > getRoadLength()) {
          double pos = temp_car.position();

          // remove first vehicle
          street.remove(0);

          // and insert it at the end with position reduced by roadLength
          temp_car.setPosition(pos - getRoadLength());
          imax = street.size();
          street.add(imax, temp_car);
          temp_car = street.get(0);
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
            (street.get(0).position() > getRoadLength())) {
          street.remove(0);
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
          int laneLastVeh = street.get(imax).lane();
          int lane = (laneLastVeh == 0) ? 1 : 0; // insert on other lane
          int iPrev = lastIndexOnLane(lane); // index of previous vehicle
          double space = street.get(iPrev).position();

          // enough space for new vehicle to enter? (!red)

          if (!(space < spaceMin)) {
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
            street.add(imax, new Car(0.0, vNew, lane, modelNew,
                changemodelNew, lNew, colorNew, randInt));
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
          street.add(imax, new Car(0.0, vNew, lane, modelNew,
              inconsiderate, lNew, colorNew, randInt));
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
        double p_back = street.get(i).position();
        double p_front = street.get(i - 1).position();
        if (p_back > p_front) {
          sorted = false;
          Moveable temp = street.get(i - 1);
          street.set(i - 1,street.get(i));
          street.set(i, temp);
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
        if (street.get(i).lane() == lane) {
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
        if (street.get(i).lane() == lane) {
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
    while (street.get(next_ind).lane() != lane) {
      next_ind--;
    }
    return next_ind;
  }

  // !! bounds not checked
  protected int prevIndexOnLane(int lane, int ind) {
    // textarea.setText("In nextIndexOnLane");
    int next_ind = ind + 1;
    while (street.get(next_ind).lane() != lane) {
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

    vL = street.get(vl);
    vR = street.get(vr);
    hL = street.get(hl);
    hR = street.get(hr);
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
  
  public List<Moveable> getStreet() {
    return street;
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
