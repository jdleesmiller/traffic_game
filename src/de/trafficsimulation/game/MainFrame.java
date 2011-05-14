package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

public class MainFrame extends JFrame implements Constants {

  private static final long serialVersionUID = 1L;

  /**
   * Padding between GUI elements, in pixels.
   */
  private static final int PAD = 5;
  
  private static final String INTRO_CARD = "intro";
  private static final String RING_ROAD_GAME_CARD = "ring_road_game";
 
  private final CardLayout cardLayout;
  private final IntroPanel introPanel;
  private final RingRoadGamePanel ringRoadGamePanel;

  /*
  private final JPanel canvasPanel;
  private final CardLayout canvasCards;
  private final RingRoadCanvas ringRoadCanvas;
  private final URoadCanvas uRoadCanvas;

  private final JButton ringRoadButton;
  private final JButton onRampButton;
  
  // for the OnRamp scenario 
  private final JPanel onRampControls;
  private final JLabel numCarsOutLabel;
  private final JSlider flowInSlider;
  */

  public MainFrame() {
    super("Traffic Flow Game");

    cardLayout = new CardLayout();
    setLayout(cardLayout);
    
    //
    // game intro card
    //
    introPanel = new IntroPanel() {
      private static final long serialVersionUID = 1L;

      @Override
      public void playRingRoadGame() {
        showRingRoadGame();
      }
      
      @Override
      public void playFlowGame() {
        // TODO Auto-generated method stub
      }
    }; 
    add(introPanel, INTRO_CARD);
    
    ringRoadGamePanel = new RingRoadGamePanel();
    add(ringRoadGamePanel, RING_ROAD_GAME_CARD);
    /*
    
    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
    add(controlPanel, BorderLayout.SOUTH);
    controlPanel.add(Box.createHorizontalStrut(PAD));
    controlPanel.add(new JLabel("scenario:"));
    
    //
    // Sim canvases that handle the animation.
    //
    canvasCards = new CardLayout();
    canvasPanel = new JPanel();
    canvasPanel.setLayout(canvasCards);
    add(canvasPanel, BorderLayout.CENTER);
    
    ringRoadCanvas = new RingRoadCanvas();
    canvasPanel.add(ringRoadCanvas, "ringRoad");
    
    uRoadCanvas = new URoadCanvas() {
      private static final long serialVersionUID = 1L;

      @Override
      public void tick() {
        super.tick();
        numCarsOutLabel.setText(String.format("%d",
            getSim().getStreet().getNumCarsOut()));
      }
    };
    canvasPanel.add(uRoadCanvas, "onRamp");
    
    //
    // Scenario selector buttons.
    //
    ringRoadButton = new JButton("ring road");
    ringRoadButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        loadRingRoadScenario();
        toggleControlVisibility();
      }
    });
    controlPanel.add(Box.createHorizontalStrut(PAD));
    controlPanel.add(ringRoadButton);
    
    onRampButton = new JButton("on ramp");
    onRampButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        loadOnRampScenario();
        toggleControlVisibility();
      }
    });
    controlPanel.add(Box.createHorizontalStrut(PAD));
    controlPanel.add(onRampButton);
    
    //
    // Controls for the on ramp scenario.
    //
    onRampControls = new JPanel();
    onRampControls.setLayout(new BoxLayout(onRampControls,
        BoxLayout.LINE_AXIS));
    controlPanel.add(Box.createHorizontalStrut(PAD));
    controlPanel.add(onRampControls);
    
    flowInSlider = new JSlider(0, Q_MAX, Q_INIT2);
    flowInSlider.setMajorTickSpacing(Q_MAX/10);
    flowInSlider.setPaintLabels(true);
    flowInSlider.setPaintTicks(true);
    flowInSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (uRoadCanvas != null) {
          uRoadCanvas.getSim().qIn = flowInSlider.getValue() / 3600.;
        }
      }
    });
    onRampControls.add(new JLabel("vehicle flow in: "));
    onRampControls.add(flowInSlider);
    onRampControls.add(Box.createHorizontalStrut(PAD));
    onRampControls.add(new JLabel("num vehicles out: "));
    numCarsOutLabel = new JLabel("0");
    onRampControls.add(numCarsOutLabel);
    onRampControls.add(Box.createHorizontalStrut(PAD));
    
    loadRingRoadScenario();
    */
  }
  
  private void stopAll() {
    introPanel.stop();
    ringRoadGamePanel.stop();
  }
  
  private void showIntro() {
    stopAll();
    cardLayout.show(getContentPane(), INTRO_CARD);
    introPanel.start();
  }
  
  private void showRingRoadGame() {
    stopAll();
    cardLayout.show(getContentPane(), RING_ROAD_GAME_CARD);
    ringRoadGamePanel.start();
  }
  
  /**
   * Show only the controls for the selected scenario.
  protected void toggleControlVisibility() {
    onRampControls.setVisible(uRoadCanvas.isVisible());
  }
   */
  
  /**
   * Load the ring road scenario. Parameters are set to the defaults from the
   * applet.
  public void loadRingRoadScenario() {
    ringRoadCanvas.stop();
    uRoadCanvas.stop();
    canvasCards.show(canvasPanel, "ringRoad");
    toggleControlVisibility();
    ringRoadCanvas.start();
  }
   */

  /**
   * Load the on ramp scenario. Parameters are set to the defaults from the
   * applet, except for the in flow, which is set according to the appropriate
   * slider control.
  public void loadOnRampScenario() {
    ringRoadCanvas.stop();
    uRoadCanvas.stop();
    canvasCards.show(canvasPanel, "onRamp");
    toggleControlVisibility();
    uRoadCanvas.start();
    uRoadCanvas.getSim().qIn = flowInSlider.getValue() / 3600.;
  }
   */

  /**
   * Application entry point.
   * 
   * @param args ignored
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        MainFrame f = new MainFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(640, 480);
        f.setVisible(true);
        f.setExtendedState(f.getExtendedState() | MAXIMIZED_BOTH);
        f.showIntro();
      }
    });
  }
}
