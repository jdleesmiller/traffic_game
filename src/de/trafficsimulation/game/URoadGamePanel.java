package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.trafficsimulation.core.Constants;

public abstract class URoadGamePanel extends JPanel implements Constants {
  private static final long serialVersionUID = 1L;
  
  private final JSlider flowInSlider;
  
  private final List<URoadCanvas> simCanvases;
  
  private final static int PAD = 10; // px
  
  private final static int SIM_PAD = 5; // px
  
  private final static int SIM_ROWS = 2;
  private final static int SIM_COLS = 1;
  
  private final static int TIME_STEPS_PER_FRAME = 8;
  
  public URoadGamePanel() {
    setLayout(new BorderLayout());
    
    //
    // top panel
    //
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
    add(topPanel, BorderLayout.NORTH);
    
    JLabel titleLabel = new JLabel("Flow Game");
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
    
    //
    // left panel (game controls)
    //
    JPanel controlPanel = new JPanel();
    add(controlPanel, BorderLayout.WEST);
    
    flowInSlider = new JSlider(0, Q_MAX, Q_INIT2);
    flowInSlider.setMajorTickSpacing(Q_MAX/10);
    flowInSlider.setPaintLabels(true);
    flowInSlider.setPaintTicks(true);
    flowInSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        updateSimParameters();
      }
    });
    controlPanel.add(flowInSlider);
    
    //
    // right panel (simulation grid)
    //
    
    JPanel simPanel = new JPanel(new GridLayout(SIM_ROWS, SIM_COLS));
    simCanvases = new ArrayList<URoadCanvas>();
    for (int i = 0; i < SIM_ROWS; ++i) {
      for (int j = 0; j < SIM_COLS; ++j) {
        URoadCanvas canvas = new URoadCanvas();
        canvas.setTimeStepsPerFrame(TIME_STEPS_PER_FRAME);
        simPanel.add(canvas);
        simCanvases.add(canvas);
      }
    }
    add(simPanel, BorderLayout.CENTER);
  }
  
  public void start() {
    for (URoadCanvas canvas : simCanvases) {
      canvas.start();
    }
    updateSimParameters();
  }

  private void updateSimParameters() {
    for (URoadCanvas canvas : simCanvases) {
      canvas.getSim().qIn = flowInSlider.getValue() / 3600.;
    }
  }
  
  public void stop() {
    for (URoadCanvas canvas : simCanvases) {
      canvas.stop();
    }
  }
  
  /**
   * Called when the user presses the back button.
   */
  public abstract void goBack();
}
