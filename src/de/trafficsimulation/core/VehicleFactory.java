package de.trafficsimulation.core;

import java.util.Random;

public interface VehicleFactory {
  
  /*
  public static Car createVehicle(double x, double v, int lane,
      MicroModel model, LaneChange lanechange, double length, Color color) {
    return new Car(x, v, lane, model, lanechange, length, color);
  }
  */

  public Car createVehicle(Random random, double position, double initialGap, int lane);
  
  /*
    if (choice_Szen == 1) {
      int nCars = (int) (density * getRoadLength() * 2.0);
      double distance = getRoadLength() / nCars;
      double vNew = 10.0;
      for (int i = 0; i < nCars; i++) {
        int lane = (i % 2 == 0) ? 1 : 0;
        street.add(i, VehicleFactory.createVehicle(
            getRoadLength() - (i + 1) * distance, vNew, lane,
            getIdmCar(), polite, PKW_LENGTH_M, colorCar, i));
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
   * 
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
    street.add(imax, VehicleFactory.createVehicle(0.0, vNew, lane, modelNew, changemodelNew,
    lNew, colorNew, randInt));
    
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
            street.add(imax, VehicleFactory.createVehicle(0.0, vNew, lane, modelNew, changemodelNew,
                lNew, colorNew, randInt));
                
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
          street.add(imax, VehicleFactory.createVehicle(0.0, vNew, lane, modelNew, inconsiderate,
              lNew, colorNew, randInt));
    
   */
}
