package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Common structure for the games on the URoad.
 */
public abstract class URoadGameBase extends JPanel {
  private static final long serialVersionUID = 1L;
  
  protected static final int FLOW_IN_MIN = 1000;

  protected static final int FLOW_IN_MAX = 2600;

  protected static final int FLOW_IN_INIT = 2500;

  protected static final int PAD = 10;
  
  /**
   * Run the sim for this long before displaying the results.
   */
  protected static final double WARMUP_SECONDS = 90*60;
  
  /**
   * Time steps per frame drawn for the visible sims. A large number here makes
   * them run very fast.
   */
  protected final static int TIME_STEPS_PER_FRAME = 10;
  
  /**
   * The name of the card to show when the simulator is starting a new run. 
   */
  protected final static String CARD_WARMUP = "warmup";
  
  /**
   * The name of the card to show when the simulator is drawing.
   */
  protected final static String CARD_GAME = "game";
  
  protected final JSlider flowInSlider;

  protected final JPanel messageContainer;

  protected final CardLayout messageCards;

  protected final JPanel gameContainer;

  protected final CardLayout gameCards;
  
  public URoadGameBase() {
    super(new BorderLayout());
    
    JPanel controlPanel = new JPanel(new BorderLayout());
    controlPanel.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
    add(controlPanel, BorderLayout.WEST);
    
    //
    // control panel
    //
    
    JLabel titleLabel = new JLabel("junction");
    titleLabel.setHorizontalAlignment(JLabel.CENTER);
    titleLabel.setFont(Resource.TITLE_FONT.deriveFont(48f));
    titleLabel.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, 2*PAD, PAD));
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
    messageContainer.setBorder(BorderFactory.createEmptyBorder(PAD, 0, 0, 0));
    flowPanel.add(messageContainer, BorderLayout.CENTER);
    
    MessageBubble warmupMessageBubble = new MessageBubble();
    warmupMessageBubble.add(Resource.makeStyledTextPane(
        "Starting Simulation..."), BorderLayout.CENTER);
    messageContainer.add(warmupMessageBubble, CARD_WARMUP);
    
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
    
    JPanel warmUpPanel = new JPanel(new GridBagLayout()); // float center
    warmUpPanel.add(warmUpBar);
    gameContainer.add(warmUpPanel, CARD_WARMUP);
  }
  
  public void start() {
    updateFlowIn();
  }
  
  protected abstract void updateFlowIn();
}
