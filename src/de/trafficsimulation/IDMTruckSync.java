package de.trafficsimulation;

public class IDMTruckSync extends IDM implements MicroModel{
    public IDMTruckSync(){
        System.out.println("in Cstr of class IDMTruckSync (no own ve calc)");

        v0=22.2;
        delta=4.0;
        a=0.6;
        b=0.9;
        s0=2.0;
        T=1.0;
        sqrtab=Math.sqrt(a*b);
	initialize();
    }
}
