package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
  
  private final static int PAD = 10; // px
  
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
    
    densitySlider = new JSlider(
        DENS_MIN_INVKM, DENS_MAX_INVKM, DENS_INIT_INVKM);
    densitySlider.setMajorTickSpacing(DENS_MAX_INVKM/10);
    densitySlider.setPaintTicks(true);
    densitySlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        updateSimParameters();
      }
    });
    ringRoadCanvas.add(densitySlider);
  }

  public void start() {
    ringRoadCanvas.start(42);
    updateSimParameters();
  }

  private void updateSimParameters() {
    ringRoadCanvas.getSim().setDensity(densitySlider.getValue() * 1e-3);
  }
  
  public void stop() {
    ringRoadCanvas.stop();
  }
  
  /**
   * Called when the user presses the back button.
   */
  public abstract void goBack();
}
