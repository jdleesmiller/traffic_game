package de.trafficsimulation;

// Trucks in downstream bottleneck region of potential synchr. traffic

public class IDMTruckSyncdown extends IDM implements MicroModel{

    public IDMTruckSyncdown(){
        System.out.println("in Cstr of IDMTruckSyncdown (no own ve calc)");

        v0=30/3.6;
        delta=4.0;
        a=0.1;
        b=2;
        s0=2.0;
        T=2.0;
        sqrtab=Math.sqrt(a*b);
	initialize();
    }
}
