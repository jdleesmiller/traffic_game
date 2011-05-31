package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

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

import de.trafficsimulation.core.Constants;
import de.trafficsimulation.game.BackgroundRunner.AggregateResults;

/* TODO 20110519
- return to main menu after long pause DONE
- flow OK / broken on each flow game sim GUI?
- logos + colors
- ramp metering game
+ vsl game
- moving average + popup
- change to run background sims for a fixed time DONE
 */

public abstract class URoadGamePanel extends JPanel implements Constants {
  private static final long serialVersionUID = 1L;
  
  private final JButton backButton;
  
  private final JSlider flowInSlider;
  
  private final JButton playButton;
  
  private final JPanel roundProgressPanel;
  
  private final JProgressBar roundProgressBar;
  
  private final JPanel scorePanel;
  
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
  private BackgroundRunner simRunner;
  
  /**
   * Records when the current round began; valid only between calls to
   * beginRound and endRound.
   */
  private long roundStartMillis;
    
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
   * Number of seconds per round. This should be set long enough to collect
   * enough stats for all in flows, on the machine on which the sim is running.
   */
  private static final int ROUND_TIME_SECONDS = 5;
  
  /**
   * Maximum number of independent simulations to run in the background. Only
   * those that finish before ROUND_TIME_SECONDS elapse will actually run.
   */
  private static final int NUM_BACKGROUND_SIMS = 10;
  
  /**
   * Warmup period for each sim, in seconds. Stats are not collected during the
   * warmup period.
   */
  private static final double BACKGROUND_SIM_WARMUP_SECONDS = 15*60;
  
  /**
   * Total run time for each sim, in seconds. This should be larger than
   * BACKGROUND_SIM_WARMUP_SECONDS, or no results will be collected.
   */
  private static final double BACKGROUND_SIM_TOTAL_SECONDS = 30*60;
  
  /**
   * Update the sim progress meter this often.
   */
  private static final int GAME_PROGRESS_DELAY_MS = 100;

  /**
   * Don't update the score estimate until we have at least this much total
   * time; this doesn't include warm up time. 
   * 
   * This must exceed (BACKGROUND_SIM_TOTAL_SECONDS -
   * BACKGROUND_SIM_WARMUP_SECONDS) * NUM_BACKGROUND_SIMS, or the GUI will just
   * hang forever.
   */
  private static final double MIN_SECONDS_FOR_ESTIMATE =
	  2*BACKGROUND_SIM_TOTAL_SECONDS;
  
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
    controlPanel.add(new JLabel("set flow in on main road:"));
    
    flowInSlider = new JSlider(0, Q_MAX, Q_INIT2);
    flowInSlider.setMajorTickSpacing(Q_MAX/4);
    flowInSlider.setPaintLabels(true);
    flowInSlider.setPaintTicks(true);
    controlPanel.add(flowInSlider);
    controlPanel.add(Box.createVerticalStrut(PAD));
    
    playButton = new JButton("Play");
    playButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        beginRound();
      }
    });
    controlPanel.add(playButton);
    
    roundProgressPanel = new JPanel();
    roundProgressPanel.setVisible(false);
    controlPanel.add(Box.createVerticalStrut(PAD));
    controlPanel.add(roundProgressPanel);
    
    roundProgressBar = new JProgressBar(0, (int)ROUND_TIME_SECONDS*1000);
    roundProgressPanel.add(roundProgressBar);
    
    scorePanel = new JPanel();
    controlPanel.add(Box.createVerticalStrut(PAD));
    controlPanel.add(scorePanel);
    controlPanel.add(Box.createVerticalGlue()); // fill up rest of space
    
    scorePanel.add(new JLabel("score: "));
    
    scoreLabel = new JLabel();
    scorePanel.add(scoreLabel);
   
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
        
        AggregateResults results = simRunner.getAggregateResults();
        
        // update score, if we have enough data
        if (results.totalStatsTime > MIN_SECONDS_FOR_ESTIMATE) {
          scoreLabel.setText(String.format("%.0f cars per hour",
              3600.0 * results.carsOut / results.totalStatsTime));
        } else {
          scoreLabel.setText("warming up...");
        }
        
        // update progress bar based on time run so far, and check for timeout
        int roundMillis = (int)(System.currentTimeMillis() - roundStartMillis);
        if (roundMillis <= ROUND_TIME_SECONDS * 1000) {
          roundProgressBar.setValue(roundMillis);
        } else {
          if (results.totalStatsTime > MIN_SECONDS_FOR_ESTIMATE) {
            // we have enough real time and enough sim time; stop now
            endRound();
          } else if (!roundProgressBar.isIndeterminate()){
            // we don't yet have enough for a score; set the progress bar to
            // indeterminate and wait; this shouldn't happen unless we're on
            // a very slow machine
            roundProgressBar.setIndeterminate(true);
            Utility.log.log(Level.WARNING,
                "round timed out after {0}ms with only {1}s stats time",
                new Object[] {roundMillis, results.totalStatsTime} );
          }
        }
      }
    });
  }
  
  protected void beginRound() {
    // each sim uses the same value for qIn (in flow on main road)
    final double qIn = flowInSlider.getValue() / 3600.;
    
    // spin up background simulation threads
    // note that the default behavior is for the background threads run at
    // priority 5, and for the event dispatch thread to run at priority 6,
    // which is fine for us (don't want to hang the GUI if we spin up lots of
    // background threads)
    startBackgroundSims(qIn);
    
    // start the timer that polls the background sim results until all sims
    // are finished
    gameProgressTimer.start();
    roundProgressBar.setValue(0);
    roundProgressBar.setIndeterminate(false);
    roundProgressPanel.setVisible(true);
    flowInSlider.setEnabled(false);
    playButton.setEnabled(false);
    backButton.setEnabled(false);
    
    // start sim visualisation
    for (URoadCanvas canvas : simCanvases) {
      canvas.start();
      canvas.getSim().qIn = qIn;
    }
    
    roundStartMillis = System.currentTimeMillis();
  }
  
  private void startBackgroundSims(final double qIn) {
    // each sim needs to know how long the roads are; use the first sim canvas
    // to compute this (but note that they're all identical)
    double uRoadLengthMeters =
      simCanvases.get(0).getURoad().getRoadLengthMeters();
    double rampLengthMeters = 
      simCanvases.get(0).getOnRampRoad().getRoadLengthMeters();
    
    // create sims; we create all sims in advance, but we won't necessarily
    // run them all
    List<URoadSim> sims = new ArrayList<URoadSim>();
    for (int i = 0; i < NUM_BACKGROUND_SIMS; ++i) {
      // each sim gets its own random number sequence; note that Java's
      // Random() uses more than just the current time to seed the generator
      // (there is a counter, too); if it didn't, this would be wrong
      URoadSim sim = new URoadSim(new Random(),
          uRoadLengthMeters, rampLengthMeters);
      
      // set our one non-default parameter
      sim.qIn = qIn;
      
      sims.add(sim);
    }
    
    // create the runner; this starts the sims running on background threads
    simRunner = new BackgroundRunner(BACKGROUND_SIM_WARMUP_SECONDS,
        BACKGROUND_SIM_TOTAL_SECONDS, sims);
  }
  
  protected void endRound() {
    gameProgressTimer.stop();
    roundProgressPanel.setVisible(false);
    for (URoadCanvas canvas : simCanvases) {
      canvas.stop();
    }
    simRunner.shutdown();
    roundStartMillis = -1;
    
    flowInSlider.setEnabled(true);
    playButton.setEnabled(true);
    backButton.setEnabled(true);
  }
  
  public void start() {
    // set default
    flowInSlider.setValue(Q_INIT2);
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
