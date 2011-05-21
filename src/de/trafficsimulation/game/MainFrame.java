package de.trafficsimulation.game;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import de.trafficsimulation.core.Constants;

public class MainFrame extends JFrame implements Constants {

  private static final long serialVersionUID = 1L;

  private static final String INTRO_CARD = "intro";
  private static final String RING_ROAD_GAME_CARD = "ring_road_game";
  private static final String FLOW_GAME_GAME_CARD = "flow_game";

  private final CardLayout cardLayout;
  private final IntroPanel introPanel;
  private final RingRoadGamePanel ringRoadGamePanel;
  private final URoadGamePanel flowGamePanel;

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
        showFlowGame();
      }
    }; 
    add(introPanel, INTRO_CARD);
    
    //
    // ring road game card
    //
    ringRoadGamePanel = new RingRoadGamePanel() {
      private static final long serialVersionUID = 1L;
      
      @Override
      public void goBack() {
        showIntro();
      }
    };
    add(ringRoadGamePanel, RING_ROAD_GAME_CARD);
    
    //
    // flow game card
    //
    flowGamePanel = new URoadGamePanel() {
      private static final long serialVersionUID = 1L;

      @Override
      public void goBack() {
        showIntro();
      }
    };
    add(flowGamePanel, FLOW_GAME_GAME_CARD);
    
  }
  
  private void stopAll() {
    introPanel.stop();
    ringRoadGamePanel.stop();
    flowGamePanel.stop();
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
  
  private void showFlowGame() {
    stopAll();
    cardLayout.show(getContentPane(), FLOW_GAME_GAME_CARD);
    flowGamePanel.start();
  }
  
  /**
   * Application entry point.
   * 
   * @param args ignored
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        setLookAndFeel();
        
        MainFrame f = new MainFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(640, 480);
        f.setVisible(true);
        f.setExtendedState(f.getExtendedState() | MAXIMIZED_BOTH);
        f.showIntro();
      }
    });
  }
    
    // TODO see 
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
      // red:    #cc3333
      // yellow: #ffcc33
      // green:  #99cc00
      //   light: #c7e667
      //   dark:  #648500
      //   triad red:    #c90024
      //   triad purple: #500a91
      //UIManager.put("nimbusBase", new Color(0xc7e667));
      //UIManager.put("nimbusBlueGrey", new Color(0x648500));
      //UIManager.put("control", new Color(0x99cc00));
      // nimbusFocus also important
      
      // try to load it up
      try {
        UIManager.setLookAndFeel(nimbusClassName);
      } catch (Exception e) {
        // just give up
      }
    }
  }
}
