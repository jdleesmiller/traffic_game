package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class NewURoadGamePanel extends JPanel {
  private static final long serialVersionUID = 1L;

  private static final int FLOW_IN_MIN = 0;

  private static final int FLOW_IN_MAX = 3000;

  private static final int FLOW_IN_INIT = 2500;

  private static final int PAD = 10;

  /**
   * Run the sim for this long before displaying the results.
   */
  private static final double WARMUP_SECONDS = 30*60;
  
  /**
   * Time steps per frame drawn for the visible sims. A large number here makes
   * them run very fast.
   */
  private final static int TIME_STEPS_PER_FRAME = 10;
  
  private final String GAME_CARD_WARMUP = "warmup";
  
  private final String GAME_CARD_BASIC = "basic";
  
  private final JSlider flowInSlider;
  
  private final URoadCanvas simCanvas;
  
  private final JPanel gameContainer;
  
  private final CardLayout gameCards;
  
  private BackgroundWarmupRunner warmupPool;
  
  private enum GameState {
    BASIC
  };
  
  private GameState gameState = GameState.BASIC;

  public NewURoadGamePanel() {
    super(new BorderLayout());
    JPanel controlPanel = new JPanel(new BorderLayout());
    add(controlPanel, BorderLayout.WEST);
    
    //
    // control panel
    //
    
    JLabel titleLabel = new JLabel("junction");
    titleLabel.setHorizontalAlignment(JLabel.CENTER);
    titleLabel.setFont(Resource.TITLE_FONT.deriveFont(48f));
    titleLabel.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
    controlPanel.add(titleLabel, BorderLayout.NORTH);
    
    JPanel flowPanel = new JPanel();
    controlPanel.add(flowPanel, BorderLayout.CENTER);
    
    flowInSlider = new JSlider(FLOW_IN_MIN, FLOW_IN_MAX, FLOW_IN_INIT);
    flowInSlider.setMajorTickSpacing((FLOW_IN_MAX - FLOW_IN_MIN)/10);
    flowInSlider.setPaintTicks(true);
    flowInSlider.putClientProperty("JComponent.sizeVariant", "large");
    flowInSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        updateFlowIn();
      }
    });
    flowPanel.add(flowInSlider, BorderLayout.NORTH);
    
    //
    // game panels
    //
    
    gameCards = new CardLayout();
    gameContainer = new JPanel(gameCards);
    add(gameContainer, BorderLayout.CENTER);
    
    //
    // 'please wait' screen
    //
    JProgressBar warmUpBar = new JProgressBar();
    warmUpBar.setIndeterminate(true);
    warmUpBar.putClientProperty("JComponent.sizeVariant", "large");
    gameContainer.add(warmUpBar, GAME_CARD_WARMUP);
    
    //
    // basic flowIn game
    //
    
    simCanvas = new URoadCanvas();
    simCanvas.setBorder(new RoundedBorder(Resource.BACKGROUND.brighter(),
        null, MessageBubble.CORNER_RADIUS,
        MessageBubble.BORDER_WIDTH, true));
    simCanvas.setTimeStepsPerFrame(TIME_STEPS_PER_FRAME);
    gameContainer.add(simCanvas, GAME_CARD_BASIC);
    
    //
    // background thread pool for sim warmups; we don't want this to be null,
    // so we create an empty one to start with
    //
    warmupPool = new BackgroundWarmupRunner();
  }
  
  private void updateFlowIn() {
    // stop previous run, if any
    simCanvas.stop();
    warmupPool.stop();
    
    // will have to run some warmups
    gameCards.show(gameContainer, GAME_CARD_WARMUP);
    
    switch(gameState) {
    case BASIC:
      simCanvas.start();
      setSimParameters(simCanvas.getSim());
      simCanvas.pause();
      
      ArrayList<SimBase> sims = new ArrayList<SimBase>(1);
      sims.add(simCanvas.getSim());
      warmupPool = new BackgroundWarmupRunner(sims, WARMUP_SECONDS,
          new Runnable() {
            @Override
            public void run() {
              gameCards.show(gameContainer, GAME_CARD_BASIC);
              simCanvas.resume();
            }
          });
      break;
    }
  }
  
  /**
   * Copy parameters from GUI to given sim.
   * 
   * @param sim not null
   */
  private void setSimParameters(URoadSim sim) {
    sim.qIn = flowInSlider.getValue() / 3600.;
  }
  
  public void start() {
    gameState = GameState.BASIC;
    updateFlowIn();
  }
  
  /** For testing */
  public static void main(String [] args) {
    JFrame f = new JFrame("ring test");
    f.setSize(640,480);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    NewURoadGamePanel p = new NewURoadGamePanel() {
      private static final long serialVersionUID = 1L;
    };
    f.add(p);
    f.setVisible(true);
    p.start();
  } 
}
