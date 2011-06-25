package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import de.trafficsimulation.core.Constants;

public abstract class RingRoadGamePanel extends JPanel implements Constants {

  private static final long serialVersionUID = 1L;

  private final RingRoadCanvas ringRoadCanvas;
  
  private final BigSlider densitySlider;
  
  private final JPanel messageContainer;
  private final CardLayout messageLayout;
  private final Timer messageTimer;
  
  private final static String LOW_DENSITY_CARD = "low";
  private final static String MEDIUM_DENSITY_CARD = "medium";
  private final static String HIGH_DENSITY_CARD = "high";
  
  /**
   * Location of the "low density" arrow, in vehicles/km.
   */
  private final static int LOW_DENSITY_INVKM = 20;
  
  /**
   * Location of the "medium density" hint, in vehicles/km.
   */
  private final static int MEDIUM_DENSITY_INVKM = 60;
  
  /**
   * Boundary between the low density and medium density messages, in
   * vehicles/km.
   */
  private final static int LOW_MEDIUM_DENSITY_INVKM = 55;
  
  /**
   * Boundary between the medium density and high density messages, in
   * vehicles/km.
   */
  private final static int MEDIUM_HIGH_DENSITY_INVKM = 70;

  /**
   * Update the adaptive messages at this interval, in milliseconds.
   */
  private static final int ADAPTIVE_MESSAGE_TIMER_DELAY_MS = 100;
  
  /**
   * Display the 'phantom jam' message when the speed of the slowest vehicle
   * is below this threshold.
   */
  private static final double MIN_SPEED_FOR_JAM = 5;
  
  /**
   * Display the 'free flow' message when the speed of the slowest vehicle
   * is below this threshold. There should be a reasonably large gap between 
   * MIN_SPEED_FOR_JAM and MIN_SPEED_FOR_FREE, to avoid switching messages due
   * to noise.
   */
  private static final double MIN_SPEED_FOR_FREE = 6;
  
  public RingRoadGamePanel() {
    setLayout(new BorderLayout());
    
    GameChoicePanel titleBar = new GameChoicePanel(true, "phantom jams", true);
    add(titleBar, BorderLayout.NORTH);
    
    //
    // float UI in the center of the ring road
    //
    ringRoadCanvas = new RingRoadCanvas();
    ringRoadCanvas.setBorder(
        BorderFactory.createEmptyBorder(UI.PAD, UI.PAD, UI.PAD, UI.PAD));
    ringRoadCanvas.setLayout(new GridBagLayout());
    add(ringRoadCanvas, BorderLayout.CENTER);
    
    //
    // the bubble
    //
    JPanel controlPanel = new MessageBubble();
    controlPanel.setLayout(new BorderLayout());
    ringRoadCanvas.add(controlPanel);
    
    densitySlider = new BigSlider(DENS_MIN_INVKM, DENS_MAX_INVKM,
        LOW_DENSITY_INVKM, MEDIUM_DENSITY_INVKM) {
      private static final long serialVersionUID = 1L;
      {
        setBackground(Color.WHITE);
        setKnobColor(UI.PURPLE);
        setRampColor(new Color(0xcccccc));
        setHintColor(UI.PURPLE.brighter());
        setBorder(BorderFactory.createEmptyBorder(0, 0, UI.PAD, 0));
      }
      @Override
      public void onValueUpdated() {
        updateDensity();
      }
    };
    controlPanel.add(densitySlider, BorderLayout.NORTH);
    
    //
    // message panels
    //
    messageContainer = new JPanel();
    messageLayout = new CardLayout();
    messageContainer.setLayout(messageLayout);
        
    JPanel lowDensityMessage = new JPanel(new BorderLayout());
    lowDensityMessage.add(UI.makeStyledTextPane(
        "When there are not many cars, traffic flows freely.\n" +
        "Try dragging the slider to add more cars..."), BorderLayout.CENTER);
    messageContainer.add(lowDensityMessage, LOW_DENSITY_CARD);
    
    JPanel mediumDensityMessage = new JPanel(new BorderLayout());
    mediumDensityMessage.add(UI.makeStyledTextPane(
        "When the road gets busy, 'stop and go' waves form.\n" +
        "These are phantom traffic jams.\n\n"), BorderLayout.CENTER);
    mediumDensityMessage.add(new RoundedButton("Go to Level 2!") {
      private static final long serialVersionUID = 1L;
      @Override
      public void click() {
        goToNextLevel();
      }
    }, BorderLayout.SOUTH);
    messageContainer.add(mediumDensityMessage, MEDIUM_DENSITY_CARD);
    
    JPanel highDensityMessage = new JPanel(new BorderLayout());
    highDensityMessage.add(UI.makeStyledTextPane(
        "When the amount of traffic gets too close to the road's capacity,\n" +
        "nobody goes anywhere!\n\n" +
        "Click the button to go to the next level..."), BorderLayout.CENTER);
    highDensityMessage.add(new RoundedButton("Go to Level 2!") {
      private static final long serialVersionUID = 1L;
      @Override
      public void click() {
        goToNextLevel();
      }
    }, BorderLayout.SOUTH);
    messageContainer.add(highDensityMessage, HIGH_DENSITY_CARD);
    controlPanel.add(messageContainer, BorderLayout.SOUTH);
    
    messageTimer = new Timer(ADAPTIVE_MESSAGE_TIMER_DELAY_MS, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        RingRoadSim sim = ringRoadCanvas.getSim();
        if (sim == null)
          return;
        
        double minSpeed = sim.getStreet().getMinSpeed();
        
        if (minSpeed < MIN_SPEED_FOR_JAM) {
          
        } else if (minSpeed > MIN_SPEED_FOR_FREE) {
          
        } else {
          // leave the current message up
        }
      }
    });
  }

  public void start() {
    ringRoadCanvas.start(42);
    densitySlider.setValue(LOW_DENSITY_INVKM);
    updateDensity();
    messageTimer.start();
  }

  private void updateDensity() {
    double density = densitySlider.getValue();
    ringRoadCanvas.getSim().setDensity(density * 1e-3);
    
    //
    // show the appropriate message, based on the slider value
    //
    if (density < LOW_MEDIUM_DENSITY_INVKM) {
      // low density
      densitySlider.setHintValue(MEDIUM_DENSITY_INVKM);
      messageLayout.show(messageContainer, LOW_DENSITY_CARD);
    } else if (density < MEDIUM_HIGH_DENSITY_INVKM) {
      // medium density
      densitySlider.setHintValue(Double.NaN);
      messageLayout.show(messageContainer, MEDIUM_DENSITY_CARD);
    } else {
      // high density
      densitySlider.setHintValue(Double.NaN);
      messageLayout.show(messageContainer, HIGH_DENSITY_CARD);
    }
  }
  
  public void stop() {
    ringRoadCanvas.stop();
    messageTimer.stop();
  }
  
  /**
   * Called when the user presses the back button.
   */
  public abstract void goToNextLevel();
  
  /** For testing */
  public static void main(String [] args) {
    JFrame f = new JFrame("ring test");
    f.setSize(800,600);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    RingRoadGamePanel p = new RingRoadGamePanel() {
      private static final long serialVersionUID = 1L;

      @Override
      public void goToNextLevel() {
      }
    };
    f.add(p);
    f.setVisible(true);
    p.start();
  }
}
