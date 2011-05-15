package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.trafficsimulation.core.Constants;

public class RingRoadGamePanel extends JPanel implements Constants {

  private static final long serialVersionUID = 1L;

  private final RingRoadCanvas ringRoadCanvas;
  
  private final JSlider densitySlider;
  
  private final static int PAD = 10; // px
  
  public RingRoadGamePanel() {
    setLayout(new BorderLayout());
    
    ringRoadCanvas = new RingRoadCanvas();
    ringRoadCanvas.setBorder(
        BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
    ringRoadCanvas.setLayout(new GridBagLayout());
    add(ringRoadCanvas, BorderLayout.CENTER);
    
    densitySlider = new JSlider(
        DENS_MIN_INVKM, DENS_MAX_INVKM, DENS_INIT_INVKM);
    densitySlider.setMajorTickSpacing(DENS_MAX_INVKM/8);
    densitySlider.setPaintLabels(true);
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
    ringRoadCanvas.start();
    updateSimParameters();
  }

  private void updateSimParameters() {
    ringRoadCanvas.getSim().setDensity(densitySlider.getValue() * 1e-3);
  }
  
  public void stop() {
    ringRoadCanvas.stop();
  }

}
