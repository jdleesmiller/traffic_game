package de.trafficsimulation.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import javax.swing.JLabel;

/**
 * Draw an arrow behind the text for this label. This doesn't do anything
 * clever to try to fit the arrow to the text; the arrow just stretches to
 * fit the smallest dimension of the label, and it is centered in the other
 * dimension.
 * 
 * TODO it should glow or something
 */
public class GlowArrow extends JLabel {

  private static final long serialVersionUID = 1L;
  
  /**
   * Arrow at the origin with the tip at x = 0 and y = -1.
   */
  private GeneralPath arrow;
  
  /**
   * Transform from the coordinates in which arrow is specified to display
   * coordinates. 
   */
  private AffineTransform transform;
  
  public GlowArrow(String text) {
    super(text);
    arrow = new GeneralPath();
    arrow.moveTo( 0.0f, -1.0f);
    arrow.lineTo( 1.0f,  0.0f);
    arrow.lineTo( 0.5f,  0.0f);
    arrow.lineTo( 0.5f,  1.0f);
    arrow.lineTo(-0.5f,  1.0f);
    arrow.lineTo(-0.5f,  0.0f);
    arrow.lineTo(-1.0f,  0.0f);
    arrow.closePath();
    
    // need to rescale when we're resized
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        handleResize();
      }
    });    
    
    // call once to initialise
    handleResize();
  }

  private void handleResize() {
    transform = new AffineTransform();
    Utility.transformToFit(this, arrow.getBounds2D(), transform);
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create(); // must make a copy here
    
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    
    if (this.isVisible()) {
      g2.setColor(Color.red);
      g2.transform(transform);
      g2.fill(arrow);
    }
    
    super.paintComponent(g);
  }
}
