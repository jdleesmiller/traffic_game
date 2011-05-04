package de.trafficsimulation;

public class IDMTruck extends IDM implements MicroModel, Constants{

    public IDMTruck(){
	//    System.out.println("in Cstr of class IDMTruck (no own ve calc)");

        v0=22.2;
        delta=4.0;
        a=A_INIT_TRUCK_MSII;
        b=4.0;
        s0=2.0;
        T=1.7;
        sqrtab=Math.sqrt(a*b);
	initialize();
    }
}

