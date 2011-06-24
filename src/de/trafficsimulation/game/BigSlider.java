package de.trafficsimulation.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A slider with a larger touchable area and some other extras that help us to
 * guide the user to particular values. This control works only with touch (or
 * the mouse) -- it doesn't do anything with keyboard events.
 * 
 * Also note that it won't work properly if it is taller than it is wide.
 */
public class BigSlider extends JPanel {
  
  private static final int DEFAULT_PREFERRED_HEIGHT = 60; // px

  private static final long serialVersionUID = 1L;

  /**
   * For testing.
   * 
   * @param args
   */
  public static void main(String[] args) {
    JFrame f = new JFrame("slider test");
    f.setSize(600, 100);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    BigSlider bs = new BigSlider(5,10,7,9) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onValueUpdated() {
        System.out.println(getValue());
      }
    };
    bs.setBorder(BorderFactory.createLineBorder(Color.BLACK, 10));
    f.add(bs);
    f.setVisible(true);
  }

  /**
   * Fraction in [0, 1].
   */
  private double fraction;
  
  private Color hintColor;

  /**
   * Fraction in [0, 1] where the 'hint' marker is displayed; may be NaN, in
   * which case the hint marker is not shown
   */
  private double hintFraction;
  
  private Stroke hintStroke;

  private Color knobColor;
  
  /**
   * Not null after construction.
   */
  private Ellipse2D knobPath;
  
  private double maxValue;
  
  private double minValue;
  
  private Color rampColor;

  /**
   * Not null after construction.
   */
  private GeneralPath rampPath;

  public BigSlider() {
    this(0, 1, 0);
  }

  public BigSlider(double minValue, double maxValue, double value) {
    this(minValue, maxValue, value, Double.NaN);
  }

  public BigSlider(double minValue, double maxValue, double value,
      double hintValue) {
    // set defaults
    rampColor = getBackground().darker();
    knobColor = getForeground().brighter();
    hintColor = knobColor.brighter();
    hintStroke = new BasicStroke(2f, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER, 10f, new float[] {3f}, 0f);
    
    setPreferredSize(new Dimension(4*DEFAULT_PREFERRED_HEIGHT, 
        DEFAULT_PREFERRED_HEIGHT));

    // need to know when we're resized
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        handleResize();
        repaint();
      }
    });

    // handle mouse / touch events
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        updateKnobX(e);
      }
    });

    addMouseMotionListener(new MouseAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        updateKnobX(e);
      }
    });

    // call once to initialise
    handleResize();
    setRange(minValue, maxValue);
    setHintValue(hintValue);
    this.fraction = getFractionForValue(value);
  }
  
  public Color getHintColor() {
    return hintColor;
  }
  
  public Color getKnobColor() {
    return knobColor;
  }
  
  /**
   * 
   * @return strictly larger than minValue
   */
  public double getMaxValue() {
    return maxValue;
  }

  /**
   * 
   * @return strictly smaller than maxValue
   */
  public double getMinValue() {
    return minValue;
  }
  
  public Color getRampColor() {
    return rampColor;
  }
  
  /**
   * The position of the knob as a fraction between 0 and 1, where 0 is the
   * left-most position and 1 is the right-most position.
   * 
   * @return in [getMinValue(), getMaxValue()]
   */
  public double getValue() {
    return getMinValue() + fraction * (getMaxValue() - getMinValue());
  }
  
  /**
   * Called when the value of the knob is changed; this method should be
   * overridden when the component is created (using an anonymous class).
   */
  public void onValueUpdated() {
    // nop
  }
  
  public void setHintColor(Color hintColor) {
    this.hintColor = hintColor;
  }
  
  /**
   * The value at which the 'hint' marker is shown.
   * 
   * @param hintValue pass NaN for no hint
   */
  public void setHintValue(double hintValue) {
    this.hintFraction = getFractionForValue(hintValue);
  }
  
  public void setKnobColor(Color knobColor) {
    this.knobColor = knobColor;
  }
  
  public void setRampColor(Color rampColor) {
    this.rampColor = rampColor;
  }

  /**
   * Set the min and max values. Note that this may change the value returned
   * by getValue().
   * 
   * @param minValue strictly smaller than maxValue
   * @param maxValue strictly larger than minValue
   */
  public void setRange(double minValue, double maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
  }
  
  /**
   * The value at which the knob is shown.
   * 
   * @param value in [getMinValue(), getMaxValue()]; clamped otherwise
   */
  public void setValue(double value) {
    setFraction(getFractionForValue(value));
  }

  private double getFractionForValue(double value) {
    return (value - getMinValue()) / (getMaxValue() - getMinValue());
  }

  /**
   * The number of pixels that we can move the knob over, after subtracting
   * insets (borders) and while keeping whole knob visible
   * 
   * @return may be zero or negative (knob too big for track)
   */
  private double getTrackPixels() {
    return rampPath.getBounds2D().getWidth() -
      knobPath.getBounds2D().getWidth();
  }

  private void handleResize() {
    // get the inner area, without insets (borders)
    Insets insets = getInsets();
    Rectangle inner = new Rectangle();
    inner.x = insets.left;
    inner.y = insets.top;
    inner.width = getWidth() - insets.left - insets.right;
    inner.height = getHeight() - insets.top - insets.bottom;

    // ramp from bottom left to top right
    rampPath = new GeneralPath();
    rampPath.moveTo(inner.getMinX(), inner.getMaxY());
    rampPath.lineTo(inner.getMaxX(), inner.getMaxY());
    rampPath.lineTo(inner.getMaxX(), inner.getCenterY());
    rampPath.closePath();

    // knob with top at top of inner rect; diameter is full inner height
    knobPath = new Ellipse2D.Double(inner.x, inner.y, inner.height,
        inner.height);
  }

  /**
   * 
   * @param fraction in [0, 1]; otherwise clamped
   */
  private void setFraction(double fraction) {
    // the track can have zero pixels left if the knob is big; handle this
    if (Double.isInfinite(fraction) || Double.isNaN(fraction))
      fraction = 1;
    
    // clamp to [0, 1] to be safe
    if (fraction <= 0) // trap -0.0
      fraction = 0;
    if (fraction > 1)
      fraction = 1;
    
    if (this.fraction != fraction) {
      this.fraction = fraction;
      onValueUpdated();
      repaint();
    }
  }

  private void updateKnobX(MouseEvent e) {
    double knob = e.getX() - getInsets().left - knobPath.getWidth() / 2;
    setFraction(knob / getTrackPixels());
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    // paint ramp
    g2.setColor(rampColor);
    g2.fill(rampPath);
    
    // paint hint marker
    if (!Double.isNaN(hintFraction)) {
      AffineTransform tx = g2.getTransform();
      g2.translate(hintFraction * getTrackPixels(), 0);
      g2.setColor(getHintColor());
      g2.setStroke(hintStroke);
      g2.draw(knobPath);
      g2.setTransform(tx);
    }

    // paint knob
    g2.translate(fraction * getTrackPixels(), 0);
    g2.setColor(getKnobColor());
    g2.fill(knobPath);
  }
}
