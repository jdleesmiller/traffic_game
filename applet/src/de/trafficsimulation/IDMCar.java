package de.trafficsimulation;

public class IDMCar extends IDM implements MicroModel, Constants{


    public IDMCar(){
	//    System.out.println("in Cstr of class IDMCar (no own ve calc)");

	v0=V0_INIT_KMH/3.6;
	delta=4.0;
	a=A_INIT_CAR_MSII;  //1
	b=B_INIT_MSII;  //1.0
	s0=S0_INIT_M;
	T =T_INIT_S;  //1.5
	sqrtab=Math.sqrt(a*b);
	initialize();
    }
}
