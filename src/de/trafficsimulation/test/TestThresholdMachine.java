package de.trafficsimulation.test;

import org.junit.Test;

import de.trafficsimulation.game.ThresholdMachine;

import static org.junit.Assert.*;

public class TestThresholdMachine {
  @Test
  public void testThresholdMachine1() {
    ThresholdMachine m = new ThresholdMachine(
        new double[] {5}, new String[] {"lo","hi"}, 0.5);
    assertEquals("lo", m.getState());
    
    m.observe(-3);
    assertEquals("lo", m.getState());
    m.observe(3);
    assertEquals("lo", m.getState());
    m.observe(4.9);
    assertEquals("lo", m.getState());
    
    // this is over the threshold, but within tolerance
    m.observe(5.4);
    assertEquals("lo", m.getState());
    
    // this is outside of tolerance
    m.observe(5.6);
    assertEquals("hi", m.getState());
    
    m.observe(6);
    assertEquals("hi", m.getState());
    
    // this is under the threshold, but within tolerance
    m.observe(4.6);
    assertEquals("hi", m.getState());
    
    // this is outside of tolerance
    m.observe(4.4);
    assertEquals("lo", m.getState());
  }
  
  @Test
  public void testThresholdMachine2() {
    ThresholdMachine m = new ThresholdMachine(
        new double[] {2,7}, new String[] {"lo","med","hi"}, 0.5);
    assertEquals("lo", m.getState());
    
    m.observe(2.4);
    assertEquals("lo", m.getState());
    
    m.observe(2.6);
    assertEquals("med", m.getState());
    
    m.observe(7.4);
    assertEquals("med", m.getState());
    
    m.observe(7.6);
    assertEquals("hi", m.getState());
  }
}
