package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.trafficsimulation.core.Constants;

/* TODO 20110519
- return to main menu after long pause DONE
- moving average + popup: flow OK / broken on each flow game sim GUI?
- logos + colors
- ramp metering game
+ vsl game
- change to run background sims for a fixed time DONE

- spin up n bg threads
- run sims in series
- still need a warmup
- just propagate flow updates to the main thread
 */

public abstract class URoadGamePanel extends JPanel implements Constants {
  private static final long serialVersionUID = 1L;
  
  private final JButton backButton;
  
  private final JSlider flowInSlider;
  
  private final JSlider rampFlowSlider;
  
  private final JSlider speedSlider;
  
  private final JPanel scorePanel;
  
  private final CardLayout scoreCardLayout;
  
  private final String SCORE_CARD_SCORE = "score";
  
  private final String SCORE_CARD_CALCULATING = "calc";
  
  private final JProgressBar roundProgressBar;
  
  private final JLabel scoreLabel;
  
  private final List<URoadCanvas> simCanvases;
  
  /**
   * Used to update the GUI with the results of the background simulations,
   * as they're running.
   */
  private final Timer gameProgressTimer;
  
  /**
   * Handles running the simulations in the background; null iff not running
   * (i.e. between rounds).
   */
  private BackgroundAverageRunner simRunner;
 
  private final static int PAD = 10; // px
  
  private final static int SIM_PAD = 5; // px
  
  private final static int SIM_ROWS = 2;
  private final static int SIM_COLS = 2;
  
  /**
   * Time steps per frame drawn for the visible sims. A large number here makes
   * them run very fast.
   */
  private final static int TIME_STEPS_PER_FRAME = 20;
  
  /**
   * Update the sim progress meter this often.
   */
  private static final int GAME_PROGRESS_DELAY_MS = 100;

  /**
   * Don't update the score estimate until we have at least this many runs
   * finished.
   */
  private static final int MIN_RUNS_FOR_ESTIMATE = 5;
  
  public URoadGamePanel() {
    setLayout(new BorderLayout());
    
    //
    // top panel
    //
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
    add(topPanel, BorderLayout.NORTH);
    
    JLabel titleLabel = new JLabel("Flow Game");
    titleLabel.setFont(titleLabel.getFont().deriveFont(20f));
    topPanel.add(Box.createHorizontalStrut(PAD));
    topPanel.add(titleLabel);
    
    backButton = new JButton("< Back");
    backButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        goBack();
      }
    });
    topPanel.add(Box.createHorizontalGlue());
    topPanel.add(backButton);
    
    //
    // left panel (game controls)
    //
    JPanel controlPanel = new JPanel();
    add(controlPanel, BorderLayout.WEST);
    
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
    controlPanel.add(Box.createVerticalStrut(PAD));
    controlPanel.add(new JLabel("set flow in on main road (veh/hr):"));
    
    flowInSlider = new JSlider(0, Q_MAX, Q_INIT2);
    flowInSlider.setMajorTickSpacing(Q_MAX/4);
    flowInSlider.setPaintLabels(true);
    flowInSlider.setPaintTicks(true);
    flowInSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        endRound();
        beginRound();
      }
    });
    controlPanel.add(flowInSlider);
    
    controlPanel.add(Box.createVerticalStrut(PAD));
    controlPanel.add(new JLabel("set flow in on ramp (veh/hr):"));
    rampFlowSlider = new JSlider(0, QRMP_MAX, QRMP_INIT2);
    rampFlowSlider.setMajorTickSpacing(QRMP_MAX/4);
    rampFlowSlider.setPaintLabels(true);
    rampFlowSlider.setPaintTicks(true);
    rampFlowSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        endRound();
        beginRound();
      }
    });
    controlPanel.add(Box.createVerticalStrut(PAD));
    controlPanel.add(rampFlowSlider);
    
    controlPanel.add(Box.createVerticalStrut(PAD));
    controlPanel.add(new JLabel("set speed limit (km/h):"));
    speedSlider = new JSlider((int)V0_MIN_KMH, (int)V0_MAX_KMH,
        (int)V0_INIT_KMH);
    speedSlider.setMajorTickSpacing((int)(V0_MAX_KMH/4.0));
    speedSlider.setPaintLabels(true);
    speedSlider.setPaintTicks(true);
    speedSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        endRound();
        beginRound();
      }
    });
    controlPanel.add(Box.createVerticalStrut(PAD));
    controlPanel.add(speedSlider);
    
    controlPanel.add(Box.createVerticalStrut(PAD));
    controlPanel.add(new JLabel("flow out: "));
    controlPanel.add(Box.createVerticalStrut(PAD));
    
    scorePanel = new JPanel();
    controlPanel.add(scorePanel);
    controlPanel.add(Box.createVerticalGlue()); // fill up rest of space
    
    scoreCardLayout = new CardLayout();
    scorePanel.setLayout(scoreCardLayout);
    
    JPanel progressPanel = new JPanel();
    scorePanel.add(progressPanel, SCORE_CARD_CALCULATING);
    
    roundProgressBar = new JProgressBar(0, (int)MIN_RUNS_FOR_ESTIMATE);
    progressPanel.add(roundProgressBar);
    
    JPanel scoreLabelPanel = new JPanel();
    scorePanel.add(scoreLabelPanel, SCORE_CARD_SCORE);
    
    scoreLabel = new JLabel();
    scoreLabelPanel.add(scoreLabel);
   
    //
    // right panel (simulation grid)
    //
    JPanel simPanel = new JPanel(new GridLayout(SIM_ROWS, SIM_COLS));
    simCanvases = new ArrayList<URoadCanvas>();
    for (int i = 0; i < SIM_ROWS; ++i) {
      for (int j = 0; j < SIM_COLS; ++j) {
        URoadCanvas canvas = new URoadCanvas();
        canvas.setTimeStepsPerFrame(TIME_STEPS_PER_FRAME);
        canvas.setBorder(BorderFactory.createMatteBorder(
            SIM_PAD, SIM_PAD, SIM_PAD, SIM_PAD, Color.BLACK));
        simPanel.add(canvas);
        
        canvas.getFloatPanel().add(new JLabel(""));
        
        simCanvases.add(canvas);
      }
    }
    add(simPanel, BorderLayout.CENTER);
    
    //
    // set up background threads and results collection for sims
    //
    gameProgressTimer = new Timer(GAME_PROGRESS_DELAY_MS, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (simRunner == null)
          return;
        
        // update per-sim flow meter
        for (URoadCanvas simCanvas : simCanvases) {
          JLabel label = (JLabel) simCanvas.getFloatPanel().getComponent(0);
          label.setText(String.format("%.0f cars / hour",
              simCanvas.getFlowMonitor().getMeanFlowOut() * 3600));
        }
        
        // update score, if we have enough data
        if (simRunner.getFlowCount() > MIN_RUNS_FOR_ESTIMATE) {
          scoreCardLayout.show(scorePanel, SCORE_CARD_SCORE);
          scoreLabel.setText(String.format("%.0f cars per hour",
              3600.0 * simRunner.getFlowMean()));
        } else {
          roundProgressBar.setValue(simRunner.getFlowCount());
        }
      }
    });
  }
  
  protected void beginRound() {
    // each sim uses the same value for qIn (in flow on main road)
    final double qIn = flowInSlider.getValue() / 3600.;
    final double qRamp = rampFlowSlider.getValue() / 3600.;
    final double v0 = speedSlider.getValue();
    
    // spin up background simulation threads
    startBackgroundSims(qIn, qRamp, v0);
    
    // start sim visualisation
    for (URoadCanvas canvas : simCanvases) {
      canvas.start();
      canvas.getSim().qIn = qIn;
      canvas.getSim().qRamp = qRamp;
      canvas.getSim().getStreet().getVehicleFactory().getCarIDM().v0 = v0;
      canvas.getSim().getStreet().getVehicleFactory().getTruckIDM().v0 = v0;
      canvas.getSim().getOnRamp().getVehicleFactory().getCarIDM().v0 = v0;
      canvas.getSim().getOnRamp().getVehicleFactory().getTruckIDM().v0 = v0;
    }
    
    // start the timer that polls the background sim results until all sims
    // are finished
    scoreCardLayout.show(scorePanel, SCORE_CARD_CALCULATING);
    gameProgressTimer.start();
    roundProgressBar.setValue(0);
    roundProgressBar.setIndeterminate(true); // TODO HACK
  }
  
  private void startBackgroundSims(final double qIn, final double qRamp,
      final double v0) {
    // each sim needs to know how long the roads are; use the first sim canvas
    // to compute this (but note that they're all identical)
    final double uRoadLengthMeters =
      simCanvases.get(0).getURoad().getRoadLengthMeters();
    final double rampLengthMeters = 
      simCanvases.get(0).getOnRampRoad().getRoadLengthMeters();
    
    // create the runner; this starts the sims running on background threads
    simRunner = new BackgroundAverageRunner() {
      @Override
      protected URoadSim getNewSim() {
        // each sim gets its own random number sequence; note that Java's
        // Random() uses more than just the current time to seed the generator
        // (there is a counter, too), and it handles concurrent access (the
        // counter is marked volatile)
        URoadSim sim = new URoadSim(new Random(), uRoadLengthMeters,
            rampLengthMeters);

        sim.qIn = qIn;
        sim.qRamp = qRamp;
        sim.getStreet().getVehicleFactory().getCarIDM().v0 = v0;
        sim.getStreet().getVehicleFactory().getTruckIDM().v0 = v0;
        sim.getOnRamp().getVehicleFactory().getCarIDM().v0 = v0;
        sim.getOnRamp().getVehicleFactory().getTruckIDM().v0 = v0;
        
        return sim;
      }
    };
  }
  
  protected void endRound() {
    gameProgressTimer.stop();
    for (URoadCanvas canvas : simCanvases) {
      canvas.stop();
    }
    simRunner.stop();
  }
  
  public void start() {
    // set default
    flowInSlider.setValue(Q_INIT2);
    rampFlowSlider.setValue(QRMP_INIT2);
    speedSlider.setValue((int)V0_INIT_KMH);
    beginRound();
  }

  public void stop() {
    for (URoadCanvas canvas : simCanvases) {
      canvas.stop();
    }
  }
  
  /**
   * Called when the user presses the back button.
   */
  public abstract void goBack();
  
  /** For testing */
  public static void main(String [] args) {
    JFrame f = new JFrame("ring test");
    f.setSize(640,480);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    URoadGamePanel p = new URoadGamePanel() {
      private static final long serialVersionUID = 1L;

      @Override
      public void goBack() {
      }
    };
    f.add(p);
    f.setVisible(true);
    p.start();
  }
}
