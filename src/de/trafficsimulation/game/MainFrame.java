package de.trafficsimulation.game;

import java.awt.AWTEvent;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.UIManager;

import de.trafficsimulation.core.Constants;

public class MainFrame extends JFrame implements Constants {

  private static final long serialVersionUID = 1L;

  private static final String COVER_CARD = "cover";
  private static final String RING_ROAD_GAME_CARD = "ring_road_game";
  private static final String FLOW_GAME_CARD = "flow_game";
//  private static final String SPEED_GAME_CARD = "speed_game";

  /**
   * Reload the intro panel if there has not been any activity after this
   * interval, in milliseconds.
   */
  private static final int INACTIVITY_TIMEOUT_MS = 5 * 60 * 1000;

  private final CardLayout cardLayout;
  private final CoverPanel coverPanel;
  private final RingRoadGamePanel ringRoadGamePanel;
  private final URoadGamePanel flowGamePanel;
//  private final URoadSpeedGamePanel speedGamePanel;

  private final Timer inactivityTimer;

  public MainFrame(GraphicsConfiguration gc) {
    super(gc);
    
    cardLayout = new CardLayout();
    setLayout(cardLayout);

    //
    // cover panel card
    //
    coverPanel = new CoverPanel() {
      private static final long serialVersionUID = 1L;
      @Override
      public void onPlayClicked() {
        showRingRoadGame();
      }
    };
    add(coverPanel, COVER_CARD);

    //
    // ring road game card
    //
    ringRoadGamePanel = new RingRoadGamePanel() {
      private static final long serialVersionUID = 1L;

      @Override
      public void goToNextLevel() {
        showFlowGame();
      }
    };
    add(ringRoadGamePanel, RING_ROAD_GAME_CARD);

    //
    // flow game card
    //
    flowGamePanel = new URoadGamePanel() {
      private static final long serialVersionUID = 1L;

      @Override
      protected void onBackClicked() {
        showRingRoadGame();
      }

      @Override
      protected void onNextClicked() {
        showCover();
        //showSpeedGame(); DISABLED
      }
    };
    add(flowGamePanel, FLOW_GAME_CARD);

//    //
//    // speed game card
//    //
//    speedGamePanel = new URoadSpeedGamePanel() {
//      private static final long serialVersionUID = 1L;
//
//      @Override
//      protected void onBackClicked() {
//        showFlowGame();
//      }
//
//      @Override
//      protected void onNextClicked() {
//        showCover();
//      }
//    };
//    add(speedGamePanel, SPEED_GAME_CARD);

    //
    // inactivity sensing (return to intro panel if no mouse activity)
    //
    inactivityTimer = new Timer(INACTIVITY_TIMEOUT_MS, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        showCover();
      }
    });
    Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
      @Override
      public void eventDispatched(AWTEvent event) {
        inactivityTimer.restart();
      }
    }, AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK);
    inactivityTimer.start();
  }

  private void stopAll() {
    ringRoadGamePanel.stop();
    flowGamePanel.stop();
//    speedGamePanel.stop();
    coverPanel.stop();
  }
  
  private void showCover() {
    stopAll();
    cardLayout.show(getContentPane(), COVER_CARD);
    coverPanel.start();
  }

  private void showRingRoadGame() {
    stopAll();
    cardLayout.show(getContentPane(), RING_ROAD_GAME_CARD);
    ringRoadGamePanel.start();
  }

  private void showFlowGame() {
    stopAll();
    cardLayout.show(getContentPane(), FLOW_GAME_CARD);
    flowGamePanel.start();
  }

//  private void showSpeedGame() { DISABLED
//    stopAll();
//    cardLayout.show(getContentPane(), SPEED_GAME_CARD);
//    speedGamePanel.start();
//  }

  /**
   * Application entry point.
   * 
   * @param args
   *          ignored
   */
  public static void main(final String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        //setLookAndFeel();
    	UIManager.put("Panel.background", UI.BACKGROUND);
        
        int device = 0;
        if (args.length > 0) {
          device = Integer.parseInt(args[0]);
        }

        MainFrame f = createOnMonitor(device);
        f.showCover();
      }
    });
  }

  /*
  // see
  // http://www.jasperpotts.com/blog/2008/08/skinning-a-slider-with-nimbus/
  // http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/color.html
  private static void setLookAndFeel() {
    // try to find the fancy Nimbus look and feel
    String nimbusClassName = null;
    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      if ("Nimbus".equals(info.getName())) {
        nimbusClassName = info.getClassName();
        break;
      }
    }

    if (nimbusClassName != null) {
      // set up nice colors
      // red: #cc3333
      // yellow: #ffcc33
      // green: #99cc00
      // light: #c7e667
      // dark: #648500
      // triad red: #c90024
      // triad purple: #500a91
      // UIManager.put("nimbusBase", new Color(0xc7e667));
      // UIManager.put("nimbusBlueGrey", new Color(0x648500));
      UIManager.put("control", UI.BACKGROUND);
      // nimbusFocus also important

      // try to load it up
      try {
        UIManager.setLookAndFeel(nimbusClassName);
      } catch (Exception e) {
        // just give up
      }
    }
  }
  */

  /**
   * 
   * @param device non-negative; capped at the number of displays
   * @return not null
   */
  private static MainFrame createOnMonitor(int device) {
    // get device info for all screens
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();
    if (device >= gs.length)
      device = gs.length - 1;
    GraphicsDevice gd = gs[device];
    GraphicsConfiguration gc = gd.getDefaultConfiguration();
    
    // create the frame on the selected device
    MainFrame f = new MainFrame(gc);
    f.setUndecorated(true); // full screen
    f.setAlwaysOnTop(true);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
    f.setSize(800, 600);
    f.setVisible(true);
    f.setExtendedState(f.getExtendedState() | MAXIMIZED_BOTH);
    
    return f;
  }
}
