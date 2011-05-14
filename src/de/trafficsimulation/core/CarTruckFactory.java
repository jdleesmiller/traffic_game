package de.trafficsimulation.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;


public class CarTruckFactory implements Constants {

  private double truckProbability = 0.0;
  
  private final ArrayList<Color> carColors;
  
  private final ArrayList<Color> truckColors;

  private IDM carIDM = new IDM() {
    {
      v0 = V0_INIT_KMH / 3.6;
      delta = 4.0;
      a = A_INIT_CAR_MSII; // 1
      b = B_INIT_MSII; // 1.0
      s0 = S0_INIT_M;
      T = T_INIT_S; // 1.5
      sqrtab = Math.sqrt(a * b);
      initialize();
    }
  };

  private IDM truckIDM = new IDM() {
    {
      v0 = 22.2;
      delta = 4.0;
      a = A_INIT_TRUCK_MSII;
      b = 4.0;
      s0 = 2.0;
      T = 1.7;
      sqrtab = Math.sqrt(a * b);
      initialize();
    }
  };

  private LaneChange inconsiderateLaneChange = new LaneChange(
      P_FACTOR_CAR, DB_CAR, MAIN_SMIN, MAIN_BSAVE, BIAS_RIGHT_CAR);
  
  private LaneChange politeLaneChange = new LaneChange(
      P_FACTOR_TRUCK, DB_TRUCK, MAIN_SMIN, MAIN_BSAVE, BIAS_RIGHT_TRUCK);
  
  public CarTruckFactory() {
    carColors = new ArrayList<Color>();
    carColors.add(Color.WHITE);
    carColors.add(Color.GREEN);
    carColors.add(Color.MAGENTA);
    carColors.add(Color.PINK);
    
    truckColors = new ArrayList<Color>();
    truckColors.add(Color.BLUE);
    truckColors.add(Color.BLACK);
  }
  
  /**
   * Create a truck with probability truckProbability or a car with probability
   * 1 - truckProbability.
   * 
   * @param random
   * @param position
   * @param initialGap
   * @param lane
   * @return
   */
  public Car createVehicle(Random random, double position, double initialGap,
      int lane) {
    double r = random.nextDouble();
    if (r < truckProbability) {
      return createTruck(random, position, initialGap, lane);
    } else {
      return createCar(random, position, initialGap, lane);
    }
  }

  /**
   * Create a new car.
   * 
   * @param position
   * @param initialGap used to set initial speed
   * @param lane
   * @return
   */
  public Car createCar(Random random, double position, double initialGap, int lane) {
    double initialSpeed = carIDM.Veq(initialGap);
    Color color = carColors.get(random.nextInt(carColors.size()));
    return new Car(position, initialSpeed, lane, carIDM,
        inconsiderateLaneChange, PKW_LENGTH_M, color);
  }
  
  /**
   * Create a new truck.
   * 
   * @param position
   * @param initialGap used to set initial speed
   * @param lane
   * @return
   */
  public Car createTruck(Random random, double position, double initialGap, int lane) {
    double initialSpeed = truckIDM.Veq(initialGap);
    Color color = truckColors.get(random.nextInt(truckColors.size()));
    return new Car(position, initialSpeed, lane, truckIDM,
        politeLaneChange, LKW_LENGTH_M, color);
  }

  public double getTruckProbability() {
    return truckProbability;
  }

  public void setTruckProbability(double truckProbability) {
    this.truckProbability = truckProbability;
  }

  public IDM getCarIDM() {
    return carIDM;
  }

  public IDM getTruckIDM() {
    return truckIDM;
  }

  public LaneChange getInconsiderateLaneChange() {
    return inconsiderateLaneChange;
  }

  public LaneChange getPoliteLaneChange() {
    return politeLaneChange;
  }
}
