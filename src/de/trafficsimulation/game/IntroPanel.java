package de.trafficsimulation.game;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * The main game choice screen.
 */
public abstract class IntroPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  
  private final RingRoadCanvas ringRoadCanvas;
  private final URoadCanvas uRoadCanvas;
  
  private static final int PAD = 10; // px
  
  public IntroPanel() {
    // overall layout is two-up with equal proportions; we then use a grid bag
    // layout in each sim canvas to float the content into the centers
    setLayout(new GridLayout());
    
    ringRoadCanvas = new RingRoadCanvas();
    ringRoadCanvas.setLayout(new GridBagLayout());
    ringRoadCanvas.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 0, PAD, Color.BLACK),
        BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD)));
    add(ringRoadCanvas);
    
    JButton ringButton = new JButton("Play Ring Road Game");
    ringButton.setFont(ringButton.getFont().deriveFont(20f));
    ringButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        playRingRoadGame();
      }
    });
    ringRoadCanvas.add(ringButton);
    
    uRoadCanvas = new URoadCanvas();
    uRoadCanvas.setLayout(new GridBagLayout());
    uRoadCanvas.setBorder(
        BorderFactory.createMatteBorder(0, PAD, 0, 0, Color.BLACK));
    add(uRoadCanvas);
    
    JButton uButton = new JButton("Play Flow Game"); 
    uButton.setFont(ringButton.getFont().deriveFont(20f));
    uButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        playFlowGame();
      }
    });
    uRoadCanvas.add(uButton);
  }
  
  /**
   * Start the sims (eye candy).
   */
  public void start() {
    ringRoadCanvas.start(42);
    uRoadCanvas.start(42);
  }

  /**
   * Stop the sims (eye candy).
   */
  public void stop() {
    ringRoadCanvas.stop();
    uRoadCanvas.stop();
  }
  
  /**
   * Call back for when the user selects the ring road game.
   */
  public abstract void playRingRoadGame();
  
  /**
   * Call back for when the user selects the flow game.
   */
  public abstract void playFlowGame();
}
