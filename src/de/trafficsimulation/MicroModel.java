package de.trafficsimulation;


/** Representation of a geneal micromodel for single-lane longitudinal dynamics.
Besides the IDM variants (classes IDM*), also other models such as the OVM (optimal-velocity model) could be developed.
*/

public interface MicroModel {
    public double Veq(double dx);
    public double calcAcc(Moveable bwd, Moveable vwd);
}
