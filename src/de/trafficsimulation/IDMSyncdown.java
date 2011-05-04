package de.trafficsimulation;

// Cars in downstream bottleneck region of potential synchr. traffic

public class IDMSyncdown extends IDM implements MicroModel{

    public IDMSyncdown(){
        System.out.println("in Cstr of class IDMSyncdown (no own ve calc)");

        v0=25.0;
        delta=4.0;
        a=0.6;
        b=0.9;
        s0=2.0;
        T=2.0;
        sqrtab=Math.sqrt(a*b);
	initialize();
    }
}
