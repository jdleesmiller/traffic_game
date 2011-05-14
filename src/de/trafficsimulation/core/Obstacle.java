package de.trafficsimulation.core;

import java.awt.Color;

public class Obstacle implements Moveable {

  private double vel = 0.0;
  private double pos;
  private int lane;
  private double length;
  private Color color = Color.white;

  // model necessary since obstacle=type "Moveable" and lane change checks
  // acceleration! influences 3 following locations labeld with xxx

  public MicroModel model;

  public MicroModel model() {
    return model;
  }

  public Obstacle(double x, int lane, double length) {
    pos = x;
    this.lane = lane;
    this.length = length;
    this.model = new MicroModel() {
      
      @Override
      public double calcAcc(Moveable bwd, Moveable vwd) {
        return 0;
      }
      
      @Override
      public double Veq(double dx) {
        return 0;
      }
    };
  }

  public void setPosition(double x) {
    pos = x;
  }

  public void setVelocity(double v) {
    vel = v;
  } // includes moving obstacle

  public void setLane(int lane) {
    this.lane = lane;
  }

  public void setLength(double length) {
    this.length = length;
  }

  public void setColor(Color color) {
  }

  public void setModel(MicroModel model) {
  }

  public void setLaneChange(LaneChange lanechange) {
  }

  public double position() {
    return pos;
  }

  public double velocity() {
    return vel;
  }

  public int lane() {
    return lane;
  }

  public double length() {
    return length;
  }

  public Color color() {
    return color;
  }

  public boolean timeToChange(double dt) {
    return false;
  }

  public boolean change(Moveable fOld, Moveable fNew, Moveable bNew) {
    return false;
  }

  public double dAcc(Moveable vwd, Moveable bwd) {
    return 0.0;
  }

  public void translate(double dt) {
  }

  public void accelerate(double dt, Moveable vwd) {
  }

  public double acceleration(Moveable vwd) {
    return 0.0;
  }
}
