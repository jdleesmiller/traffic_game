package de.trafficsimulation.core;

/**
 * An assortment of constants influencing the global appearance and
 * functionality of the applet. In many cases, the applet can be adapted to
 * special needs (e.g. a different applet size, different time steps, different
 * truck percentage etc) by simply changing some numbers here and recompiling.
 */

public interface Constants { // MicroApplet3_0

  static final boolean CLOCKWISE = true; // car movement in ring geometry
  static final double TIMESTEP_S = 0.25; // simulation time step
  
  static final double FRAC_TRUCK_INIT = 0.1; // not for circle
  static final double FRAC_TRUCK_INIT_CIRCLE = 0.005;

  // #######################################################
  // Lane-change parameters
  // Safety: BSAVE,SMIN
  // Incentive: BIAS*, P_FACTOR, DB
  // #######################################################

  static final double MAIN_BSAVE = 12.;
  static final double MAIN_BSAVE_SELF = 12.;
  static final double MAIN_SMIN = 2.;

  static final double BIAS_RIGHT_CAR = 0.1; // right-lane bias
  static final double BIAS_RIGHT_TRUCK = 0.3;
  static final double P_FACTOR_CAR = 0.2; // politeness factor
  static final double P_FACTOR_TRUCK = 0.2;
  static final double DB_CAR = 0.3; // changing thresholds (m/s^2)
  static final double DB_TRUCK = 0.2;

  static final double V0_MIN_KMH = 1;
  static final double V0_MAX_KMH = 200;
  static final double V0_INIT_KMH = 120;

  static final double S0_MIN_M = 0;
  static final double S0_MAX_M = 6;
  static final double S0_INIT_M = 2;

  static final double S1_MIN_M = 0;
  static final double S1_MAX_M = 15;
  static final double S1_INIT_M = 5;

  static final double T_MIN_S = 0.3;
  static final double T_MAX_S = 3;
  // static final double T_INIT_S = 1.5;
  static final double T_INIT_S = 1.5;

  static final double A_MIN_MSII = 0.3;
  static final double A_MAX_MSII = 3;
  static final double A_INIT_CAR_MSII = 0.5;
  static final double A_INIT_TRUCK_MSII = 0.4;

  static final double B_MIN_MSII = 0.5;
  static final double B_MAX_MSII = 5.0;
  static final double B_INIT_MSII = 3.0;
  static final double MAX_BRAKING = 20.0;

  // traffic control variables

  static final int DENS_MIN_INVKM = 4; // (veh/km/ (2 lanes))
  static final int DENS_MAX_INVKM = 80; //
  static final int DENS_INIT_INVKM = 40; //

  static final int SPEED_MAX = 20; // (ms per frame)
  static final int SPEED_MIN = 400;
  static final int SPEED_INIT = 25; // time warp!
  // static final double LNSPEED_MAX = Math.log(1./20);
  // static final double LNSPEED_MIN = Math.log(1./500);

  static final int V0_LIMIT_MAX_KMH = 140; // (km/h) (140: free)
  static final int V0_LIMIT_MIN_KMH = 20;
  static final int V0_LIMIT_INIT_KMH = 80;
  static final int VMAX_TRUCK_KMH = 80;

  static final int Q_MAX = 4000; // (veh/h/ (2 lanes))
  static final int QRMP_MAX = 1800; // (veh/h/lane)
  static final int Q_INIT2 = 2800; // 3300
  static final int QRMP_INIT2 = 400;

  static final int POLITENESS_MIN = -1; // Politeness factor (0..P_MAX)
  static final int POLITENESS_MAX = 2; // Politeness factor (0..P_MAX)
  static final double PRMP_MIN = 0; // may be < 0
  static final double PRMP_MAX = 3;

  static final double DELTAB_MAX = 1; // Switching threshold (m/s^2)
  static final double DELTABRAMP_MIN = -2; // Switching threshold (m/s^2)
  static final double DELTABRAMP_MAX = 1; // Switching threshold (m/s^2)
  static final double DELTABRAMP_INIT = -2;

  // Geometric simulation constants

  static final double STRAIGHT_RDLEN_M = 400.;
  static final double RADIUS_M = 120.; // of circular road sections
  static final double L_RAMP_M = 100.; // length of on ramp
  static final double L_STRAIGHT_M = 200.; // of straight sections of U
  static final double REL_ROAD_MARGIN = 0.01; // relative space between roads
  static final double LANEWIDTH_M = 10.; // width of one lane
  static final double LINELENGTH_M = 4.; // white middle lines
  static final double VEH_WIDTH_M = 4.; // of both cars and trucks
  static final double PKW_LENGTH_M = 6.;
  static final double LKW_LENGTH_M = 10.;
}
