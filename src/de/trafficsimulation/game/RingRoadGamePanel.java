package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import de.trafficsimulation.core.Constants;

public abstract class RingRoadGamePanel extends JPanel implements Constants {

  /**
   * Update the adaptive messages at this interval, in milliseconds.
   */
  private static final int ADAPTIVE_MESSAGE_TIMER_DELAY_MS = 100;

  private final static String FREE_FLOW_CARD = "free";
  private final static String CONGESTION_CARD = "congestion";
  private final static String JAM_CARD = "jam";

  /**
   * The density for the 'hint' to encourage the user to increase the density.
   */
  private final static int HINT_DENSITY_INVKM = 60;

  /**
   * The density of the road when the sim starts.
   */
  private final static int INITIAL_DENSITY_INVKM = 20;

  /**
   * The message cards to show, according to the MIN_SPEED_THRESHOLDS.
   */
  private static final String[] MIN_SPEED_CARDS = { JAM_CARD, CONGESTION_CARD,
      FREE_FLOW_CARD };

  /**
   * Minimum speed thresholds used to set the adaptive messages; speeds are in
   * meters per second.
   */
  private static final double[] MIN_SPEED_THRESHOLDS = { 2.0, 12.0 };
  /**
   * Don't change the current state unless the minimum speed is at least this
   * much outside of the interval for the current state. This avoids rapid
   * switching of messages due to noise.
   */
  private static final double MIN_SPEED_TOLERANCE = 1.0;
  private static final long serialVersionUID = 1L;

  /** For testing */
  public static void main(String[] args) {
    JFrame f = new JFrame("ring test");
    f.setSize(800, 600);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    RingRoadGamePanel p = new RingRoadGamePanel() {
      private static final long serialVersionUID = 1L;

      @Override
      public void onNextClicked() {
      }

      @Override
      public void onBackClicked() {
      }
    };
    f.add(p);
    f.setVisible(true);
    p.start();
  }

  private final BigSlider densitySlider;

  private final JPanel messageContainer;

  private final CardLayout messageLayout;

  private final ThresholdMachine messageMachine;

  private final Timer messageTimer;

  private final RingRoadCanvas ringRoadCanvas;

  private final MessageBubble controlPanel;

  public RingRoadGamePanel() {
    setLayout(new BorderLayout());

    //
    // title bar
    //
    GameChoicePanel titleBar = new GameChoicePanel(false, "phantom jams", true) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onBackClicked() {
        RingRoadGamePanel.this.onBackClicked();
      }

      @Override
      public void onNextClicked() {
        RingRoadGamePanel.this.onNextClicked();
      }
    };
    add(titleBar, BorderLayout.NORTH);

    //
    // float UI in the center of the ring road
    //
    ringRoadCanvas = new RingRoadCanvas();
    ringRoadCanvas.setBorder(BorderFactory.createEmptyBorder(UI.PAD, UI.PAD,
        UI.PAD, UI.PAD));
    ringRoadCanvas.setLayout(new GridBagLayout());
    add(ringRoadCanvas, BorderLayout.CENTER);

    controlPanel = new MessageBubble();
    controlPanel.setLayout(new BorderLayout());
    ringRoadCanvas.add(controlPanel);

    densitySlider = new BigSlider(DENS_MIN_INVKM, DENS_MAX_INVKM,
        INITIAL_DENSITY_INVKM, HINT_DENSITY_INVKM) {
      private static final long serialVersionUID = 1L;

      {
        setPreferredSize(new Dimension(1, 90));
        setBorder(BorderFactory.createEmptyBorder(UI.PAD, 0, 2 * UI.PAD, 0));
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
    messageContainer.setBackground(Color.WHITE);
    controlPanel.add(messageContainer, BorderLayout.CENTER);
    messageLayout = new CardLayout();
    messageContainer.setLayout(messageLayout);

    JPanel freeFlowMessage = new JPanel(new BorderLayout());
    freeFlowMessage.add(UI.makeTrafficReportLabel("Free Flow", UI.GREEN),
        BorderLayout.NORTH);
    freeFlowMessage.add(UI.makeStyledTextPane(
        "There are very few cars on the road, and everyone can\n"
            + "drive at their own speed.\n", "regular", "\n", "small",
        "Try moving the ", "regular", "purple circle", "purple",
        " to the right to add more cars..."), BorderLayout.CENTER);
    messageContainer.add(freeFlowMessage, FREE_FLOW_CARD);

    JPanel congestionMessage = new JPanel(new BorderLayout());
    congestionMessage.add(UI.makeTrafficReportLabel("Congestion", UI.AMBER),
        BorderLayout.NORTH);
    congestionMessage
        .add(
            UI.makeStyledTextPane(
                "The road is busy, and the drivers need to slow down for\nsafety.\n",
                "regular", "\n", "small", "This flow is unstable.\n",
                "regular", "\n", "small",
                "Try moving the circle to the right to add even more cars..."),
            BorderLayout.CENTER);
    messageContainer.add(congestionMessage, CONGESTION_CARD);

    JPanel jamMessage = new JPanel(new BorderLayout());
    jamMessage.add(UI.makeTrafficReportLabel("Phantom Jams", UI.RED),
        BorderLayout.NORTH);
    jamMessage.add(UI.makeStyledTextPane(
        "The road is busy, and the drivers need to slow down for\nsafety.\n",
        "regular", "\n", "small", "Instability causes stop-start driving.\n",
        "regular", "\n", "small",
        "The phantom jams move backwards while the cars\nmove forwards."),
        BorderLayout.CENTER);
    messageContainer.add(jamMessage, JAM_CARD);

    messageMachine = new ThresholdMachine(MIN_SPEED_THRESHOLDS,
        MIN_SPEED_CARDS, MIN_SPEED_TOLERANCE);

    messageTimer = new Timer(ADAPTIVE_MESSAGE_TIMER_DELAY_MS,
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            updateMessage();
          }
        });
  }

  /**
   * Called when the user presses the back button; it isn't actually visible,
   * but it can be touched.
   */
  public abstract void onBackClicked();

  /**
   * Called when the user presses the next button.
   */
  public abstract void onNextClicked();

  public void start() {
    ringRoadCanvas.start(42);
    densitySlider.setValue(INITIAL_DENSITY_INVKM);
    updateDensity();
    messageTimer.start();
    updateMessage();
  }

  public void stop() {
    ringRoadCanvas.stop();
    messageTimer.stop();
  }

  private void updateDensity() {
    double density = densitySlider.getValue();
    ringRoadCanvas.getSim().setDensity(density * 1e-3);
  }

  private void updateMessage() {
    RingRoadSim sim = ringRoadCanvas.getSim();
    if (sim == null)
      return;

    // detect new state; we give it a couple of simulated seconds to get
    // started, because otherwise get a flash of congestion initially
    String newState;
    if (sim.getTime() > 5) {
      double minSpeed = sim.getStreet().getMinSpeed();
      if (!messageMachine.observe(minSpeed)) {
        return;
      } else {
        newState = messageMachine.getState();
      }
    } else {
      newState = FREE_FLOW_CARD;
    }

    // show message
    messageLayout.show(messageContainer, newState);

    // set border color to match the traffic report
    if (newState.equals(FREE_FLOW_CARD)) {
      controlPanel.getRoundedBorder().setBorderColor(UI.GREEN);
    } else if (newState.equals(CONGESTION_CARD)) {
      controlPanel.getRoundedBorder().setBorderColor(UI.AMBER);
    } else {
      controlPanel.getRoundedBorder().setBorderColor(UI.RED);
    }

    // hide the hint when jams form
    if (newState.equals(JAM_CARD)) {
      densitySlider.setHintValue(Double.NaN);
    } else {
      densitySlider.setHintValue(HINT_DENSITY_INVKM);
    }
  }
}
