package de.trafficsimulation.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

/**
 * A very simple clickable button with styled logo.
 *
 * @author User
 *
 */
public class RoundedButton extends JLabel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private static final int CORNER_RADIUS = 20; // px
  
  private static final int BORDER_WIDTH = 4; // px
  
  public RoundedButton(String text) {
    this(text, Resource.HEADER_FONT.deriveFont(18f), CORNER_RADIUS);
  }
  
  public RoundedButton(String text, Font font, int cornerRadius) {
    super(text);
    
    setBorder(new RoundedBorder(Color.BLACK, null, cornerRadius, BORDER_WIDTH));
    setFont(font);
    
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        // this gets us both clicks and drags
        click();
      }
    });
  }
  
  public void click() {
  }
}
