package de.trafficsimulation.game;

import de.trafficsimulation.core.Constants;

public class Sim implements Constants {
  public double density = 0.001 * DENS_INIT_INVKM; // avg. density closed s.
  public double p_factor = 0.; // lanechanging: politeness factor
  public double deltaB = 0.2; // lanechanging: changing threshold
  public int floatcar_nr = 0;
}
