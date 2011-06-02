package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;

/**
 * Just a decorated panel.
 */
public class MessageBubble extends JPanel {
  
  private static final long serialVersionUID = 1L;
  
  private final static int CORNER_RADIUS = 20; // px
  
  private final static int BORDER_WIDTH = 10; // px
  
  public MessageBubble() {
    super(new BorderLayout());
    
    setOpaque(true);
    setBorder(new RoundedBorder(Resource.BACKGROUND_HIGHLIGHT, Color.WHITE,
        CORNER_RADIUS, BORDER_WIDTH));
  }
}
