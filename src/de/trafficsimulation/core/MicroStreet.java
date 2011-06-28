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
  
  public static final Color colorCar = new Color(210, 0, 0);
  public static final Color colorTruck = new Color(40, 40, 60);

  protected List<Moveable> street = new ArrayList<Moveable>();

  // neighbours
  protected Moveable vL, vR, hL, hR;

  // can be modified interactively

  protected final Random random;
  protected final CarTruckFactory vehicleFactory;
  protected final int choice_Szen;
  private final double roadLength; // length depends on choice_Szen

  // Source: nin = integral of inflow mod 1
  protected double nin = 0.0;
  
  // see getNumCarsOut
  private int numCarsOut;

  public MicroStreet(Random random, CarTruckFactory vehicleFactory,
      double length, double density, int choice_Szen) {
    
    this.random = random;
    this.roadLength = length;
    this.vehicleFactory = vehicleFactory;
    this.choice_Szen = choice_Szen;

    //System.out.println("MicroStreet(args) cstr: roadLength=" + getRoadLength());

    // ### closed circle; free traffic or stop&go, depending on density

    if (choice_Szen == 1) {
      int nCars = (int) (density * getRoadLength() * 2.0);
      double distance = getRoadLength() / nCars;
      double initialGap = 2 * distance;
      for (int i = 0; i < nCars; i++) {
        int lane = (i % 2 == 0) ? 1 : 0;
        Car newCar = vehicleFactory.createVehicle(random,
            getRoadLength() - (i + 1) * distance, initialGap, lane);
        newCar.tdelay = random.nextDouble();
        // all cars and trucks are polite in this scenario
        newCar.setLaneChange(vehicleFactory.getPoliteLaneChange());
        street.add(i, newCar);
      }
      street.get(0).setVelocity(0.5 * street.get(0).velocity());
    }
  }

  // ################# end constructor ##########################

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
      int lane = street.get(i).lane();
      int iFront = nextIndexOnLane(lane, i);
      double distance = street.get(iFront).position() - street.get(i).position();
      temp.add(i, distance);
    }
    return temp;
  }

  public void update(double dt, double density, double qIn) {
    
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

    if (choice_Szen == 1) {
      adaptToNewDensity(density);
    }
    ioFlow(dt, qIn, choice_BC);
  }

  // HIER truck => car implementieren!!

  protected void adaptToNewDensity(double density) {
    int nCars_wished = (int) (density * getRoadLength() * 2.0);
    int nCars = street.size();
    if (nCars_wished > nCars) {
      //System.out.println("nCars_wished=" + nCars_wished + " nCars=" + nCars);
      insertOneVehicle();
    }
    if (nCars_wished < nCars) {
      //System.out.println("nCars_wished=" + nCars_wished + " nCars=" + nCars);
      removeOneVehicle();
    }
  }

  /*
   * <Treiber aug06> Methode veraendert, so dass auch bei 0 Fz auf beiden Spuren
   * neue Fz eingefuehrt werden koennen (Ringstrasse) </Treiber>
   * translation: Method changed, so that even at 0 Fz on both tracks
   * new Fz can be introduced (ring road)
   */

  private void insertOneVehicle() {
    // determine position and index of front veh
    int nveh = street.size();
    final double mingap = 10.;
    double maxgap = 0.;
    int i_maxgap = 0;
    double pos_maxgap; // position of vehicle which maxgap in front
    int lane_maxgap; // lane of vehicle which maxgap in front
    
    // compute distances between vehicles
    List<Double> distances = setDistances();

    int nleft = 0;
    int nright = 0;
    for (int i = 0; i < nveh; i++) {
      Moveable me = street.get(i);
      if (me.lane() == LEFT) {
        nleft++;
      } else {
        nright++;
      }
      //System.out.println("i=" + i + " lane=" + me.lane() + " pos="
      //    + (int) me.position());

      double gap = distances.get(i);
      if (gap > maxgap) {
        maxgap = gap;
        i_maxgap = i;
      }
    }

    if (nleft < 2) {
      //System.out.println("nleft<2!!!");
      maxgap = getRoadLength();
      pos_maxgap = 0;
      lane_maxgap = LEFT;
      i_maxgap = nveh;
    } else if (nright < 2) {
      //System.out.println("nright<2!!!");
      maxgap = getRoadLength();
      pos_maxgap = 0;
      lane_maxgap = RIGHT;
      i_maxgap = nveh;
    } else {
      pos_maxgap = street.get(i_maxgap).position();
      lane_maxgap = street.get(i_maxgap).lane();
    }
    //System.out.println("MicroStreet.insertOneVehicle: maxgap=" + (int) maxgap
    //    + " index=" + i_maxgap + " pos=" + pos_maxgap + " lane=" + lane_maxgap);

    // insert vehicle if sufficient gap

    if (maxgap > mingap) {
      double posNew = pos_maxgap + 0.5 * maxgap;
      street.add(i_maxgap, vehicleFactory.createVehicle(random,
          posNew, 0.5*maxgap, lane_maxgap));
    }

  }

  private void removeOneVehicle() {
    int indexToRemove = Math.abs(random.nextInt()) % (street.size());
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

    MicroModel carIDM = vehicleFactory.getCarIDM();
    if (choice_BC == 0) { // periodic BC
      street.add(0, new BCCar(upLeftPos + getRoadLength(), upLeftVel,
          0, carIDM, PKW_LENGTH_M));
      street.add(0, new BCCar(upRightPos + getRoadLength(),
          upRightVel, 1, carIDM, PKW_LENGTH_M));
      int imax = street.size();
      street.add(imax, new BCCar(downLeftPos - getRoadLength(),
          downLeftVel, 0, carIDM, PKW_LENGTH_M));
      imax++;
      street.add(imax, new BCCar(downRightPos - getRoadLength(),
          downRightVel, 1, carIDM, PKW_LENGTH_M));
    }
    if (choice_BC == 1) { // open BC
      double dx = 200.; // distance of the boundary cars
      street.add(0, new BCCar(downLeftPos + dx, downLeftVel, 0,
          carIDM, PKW_LENGTH_M));
      street.add(0, new BCCar(downRightPos + dx, downRightVel, 1,
          carIDM, PKW_LENGTH_M));
      int imax = street.size();
      street.add(imax, new BCCar(upLeftPos - dx, upLeftVel, 0,
          carIDM, PKW_LENGTH_M));
      imax = street.size();
      street.add(imax, new BCCar(upRightPos - dx, upRightVel, 1,
          carIDM, PKW_LENGTH_M));
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

  protected void ioFlow(double dt, double qIn, int choice_BC) {

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

          // enough space for new vehicle to enter?
          if (space >= spaceMin) {
            street.add(vehicleFactory.createVehicle(random, 0.0, space, lane));
          }
        }

        // at least one lane without vehicles

        else {
          int lane = (i_lu < 0) ? 0 : 1;
          street.add(
              vehicleFactory.createVehicle(random, 0.0, spaceFree, lane));
          street.get(street.size() - 1).setLaneChange(
              vehicleFactory.getInconsiderateLaneChange());
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

  public double getRoadLength() {
    return roadLength;
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
  
  /**
   * Reset numCarsOut to zero; this is useful if you want to discard some
   * time at the start of a simulation.
   */
  public void resetNumCarsOut() {
    numCarsOut = 0;
  }
  
  public CarTruckFactory getVehicleFactory() {
    return vehicleFactory;
  }
  
  /**
   * The lowest speed of any car in the given lane.
   * 
   * @return in meters per second; infinity if there are no cars in lane
   */
  public double getMinSpeedInLane(int lane) {
    double min = Double.POSITIVE_INFINITY;
    for (Moveable car : street) {
      if (car.velocity() < min && car.lane() == lane) {
        min = car.velocity();
      }
    }
    return min;
  }
  
  /**
   * The lowest speed of any car on the street.
   * 
   * @return in meters per second
   */
  public double getMinSpeed() {
    double min = Double.POSITIVE_INFINITY;
    for (Moveable car : street) {
      if (car.velocity() < min) {
        min = car.velocity();
      }
    }
    return min;
  }
}
