package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.Timer;
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
  
  /**
   * Update the sim flow out meter this often.
   */
  private static final int SCORE_TIMER_DELAY_MS = 100;
  
  private final static String CARD_WARMUP = "warmup";
  
  private final static String CARD_GAME = "basic";
  
  private final static String CARD_HIGH_FLOW = "high";
  
  private final static String CARD_MEDIUM_FLOW = "medium";
  
  private final static String CARD_OPTIMAL_FLOW = "optimal";
  
  private final static String CARD_LOW_FLOW = "low";
  
  private final JSlider flowInSlider;
  
  private final URoadCanvas simCanvas;
  
  private final JPanel messageContainer;
  
  private final CardLayout messageCards;
  
  private final JPanel gameContainer;
  
  private final CardLayout gameCards;
  
  private BackgroundWarmupRunner warmupPool;
  
  private final Timer scoreTimer; 
  
  private final SortedMap<Integer, String> breaks =
    new TreeMap<Integer, String>() {
    private static final long serialVersionUID = 1L;
    {
      put(FLOW_IN_MIN, CARD_LOW_FLOW);
      put(1800, CARD_OPTIMAL_FLOW);
      put(1900, CARD_MEDIUM_FLOW);
      put(2100, CARD_HIGH_FLOW);
    }
  };
  
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
    
    JPanel flowPanel = new JPanel(new BorderLayout());
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
    
    messageCards = new CardLayout();
    messageContainer = new JPanel(messageCards);
    flowPanel.add(messageContainer, BorderLayout.CENTER);
    
    MessageBubble warmupMessageBubble = new MessageBubble();
    warmupMessageBubble.add(Resource.makeStyledTextPane(
        "Calculating..."), BorderLayout.CENTER);
    messageContainer.add(warmupMessageBubble, CARD_WARMUP);
    
    // TODO probably don't want to have this -- need cards based on slider
    // but could still have a warmup card... not really necessary, but makes
    // the user feel more in control
    MessageBubble highFlowMessageBubble = new MessageBubble();
    highFlowMessageBubble.add(Resource.makeStyledTextPane(
        "The junction is congested!\n" +
        "Drag the slider to reduce the traffic on the main road."),
        BorderLayout.CENTER);
    messageContainer.add(highFlowMessageBubble, CARD_HIGH_FLOW);
    
    MessageBubble mediumFlowMessageBubble = new MessageBubble();
    mediumFlowMessageBubble.add(Resource.makeStyledTextPane(
        "The flow is unstable!\n" +
        "It may run OK for a while, but flow will eventually break down.\n" +
        "Drag the slider to reduce the traffic on the main road."),
        BorderLayout.CENTER);
    messageContainer.add(mediumFlowMessageBubble, CARD_MEDIUM_FLOW);
    
    MessageBubble optimalFlowMessageBubble = new MessageBubble();
    optimalFlowMessageBubble.add(Resource.makeStyledTextPane(
        "This flow is pretty close to optimal!"),
        BorderLayout.CENTER);
    optimalFlowMessageBubble.add(new RoundedButton("Go to Level 3!") {
      private static final long serialVersionUID = 1L;
      @Override
      public void click() {
        nextLevel();
      }
    }, BorderLayout.SOUTH);
    messageContainer.add(optimalFlowMessageBubble, CARD_OPTIMAL_FLOW);
    
    MessageBubble lowFlowMessageBubble = new MessageBubble();
    lowFlowMessageBubble.add(Resource.makeStyledTextPane(
        "The flow is low!\n" +
        "Drag the slider to increase the traffic on the main road."),
        BorderLayout.CENTER);
    messageContainer.add(lowFlowMessageBubble, CARD_LOW_FLOW);
    
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
    gameContainer.add(warmUpBar, CARD_WARMUP);
    
    //
    // the sim
    //
    
    simCanvas = new URoadCanvas();
    simCanvas.setBorder(new RoundedBorder(Resource.BACKGROUND.brighter(),
        null, MessageBubble.CORNER_RADIUS,
        MessageBubble.BORDER_WIDTH, true));
    simCanvas.setTimeStepsPerFrame(TIME_STEPS_PER_FRAME);
    simCanvas.getFloatPanel().add(new JLabel());
    gameContainer.add(simCanvas, CARD_GAME);
    
    //
    // background thread pool for sim warmups; we don't want this to be null,
    // so we create an empty one to start with
    //
    warmupPool = new BackgroundWarmupRunner();
    
    //
    // periodically update the score
    //
    scoreTimer = new Timer(SCORE_TIMER_DELAY_MS, new ActionListener() {      
      @Override
      public void actionPerformed(ActionEvent e) {
        JLabel label = (JLabel) simCanvas.getFloatPanel().getComponent(0);
        URoadSim sim = simCanvas.getSim();
        if (sim != null) {
          label.setText(String.format("%.0f cars / hour",
              simCanvas.getSim().getMeanFlowOut() * 3600));
        }
      }
    });
  }
  
  private void updateFlowIn() {
    // stop previous run, if any
    stop();
    
    // will have to run some warmups
    messageCards.show(messageContainer, CARD_WARMUP);
    gameCards.show(gameContainer, CARD_WARMUP);
    
    // create new sim, but don't start running yet
    simCanvas.start();
    setSimParameters(simCanvas.getSim());
    simCanvas.pause();
      
    // run warmup in the background
    final int flowIn = flowInSlider.getValue();
    ArrayList<SimBase> sims = new ArrayList<SimBase>(1);
    sims.add(simCanvas.getSim());
    warmupPool = new BackgroundWarmupRunner(sims, WARMUP_SECONDS,
        new Runnable() {
      @Override
      public void run() {
        gameCards.show(gameContainer, CARD_GAME);
        showFlowMessage(flowIn);
        simCanvas.resume();
        scoreTimer.start();
      }
    });
  }
  
  private void showFlowMessage(int flowIn) {
    // note: this loop gives us the keys in ascending order
    int flowInKey = FLOW_IN_MIN;
    for (int key : breaks.keySet()) {
      if (flowIn >= key)
        flowInKey = key;
    }
    messageCards.show(messageContainer, breaks.get(flowInKey));
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
    updateFlowIn();
  }
  
  public void stop() {
    scoreTimer.stop();
    simCanvas.stop();
    warmupPool.stop();
  }
  
  public void nextLevel() {
    
  }
  
  /** For testing */
  public static void main(String [] args) {
    JFrame f = new JFrame("ring test");
    f.setSize(1024,600);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    NewURoadGamePanel p = new NewURoadGamePanel() {
      private static final long serialVersionUID = 1L;
    };
    f.add(p);
    f.setVisible(true);
    p.start();
  } 
}
