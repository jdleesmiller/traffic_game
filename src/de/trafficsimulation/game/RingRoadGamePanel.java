package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
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

public abstract class RingRoadGamePanel extends JPanel implements Constants {

  private static final long serialVersionUID = 1L;

  private final RingRoadCanvas ringRoadCanvas;
  
  private final JSlider densitySlider;
  
  private final GlowArrow lowDensityArrow;
  private final GlowArrow mediumDensityArrow;
  private final GlowArrow highDensityArrow;
  
  private final JPanel messageContainer;
  private final CardLayout messageLayout;
  
  private final static String LOW_DENSITY_CARD = "low";
  private final static String MEDIUM_DENSITY_CARD = "medium";
  private final static String HIGH_DENSITY_CARD = "high";
  
  private final static int PAD = 10; // px
  
  /**
   * Location of the "low density" arrow, in vehicles/km.
   */
  private final static int LOW_DENSITY_INVKM = 20;
  
  /**
   * Location of the "medium density" arrow, in vehicles/km.
   */
  private final static int MEDIUM_DENSITY_INVKM = 40;
  
  /**
   * Location of the "high density" arrow, in vehicles/km.
   */
  private final static int HIGH_DENSITY_INVKM = DENS_MAX_INVKM;
  
  /**
   * Boundary between the low density and medium density messages, in
   * vehicles/km.
   */
  private final static int LOW_MEDIUM_DENSITY_INVKM =
    (LOW_DENSITY_INVKM + MEDIUM_DENSITY_INVKM) / 2;
  
  /**
   * Boundary between the medium density and high density messages, in
   * vehicles/km.
   */
  private final static int MEDIUM_HIGH_DENSITY_INVKM =
    (MEDIUM_DENSITY_INVKM + HIGH_DENSITY_INVKM) / 2;
  
  public RingRoadGamePanel() {
    setLayout(new BorderLayout());
    
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
    add(topPanel, BorderLayout.NORTH);
    
    JLabel titleLabel = new JLabel("Ring Road Game");
    titleLabel.setFont(titleLabel.getFont().deriveFont(20f));
    topPanel.add(Box.createHorizontalStrut(PAD));
    topPanel.add(titleLabel);
    
    JButton backButton = new JButton("< Back");
    backButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        goBack();
      }
    });
    topPanel.add(Box.createHorizontalGlue());
    topPanel.add(backButton);
    
    ringRoadCanvas = new RingRoadCanvas();
    ringRoadCanvas.setBorder(
        BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
    ringRoadCanvas.setLayout(new GridBagLayout());
    add(ringRoadCanvas, BorderLayout.CENTER);
    
    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new BorderLayout());
    ringRoadCanvas.add(controlPanel);
    
    //
    // density slider control
    //
    densitySlider = new JSlider(
        DENS_MIN_INVKM, DENS_MAX_INVKM, LOW_DENSITY_INVKM);
    densitySlider.setMajorTickSpacing(DENS_MAX_INVKM/10);
    densitySlider.setPaintTicks(true);
    Hashtable<Integer, Component> labels = new Hashtable<Integer, Component>();
    lowDensityArrow = new GlowArrow("    ");
    mediumDensityArrow = new GlowArrow("    ");
    highDensityArrow = new GlowArrow("    ");
    labels.put(LOW_DENSITY_INVKM, lowDensityArrow);
    labels.put(MEDIUM_DENSITY_INVKM, mediumDensityArrow);
    labels.put(HIGH_DENSITY_INVKM, highDensityArrow);
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
    
    JPanel lowDensityMessage = new JPanel();
    lowDensityMessage.add(new JLabel("Low Density"));
    messageContainer.add(lowDensityMessage, LOW_DENSITY_CARD);
    
    JPanel mediumDensityMessage = new JPanel();
    mediumDensityMessage.add(new JLabel("Medium Density"));
    messageContainer.add(mediumDensityMessage, MEDIUM_DENSITY_CARD);
    
    JPanel highDensityMessage = new JPanel();
    highDensityMessage.add(new JLabel("High Density"));
    messageContainer.add(highDensityMessage, HIGH_DENSITY_CARD);
    
    controlPanel.add(messageContainer, BorderLayout.SOUTH);
  }

  public void start() {
    ringRoadCanvas.start(42);
    
    densitySlider.setValue(LOW_DENSITY_INVKM);
    updateDensity();
  }

  private void updateDensity() {
    int density = densitySlider.getValue();
    ringRoadCanvas.getSim().setDensity(density * 1e-3);
    
    //
    // show the appropriate message, based on the slider value
    //
    if (density < LOW_MEDIUM_DENSITY_INVKM) {
      // low density
      lowDensityArrow.setVisible(false);
      mediumDensityArrow.setVisible(true);
      highDensityArrow.setVisible(false);
      messageLayout.show(messageContainer, LOW_DENSITY_CARD);
      
    } else if (density < MEDIUM_HIGH_DENSITY_INVKM) {
      // medium density
      lowDensityArrow.setVisible(false);
      mediumDensityArrow.setVisible(false);
      highDensityArrow.setVisible(true);
      messageLayout.show(messageContainer, MEDIUM_DENSITY_CARD);
    } else {
      // high density
      lowDensityArrow.setVisible(false);
      mediumDensityArrow.setVisible(false);
      highDensityArrow.setVisible(false);
      messageLayout.show(messageContainer, HIGH_DENSITY_CARD);
    }
  }
  
  public void stop() {
    ringRoadCanvas.stop();
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
    RingRoadGamePanel p = new RingRoadGamePanel() {
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
