package de.trafficsimulation.core;

/**
 * Basis class for the microscopic traffic model IDM (intelligent-driver model,
 * see <a href="http://xxx.uni-augsburg.de/abs/cond-mat/0002177"> M. Treiber, A.
 * Hennecke, and D. Helbing, Congested Traffic States in Empirical Observations
 * and Microscopic Simulations, Phys. Rev. E 62, 1805 (2000)].</a> <br>
 * <br>
 * The classes IDMCar, IDMTruck, etc are concrete realisations of this class for
 * trucks, cars, etc.
 */

public abstract class IDM implements MicroModel {

  public double v0;
  public double delta;
  public double a;
  public double b;
  public double s0;
  public double s1;
  public double T;
  public double sqrtab;
  private static final int ismax = 100; // ve(s)=ve(ismax) for s>ismax assumed
  private double[] veqTab = new double[ismax + 1]; // table in steps of ds=1m

  public IDM() {
    ;
  }

  public void set_v0(double v0) {
    this.v0 = v0;
  }

  public void set_T(double T) {
    this.T = T;
  }

  public void set_a(double a) {
    this.a = a;
  }

  public void set_b(double b) {
    this.b = b;
  }

  public void set_s0(double s0) {
    this.s0 = s0;
  }

  public void set_s1(double s1) {
    this.s1 = s1;
  }

  public void set_params(IDM idm) {
    this.set_v0(idm.v0);
    this.set_a(idm.a);
    this.set_b(idm.b);
    this.set_T(idm.T);
    this.set_s0(idm.s0);
    this.set_s1(idm.s1);
  }

  // calculate table of equilibrium velocity (s) with relaxation method;
  // veqTab[i] = equilibrium velocity for s=i*ds with ds=1 m, i=0..100

  public void initialize() {
    final double dt = 0.5; // relaxation with timestep=0.5 s
    final double kmax = 20; // number of iterations in rlaxation
    veqTab[0] = 0.0;
    for (int is = 1; is <= ismax; is++) { // table in steps of ds=1 (m)

      double Ve = veqTab[is - 1];
      // if(is>=ismax-3){System.out.println("is="+is+" Ve="+Ve);}

      for (int k = 0; k < kmax; k++) {
        double s_star = s0 + s1 * Math.sqrt(Ve / v0) + Ve * T;
        double acc = a
            * (1 - Math.pow((Ve / v0), delta) - (s_star * s_star) / (is * is));
        Ve = Ve + acc * dt;
        if (Ve < 0.0) {
          Ve = 0.0;
        }
      }
      veqTab[is] = Ve;
    }
    //System.out.println("IDM.initialize():" + "  veqTab[0]=" + veqTab[0]
    //    + ", veqTab[" + ismax + "]=" + veqTab[ismax]);
  }

  // function for equilibrium velocity using above table; ve(s>ismax)=ve(ismax)

  public double Veq(double dx) {
    int is = (int) dx;
    double V = 0.0;
    if (is < ismax) {
      double rest = dx - ((double) is);
      V = (1 - rest) * veqTab[is] + rest * veqTab[is + 1];
    }
    if (is >= ismax) {
      V = veqTab[ismax];
    }
    if (is <= 0) {
      V = 0.0;
    }
    return V;
  }

  public double calcAcc(Moveable bwd, Moveable vwd) {
    double delta_v = bwd.velocity() - vwd.velocity();
    double s = vwd.position() - bwd.position() - bwd.length(); // pos: END of
                                                               // vehicles!
    double vel = bwd.velocity();
    double s_star_raw = s0 + s1 * Math.sqrt(vel / v0) + vel * T
        + (vel * delta_v) / (2 * sqrtab);
    double s_star = (s_star_raw > s0) ? s_star_raw : s0;
    double acc = a
        * (1 - Math.pow((vel / v0), delta) - (s_star * s_star) / (s * s));
    if (acc < -Constants.MAX_BRAKING) {
      acc = -Constants.MAX_BRAKING;
    }
    return acc;
  }

}
