package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.trafficsimulation.core.Constants;

public class MainFrame extends JFrame implements Constants {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static final int SCENARIO_RING_ROAD = 1;
  public static final int SCENARIO_ON_RAMP = 2;
  
  /**
   * Padding between GUI elements, in pixels.
   */
  private static final int PAD = 5;

  private SimCanvas canvas;

  private JButton ringRoadButton;
  private JButton onRampButton;
  
  // for the OnRamp scenario 
  private JPanel onRampControls;
  private JLabel numCarsOutLabel;
  private JSlider flowInSlider;

  public MainFrame() {
    super("Traffic Flow Game");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    // the canvas needs to know when it's been resized
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        canvas.setScales();
      }
    });

    //
    // Create the simulator canvas, which handles the animation.
    //
    
    // NB: these defaults don't matter; they're overwritten later, but we need
    // something sensible to pass to the constructor now
    double density = 0.001 * DENS_INIT_INVKM; // avg. density closed s.
    int tsleep_ms = SPEED_INIT; // sleeping time per sim. step
    double qIn = Q_INIT2; // vehicles per hour
    double p_factorRamp = 0.; // ramp Lanechange factor
    double deltaBRamp = DELTABRAMP_INIT; // ramp Lanechange factor
    double perTr = FRAC_TRUCK_INIT_CIRCLE; // truck fraction !!!
    double p_factor = 0.; // lanechanging: politeness factor
    double deltaB = 0.2; // lanechanging: changing threshold
    canvas = new SimCanvas(SCENARIO_ON_RAMP, density, qIn, perTr, p_factor,
        deltaB, p_factorRamp, deltaBRamp, tsleep_ms) {
      private static final long serialVersionUID = 1L;

      @Override
      public void tick() {
        super.tick();
        numCarsOutLabel.setText(String.format("%d", microstreet.getNumCarsOut()));
      }
    };
    add(canvas, BorderLayout.CENTER);
    
    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
    add(controlPanel, BorderLayout.SOUTH);
    controlPanel.add(Box.createHorizontalStrut(PAD));
    controlPanel.add(new JLabel("scenario:"));
    
    //
    // Scenario selector buttons.
    //
    ringRoadButton = new JButton("ring road");
    ringRoadButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        loadRingRoadScenario();
        toggleControlVisibility();
      }
    });
    controlPanel.add(Box.createHorizontalStrut(PAD));
    controlPanel.add(ringRoadButton);
    
    onRampButton = new JButton("on ramp");
    onRampButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        loadOnRampScenario();
        toggleControlVisibility();
      }
    });
    controlPanel.add(Box.createHorizontalStrut(PAD));
    controlPanel.add(onRampButton);
    
    //
    // Controls for the on ramp scenario.
    //
    onRampControls = new JPanel();
    onRampControls.setLayout(new BoxLayout(onRampControls,
        BoxLayout.LINE_AXIS));
    controlPanel.add(Box.createHorizontalStrut(PAD));
    controlPanel.add(onRampControls);
    
    flowInSlider = new JSlider(0, Q_MAX, Q_INIT2);
    flowInSlider.setMajorTickSpacing(Q_MAX/10);
    flowInSlider.setPaintLabels(true);
    flowInSlider.setPaintTicks(true);
    flowInSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        setParamsForOnRampScenario();
      }
    });
    onRampControls.add(new JLabel("vehicle flow in: "));
    onRampControls.add(flowInSlider);
    onRampControls.add(Box.createHorizontalStrut(PAD));
    onRampControls.add(new JLabel("num vehicles out: "));
    numCarsOutLabel = new JLabel("0");
    onRampControls.add(numCarsOutLabel);
    onRampControls.add(Box.createHorizontalStrut(PAD));
  }
  
  /**
   * Show only the controls for the selected scenario.
   */
  protected void toggleControlVisibility() {
    onRampControls.setVisible(canvas.getScenario() == SCENARIO_ON_RAMP);
  }

  /**
   * Load the ring road scenario. Parameters are set to the defaults from the
   * applet.
   */
  public void loadRingRoadScenario() {
    double density = 0.001 * DENS_INIT_INVKM;
    double qIn = Q_INIT2; // vehicles per hour

    int truckPerc1 = (int) (100 * FRAC_TRUCK_INIT_CIRCLE);
    double perTr = truckPerc1 / 100.;
    
    double v0_limit = 120 / 3.6; // speed limit (m/s)
    double p_factor = 0.; // lanechanging: politeness factor
    double deltaB = 0.2; // lanechanging: changing threshold
    int tsleep_ms = SPEED_INIT; // sleeping time per sim. step

    canvas.stop(); // must stop first
    canvas.newValues(SCENARIO_RING_ROAD, density, qIn, perTr, v0_limit,
        p_factor,
        deltaB, tsleep_ms);
    canvas.start(SCENARIO_RING_ROAD, density);
    toggleControlVisibility();
  }

  /**
   * Set the in flow to the value from the slider control, and set everything
   * else to defaults (taken directly from the applet).
   */
  private void setParamsForOnRampScenario() {
    double qIn = flowInSlider.getValue() / 3600.;
    double qRamp = QRMP_INIT2 / 3600.;
    int truckPerc2 = (int) (100 * FRAC_TRUCK_INIT);
    double perTr = truckPerc2 / 100.; // no slider bar in this scenario//!!!

    double p_factorRamp = 0.;
    double deltaBRamp = DELTABRAMP_INIT; // negative shift threshold for onramp!
    
    double p_factor = 0.; // lanechanging: politeness factor
    double deltaB = 0.2; // lanechanging: changing threshold
    int tsleep_ms = SPEED_INIT; // sleeping time per sim. step
    
    canvas.newValues2(qIn, perTr, p_factor, deltaB, qRamp, p_factorRamp,
        deltaBRamp, tsleep_ms);
  }

  /**
   * Load the on ramp scenario. Parameters are set to the defaults from the
   * applet, except for the in flow, which is set according to the appropriate
   * slider control.
   */
  public void loadOnRampScenario() {
    canvas.stop(); // must stop first
    setParamsForOnRampScenario();
    canvas.start(SCENARIO_ON_RAMP, 0.001 * DENS_INIT_INVKM);
    toggleControlVisibility();
  }

  /**
   * Application entry point.
   * 
   * @param args ignored
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        MainFrame f = new MainFrame();
        f.setVisible(true);
        f.setSize(640, 480);
        f.setExtendedState(f.getExtendedState() | MAXIMIZED_BOTH);
        f.loadRingRoadScenario();
      }
    });
  }
}
