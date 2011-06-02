package de.trafficsimulation.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.AbstractBorder;

/**
 * Draw a border with rounded corners.
 * 
 * The insets are currently set conservatively, at the centers of the corner 
 * arcs.
 */
public class RoundedBorder extends AbstractBorder {
  private static final long serialVersionUID = 1L;
  
  private final Color borderColor;
  private final Color innerColor;
  private final float weight;
  private final Stroke stroke;
  private final RoundRectangle2D.Float shape;
  private final int inset;

  public RoundedBorder() {
    this(Color.BLACK, null, 1f, 5f);
  }
  
  public RoundedBorder(Color borderColor, Color innerColor, float cornerRadius, float weight) {
    this.borderColor = borderColor;
    this.innerColor = innerColor;
    this.weight = weight;
    this.stroke = new BasicStroke(weight);
    this.shape = new RoundRectangle2D.Float();
    this.inset = (int)Math.ceil(cornerRadius);
    
    shape.arcwidth = cornerRadius * 2;
    shape.archeight = cornerRadius * 2;
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    
    shape.setFrame(x + weight/2, y + weight/2, w - weight, h - weight);
    
    if (innerColor != null) {
      g2.setColor(innerColor);
      g2.fill(shape); 
    }
    g2.setColor(borderColor);
    g2.setStroke(stroke);
    g2.draw(shape); 
  }

  public Insets getBorderInsets(Component c) {
    return new Insets(inset, inset, inset, inset);
  }

  public Insets getBorderInsets(Component c, Insets i) {
    i.left = i.right = i.bottom = i.top = inset;
    return i;
  }

  public boolean isBorderOpaque() {
    return true;
  }
}
