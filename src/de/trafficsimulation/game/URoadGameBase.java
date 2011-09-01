package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 * Common structure for the games on the URoad.
 */
public abstract class URoadGameBase extends JPanel {
  private static final long serialVersionUID = 1L;
  
  protected static final int FLOW_IN_MIN = 1000;

  protected static final int FLOW_IN_MAX = 2600;

  protected static final int FLOW_IN_INIT = 2500;

  /**
   * Run the sim for this long before displaying the results.
   */
  protected static final double WARMUP_SECONDS = 90*60;
  
  /**
   * Time steps per frame drawn for the visible sims. A large number here makes
   * them run very fast.
   */ protected final static int TIME_STEPS_PER_FRAME = 1;
  
  /**
   * The name of the card to show when the simulator is starting a new run. 
   */
  protected final static String CARD_WARMUP = "warmup";
  
  /**
   * The name of the card to show when the simulator is drawing.
   */
  protected final static String CARD_GAME = "game";
  
  protected final BigSlider flowInSlider;

  protected final JPanel messageContainer;

  protected final CardLayout messageCards;

  protected final JPanel gameContainer;

  protected final CardLayout gameCards;

  protected final JPanel ideaContainer;
  
  public URoadGameBase(String title, boolean next) {
    super(new BorderLayout());
    
    //
    // title bar
    //
    GameChoicePanel titleBar = new GameChoicePanel(true, title, next) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onBackClicked() {
        URoadGameBase.this.onBackClicked();
      }

      @Override
      public void onNextClicked() {
        URoadGameBase.this.onNextClicked();
      }
    };
    add(titleBar, BorderLayout.NORTH);
    
    //
    // control panel
    //
    JPanel leftPanel = new JPanel();
    leftPanel.setBorder(BorderFactory.createEmptyBorder(UI.PAD, UI.PAD, UI.PAD, UI.PAD));
    leftPanel.setLayout(new GridLayout(2, 1, 0, UI.PAD));
    add(leftPanel, BorderLayout.WEST);
    
    ideaContainer = new MessageBubble();
    leftPanel.add(ideaContainer);
    
    JPanel controlPanel = new MessageBubble();
    leftPanel.add(controlPanel);
    
    flowInSlider = new BigSlider(FLOW_IN_MIN, FLOW_IN_MAX, FLOW_IN_INIT) {
      private static final long serialVersionUID = 1L;
      
      {
        setPreferredSize(new Dimension(1, 100));
        setBorder(BorderFactory.createEmptyBorder(UI.PAD, 0, 3 * UI.PAD, 0));
      }
      
      @Override
      public void onValueUpdated() {
        updateFlowIn();
      }
    };
    flowInSlider.setBackground(Color.WHITE);
    controlPanel.add(flowInSlider, BorderLayout.NORTH);
    
    messageCards = new CardLayout();
    messageContainer = new JPanel(messageCards);
    messageContainer.setBackground(Color.WHITE);
    controlPanel.add(messageContainer, BorderLayout.CENTER);
    
    JPanel warmupMessage = new JPanel();
    warmupMessage.setBackground(Color.WHITE);
    warmupMessage.add(UI.makeStyledTextPane(
        "Starting Simulation..."), BorderLayout.CENTER);
    messageContainer.add(warmupMessage, CARD_WARMUP);
    
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
    flowInSlider.setValue(FLOW_IN_INIT);
    updateFlowIn();
  }
  
  protected abstract void onBackClicked();
  
  protected abstract void onNextClicked();
  
  protected abstract void updateFlowIn();
}
