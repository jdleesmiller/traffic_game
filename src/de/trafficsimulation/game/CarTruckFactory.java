package de.trafficsimulation.game;

import java.awt.Color;
import java.util.Random;

import de.trafficsimulation.core.Car;
import de.trafficsimulation.core.Constants;
import de.trafficsimulation.core.IDM;
import de.trafficsimulation.core.LaneChange;
import de.trafficsimulation.core.MicroModel;

public class CarTruckFactory implements Constants {

  private double truckFraction = 0.0;

  private MicroModel idmCar = new IDM() {
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

  private MicroModel idmTruck = new IDM() {
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

  private LaneChange laneChangeCar = new LaneChange("car");

  private LaneChange laneChangeTruck = new LaneChange("truck");

  public Car createVehicle(Random random, double position, double initialGap,
      int lane) {
    double r = random.nextDouble();
    if (r < truckFraction) {
      return createTruck(random, position, initialGap, lane);
    } else {
      return createCar(random, position, initialGap, lane);
    }
  }

  public Car createCar(Random random, double position, double initialGap,
      int lane) {
    double initialSpeed = idmCar.Veq(initialGap);
    return new Car(position, initialSpeed, lane, idmCar, laneChangeCar,
        PKW_LENGTH_M, Color.RED);
  }
  
  public Car createTruck(Random random, double position, double initialGap,
      int lane) {
    double initialSpeed = idmTruck.Veq(initialGap);
    return new Car(position, initialSpeed, lane, idmTruck, laneChangeTruck,
        PKW_LENGTH_M, Color.BLACK);
  }

}
