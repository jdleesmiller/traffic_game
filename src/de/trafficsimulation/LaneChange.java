package de.trafficsimulation;

// MicroApplet2_0



/**
Implementation of the lane-changing model
MOBIL ("Minimizing Overall Brakings Induced by Lane-changes"), see
<a href="http://141.30.51.183/~treiber/publications/MOBIL.pdf">
M. Treiber and D. Helbing, Realistische Mikrosimulation von Straﬂenverkehr mit einem einfachen Modell </a>,
16. Symposium "Simulationstechnik ASIM 2002" Rostock, 10.09 -13.09.2002, edited by Djamshid Tavangarian and Rolf Gr\"utzner pp. 514--520.

*/

public class LaneChange implements Constants{

    // !! p and db and bsave overwritten by set_p, set_db!!

    private double p=P_FACTOR_CAR; // politeness factor
    private double db=DB_CAR;      // changing threshold
    private double gapMin=MAIN_SMIN;       // minimum safe (net) distance
    private double bsave=MAIN_BSAVE;     // maximum safe braking deceleration for other vehicles
    private double bsaveSelf=MAIN_BSAVE_SELF;     // maximum safe braking deceleration for subject vehicle

    // asymmetric European parts

    //private double biasRight = BIAS_RIGHT_CAR; //bias (m/s^2) to drive right ln
    private double biasRight = 0; // overwritten

    final int LEFT=0;             // left lane=0
    final int RIGHT=1;             


    public LaneChange (String lctype){
        if( lctype.equals("truck")){
           System.out.println("in Cstr LaneChange(''truck''): "
                            +"db=" +db+" bsave="+bsave);
           p=P_FACTOR_TRUCK;  //!! overwritten in sliders
           db=DB_TRUCK;  //!! overwritten in sliders
           gapMin=MAIN_SMIN;
           bsave=MAIN_BSAVE;
           biasRight=BIAS_RIGHT_TRUCK;
	}

        else{         // default
          System.out.println("in Cstr of LaneChange(''car''): "
                            +"db=" +db+" bsave="+bsave);
	}
    }


    public LaneChange (double p, double db, 
                       double gapMin, double bsave, double biasRight){
        this(p,db);
        assert bsave <= MAX_BRAKING;
        this.gapMin=gapMin;
        this.bsave=bsave;
        this.biasRight=biasRight;
        System.out.println("Cstr of LaneChange(5 args): db="+db+" bsave="+bsave);
    }

    public LaneChange (double p, double db){
        assert bsaveSelf <= MAX_BRAKING;
        this.p=p;
        this.db=db;
        System.out.println("Cstr of LaneChange(5 args): db="+db+" bsave="+bsave);
    }

    public void set_p(double p){this.p=p;
    //System.out.println("LaneChange.set_p: p="+p);
    }
    public void set_db(double db){this.db=db;
    //System.out.println("LaneChange.set_db: db="+db);
    }
    public double get_p(){return(p);}
    public double get_db(){return(db);}
    public void set_gapMin(double gapMin){this.gapMin=gapMin;
    //System.out.println("LaneChange.set_gapMin: gapMin="+gapMin);
    }

    public void set_bsave(double bsave){
        assert bsave <= MAX_BRAKING;
        this.bsave=bsave;
      //System.out.println("LaneChange.set_bsave: bsave="+bsave);
    }

    public void set_biasRight(double bias){this.biasRight=bias;
    //System.out.println("LaneChange.set_biasRight: biasRight="+biasRight);
    }


    public boolean changeOK(Moveable me, Moveable fOld, 
        Moveable fNew, Moveable bNew) {

        // fOld: front veh on old lane; 
        // fNew,bNew: front,back vehicle on new lane; 

        double gapFront=fNew.position() - me.position() - me.length();
        if (gapFront <= gapMin) {
            return false;
        }
        double gapBack=me.position() - bNew.position() - bNew.length();
        if (gapBack <= gapMin) {
            return false;
        }

        // safety criterion (a>-bsave); 
        double bNew_acc = bNew.model().calcAcc(bNew,me);
        if (bNew_acc < -bsave) {
            return false;
        }
        double my_acc = me.model().calcAcc(me,fNew);
        if (my_acc < -bsaveSelf) {
            return false;
        }

        // incentive criterion (always model of BACK vehicle used!!)
        // works also for on-ramp: on-ramp has 1 lane with index 0
        // === LEFT on main road -> strong desired to change = 
        // large positive biasRight for lcModel of ramp vehicles

        double my_adv  = my_acc - me.model().calcAcc(me,fOld)
                       + ((me.lane()==LEFT) ? 1 : -1) * biasRight;

        double others_disadv = bNew.model().calcAcc(bNew,fNew) - bNew_acc;

        if (others_disadv<0) {
            others_disadv=0;
        }
        return my_adv - p*others_disadv > db ? true : false;
    }
}
