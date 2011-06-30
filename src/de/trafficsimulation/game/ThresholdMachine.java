package de.trafficsimulation.game;

/**
 * A threshold-based state machine. Each state corresponds to a (possibly
 * infinite) interval of the real line. An observation causes the machine to
 * transition to the corresponding state. A tolerance can be provided to avoid
 * chatter at the boundaries between states.
 */
public class ThresholdMachine {
  private final String[] states;
  private final double[] thresholds;
  private final double tolerance;
  private int state;

  /**
   * 
   * @param thresholds
   *          finite thresholds between states; NaNs not allowed
   * @param states
   *          names of states; the number of states must be one more than the
   *          number of thresholds
   * @param tolerance
   *          tolerance for switching between states; non-negative
   */
  public ThresholdMachine(double[] thresholds, String[] states, double tolerance) {
    if (thresholds.length != states.length - 1)
      throw new IllegalArgumentException("threshold/state mismatch");
    if (states.length < 1)
      throw new IllegalArgumentException("must have at least one state");

    // add sentinels to the thresholds
    this.thresholds = new double[thresholds.length + 2];
    this.thresholds[0] = Double.NEGATIVE_INFINITY;
    this.thresholds[this.thresholds.length - 1] = Double.POSITIVE_INFINITY;
    for (int i = 0; i < thresholds.length; ++i) {
      this.thresholds[i + 1] = thresholds[i];
    }

    this.states = states;
    this.tolerance = tolerance;
    state = 0;
  }

  /**
   * Observe a new value; this will cause the machine to transition to a new
   * state if the value is not within tolerance for the current state.
   * 
   * @param value
   *          finite; not NaN
   * @return true iff state changed as a result of this observation
   */
  public boolean observe(double value) {
    if (isValueInState(value, state, tolerance)) {
      // the value is in the current state (within tolerance)
      return false;
    } else {
      // we have to change state; find new state without using tolerance
      for (state = 0; state < states.length; ++state)
        if (isValueInState(value, state, 0))
          return true;

      // should be unreachable due to sentinels
      throw new IllegalStateException("value does not match any state");
    }
  }

  /**
   * The current state of the machine.
   * 
   * @return an element from the states array passed upon construction
   */
  public String getState() {
    return states[state];
  }

  /**
   * The index of the current state of the machine.
   * 
   * @return in [0, states.length)
   */
  public int getStateIndex() {
    return state;
  }

  private boolean isValueInState(double value, int state, double tolerance) {
    // note: here we use the sentinels at the ends of the thresholds array
    double lo = thresholds[state];
    double hi = thresholds[state + 1];
    return lo - tolerance < value && value < hi + tolerance;
  }
}
