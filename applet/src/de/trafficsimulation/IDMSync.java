package de.trafficsimulation;

public class IDMSync extends IDM implements MicroModel{

    public IDMSync(){
        System.out.println("in Cstr of class IDMSync (no own ve calc)");

        v0=33.0;
        delta=4.0;
        a=2.0;
        b=0.9;
        s0=2.0;
        T=1.0;
        sqrtab=Math.sqrt(a*b);
	initialize();
    }
}

