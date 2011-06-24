package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class URoadSpeedGamePanel extends URoadGameBase {
  private static final long serialVersionUID = 1L;

  /**
   * Update the sim flow out meter this often.
   */
  private static final int SCORE_TIMER_DELAY_MS = 100;
  
  private final static int SIM_GRID_ROWS = 2;
  
  private final static int SIM_GRID_COLS = 2;
  
  private final static int[] simSpeeds = {70, 80, 60, 50};
  
  private final List<URoadCanvas> simCanvases;
  
  private final Timer scoreTimer; 
  
  private BackgroundWarmupRunner warmupPool;
  
  public URoadSpeedGamePanel() {
    //
    // in-game message 
    //
    MessageBubble mediumFlowMessageBubble = new MessageBubble();
    mediumFlowMessageBubble.add(UI.makeStyledTextPane(
        "Lower speed limits lead to higher capacity."),
        BorderLayout.CENTER);
    messageContainer.add(mediumFlowMessageBubble, CARD_GAME);
    
    //
    // simulation grid
    //
    simCanvases = new ArrayList<URoadCanvas>();
    JPanel simGrid = new JPanel(new GridLayout(SIM_GRID_ROWS, SIM_GRID_COLS));
    for (int i = 0; i < simSpeeds.length; ++i) {
      URoadCanvas simCanvas = new URoadCanvas();
      simCanvas.setBorder(BorderFactory.createCompoundBorder(BorderFactory
          .createEmptyBorder(PAD, PAD, PAD, PAD), new RoundedBorder(
          UI.BACKGROUND.brighter(), null, MessageBubble.CORNER_RADIUS,
          MessageBubble.BORDER_WIDTH, true)));
      simCanvas.setTimeStepsPerFrame(TIME_STEPS_PER_FRAME);
      simCanvas.getFloatPanel().add(new JLabel());
      simGrid.add(simCanvas);
      simCanvases.add(simCanvas);
    }
    gameContainer.add(simGrid, CARD_GAME);
    
    //
    // background thread pool for sim warmups; we don't want this to be null,
    // so we create an empty one to start with
    //
    warmupPool = new BackgroundWarmupRunner();
    
    //
    // periodically update the scores
    //
    scoreTimer = new Timer(SCORE_TIMER_DELAY_MS, new ActionListener() {      
      @Override
      public void actionPerformed(ActionEvent e) {
        for (URoadCanvas simCanvas : simCanvases) {
          JLabel label = (JLabel) simCanvas.getFloatPanel().getComponent(0);
          URoadSim sim = simCanvas.getSim();
          if (sim != null) {
            label.setText(String.format("%.0f cars / hour",
                simCanvas.getSim().getMeanFlowOut() * 3600));
          }
        }
      }
    });
  }
  
  @Override
  protected void updateFlowIn() {
    // stop previous run, if any
    stop();
    
    // will have to run some warmups
    messageCards.show(messageContainer, CARD_WARMUP);
    gameCards.show(gameContainer, CARD_WARMUP);
    
    // create new sim, but don't start running yet
    ArrayList<SimBase> sims = new ArrayList<SimBase>(1);
    for (int i = 0; i < simSpeeds.length; ++i) {
      URoadCanvas simCanvas = simCanvases.get(i);
      simCanvas.start();
      setSimParameters(simCanvas.getSim(), simSpeeds[i]);
      simCanvas.pause();
      sims.add(simCanvas.getSim());
    }
    
    // run warmup in the background
    warmupPool = new BackgroundWarmupRunner(sims, WARMUP_SECONDS,
        new Runnable() {
      @Override
      public void run() {
        messageCards.show(messageContainer, CARD_GAME);
        gameCards.show(gameContainer, CARD_GAME);
        for (URoadCanvas simCanvas : simCanvases) {
          simCanvas.resume();
        }
        scoreTimer.start();
      }
    });
  }
  
  private void setSimParameters(URoadSim sim, int speedMph) {
    sim.qIn = flowInSlider.getValue() / 3600.;
    double v0 = Utility.milesPerHourToMetersPerSecond(speedMph);
    sim.getStreet().getVehicleFactory().getCarIDM().v0 = v0;
    sim.getStreet().getVehicleFactory().getTruckIDM().v0 = v0;
    sim.getOnRamp().getVehicleFactory().getCarIDM().v0 = v0;
    sim.getOnRamp().getVehicleFactory().getTruckIDM().v0 = v0;
  }

  public void stop() {
    scoreTimer.stop();
    for (URoadCanvas simCanvas : simCanvases)
      simCanvas.stop();
    warmupPool.stop();
  }
  
  /** For testing */
  public static void main(String [] args) {
    JFrame f = new JFrame("ring test");
    f.setSize(1024,600);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    URoadSpeedGamePanel p = new URoadSpeedGamePanel() {
      private static final long serialVersionUID = 1L;
    };
    f.add(p);
    f.setVisible(true);
    p.start();
  } 
  
}
