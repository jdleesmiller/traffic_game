package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.trafficsimulation.core.Constants;

public abstract class RingRoadGamePanel extends JPanel implements Constants {

  private static final long serialVersionUID = 1L;

  private final RingRoadCanvas ringRoadCanvas;
  
  private final JSlider densitySlider;
  
  private final JPanel uiContainer;
  private final CardLayout uiLayout;
  
  private final GlowArrow mediumDensityArrow;
  
  private final JPanel messageContainer;
  private final CardLayout messageLayout;
  
  private final static String INTRO_CARD = "intro";
  private final static String CONTROL_CARD = "control";
  
  private final static String LOW_DENSITY_CARD = "low";
  private final static String MEDIUM_DENSITY_CARD = "medium";
  private final static String HIGH_DENSITY_CARD = "high";
  
  private final static int PAD = 20; // px
  
  /**
   * Location of the "low density" arrow, in vehicles/km.
   */
  private final static int LOW_DENSITY_INVKM = 20;
  
  /**
   * Location of the "medium density" arrow, in vehicles/km.
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
  
  public RingRoadGamePanel() {
    setLayout(new BorderLayout());
    
    //
    // float UI in the center of the ring road
    //
    ringRoadCanvas = new RingRoadCanvas();
    ringRoadCanvas.setBorder(
        BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
    ringRoadCanvas.setLayout(new GridBagLayout());
    add(ringRoadCanvas, BorderLayout.CENTER);
    
    uiContainer = new JPanel();
    uiLayout = new CardLayout();
    uiContainer.setLayout(uiLayout);
    ringRoadCanvas.add(uiContainer);
    
    // 
    // intro screen panel
    //
    // note: the font metrics for the title font are wrong, so we have to
    // put some extra padding in.
    //
    JPanel introMessage = new JPanel();
    introMessage.setLayout(new GridBagLayout()); // float center
    JLabel playButton = new RoundedButton("touch to play",
        Resource.TITLE_FONT.deriveFont(48f), 40) {
      private static final long serialVersionUID = 1L;
      @Override
      public void click() {
        beginGame();
      }
    };
    introMessage.add(playButton);
    uiContainer.add(introMessage, INTRO_CARD);
    
    //
    // density game panel
    //
    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new BorderLayout());
    uiContainer.add(controlPanel, CONTROL_CARD);
    
    //
    // title
    //
    JLabel titleLabel = new JLabel("ring road");
    titleLabel.setHorizontalAlignment(JLabel.CENTER);
    titleLabel.setFont(Resource.TITLE_FONT.deriveFont(48f));
    titleLabel.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
    controlPanel.add(titleLabel, BorderLayout.NORTH);
    
    //
    // density slider control
    //
    densitySlider = new JSlider(
        DENS_MIN_INVKM, DENS_MAX_INVKM, LOW_DENSITY_INVKM);
    densitySlider.setMajorTickSpacing(DENS_MAX_INVKM/10);
    densitySlider.setPaintTicks(true);
    densitySlider.putClientProperty("JComponent.sizeVariant", "large");
    Hashtable<Integer, Component> labels = new Hashtable<Integer, Component>();
    mediumDensityArrow = makeArrow(labels, MEDIUM_DENSITY_INVKM);
    densitySlider.setLabelTable(labels);
    densitySlider.setPaintLabels(true);
    densitySlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        updateDensity();
      }
    });
    controlPanel.add(densitySlider, BorderLayout.CENTER);
    
    //
    // message panels
    //
    messageContainer = new JPanel();
    messageLayout = new CardLayout();
    messageContainer.setLayout(messageLayout);
        
    JPanel lowDensityMessage = new MessageBubble();
    lowDensityMessage.add(Resource.makeStyledTextPane(
        "When there are not many cars, traffic flows freely.\n" +
        "Try dragging the slider to add more cars..."), BorderLayout.CENTER);
    messageContainer.add(lowDensityMessage, LOW_DENSITY_CARD);
    
    JPanel mediumDensityMessage = new MessageBubble();
    mediumDensityMessage.add(Resource.makeStyledTextPane(
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
    
    JPanel highDensityMessage = new MessageBubble();
    highDensityMessage.add(Resource.makeStyledTextPane(
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
  }

  public void start() {
    ringRoadCanvas.start(42);
    densitySlider.setValue(LOW_DENSITY_INVKM);
    updateDensity();
    messageLayout.show(messageContainer, INTRO_CARD);
  }
  
  public void beginGame() {
    uiLayout.show(uiContainer, CONTROL_CARD);
  }

  private void updateDensity() {
    int density = densitySlider.getValue();
    ringRoadCanvas.getSim().setDensity(density * 1e-3);
    
    //
    // show the appropriate message, based on the slider value
    //
    if (density < LOW_MEDIUM_DENSITY_INVKM) {
      // low density
      mediumDensityArrow.setVisible(true);
      messageLayout.show(messageContainer, LOW_DENSITY_CARD);
    } else if (density < MEDIUM_HIGH_DENSITY_INVKM) {
      // medium density
      mediumDensityArrow.setVisible(false);
      messageLayout.show(messageContainer, MEDIUM_DENSITY_CARD);
    } else {
      // high density
      mediumDensityArrow.setVisible(false);
      messageLayout.show(messageContainer, HIGH_DENSITY_CARD);
    }
  }
  
  private GlowArrow makeArrow(Hashtable<Integer, Component> labels, int density)
  {
    GlowArrow arrow = new GlowArrow("    ", new Color(0x9762c8));
    arrow.setFont(arrow.getFont().deriveFont(24f));
    labels.put(density, arrow);
    return arrow;
  }
  
  public void stop() {
    ringRoadCanvas.stop();
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
    p.beginGame();
  }
}
