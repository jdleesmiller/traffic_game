package de.trafficsimulation.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    BigSlider bs = new BigSlider(0.5) {
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
  public double value;

  private Color knobColor;

  /**
   * Not null after construction.
   */
  private Ellipse2D knobPath;

  private Color rampColor;

  /**
   * Not null after construction.
   */
  private GeneralPath rampPath;
  
  public BigSlider() {
    this(0);
  }

  public BigSlider(double value) {
    // set defaults
    rampColor = getBackground().darker();
    knobColor = Color.GREEN;

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
    setValue(value);
  }
  
  private void updateKnobX(MouseEvent e) {
    double knob = e.getX() - getInsets().left - knobPath.getWidth() / 2;
    double newValue = knob / getTrackPixels();
    
    // the track can have zero pixels left if the knob is big; handle this
    if (Double.isInfinite(newValue) || Double.isNaN(newValue))
      newValue = 1;
    
    // clamp to [0, 1] to be safe
    if (newValue <= 0) // trap -0.0
      newValue = 0;
    if (newValue > 1)
      newValue = 1;
    
    setValue(newValue);
  }

  /**
   * The position of the knob as a fraction between 0 and 1, where 0 is the
   * left-most position and 1 is the right-most position.
   * 
   * @return in [0, 1]
   */
  public double getValue() {
    return value;
  }
  
  /**
   * Called when the value of the knob is changed; this method should be
   * overridden when the component is created (using an anonymous class).
   */
  public void onValueUpdated() {
    // nop
  }
  
  /**
   * The position of the knob as a fraction between 0 and 1, where 0 is the
   * left-most position and 1 is the right-most position.
   * 
   * @param value in [0, 1]
   */
  public void setValue(double value) {
    if (this.value != value) {
      this.value = value;
      onValueUpdated();
      repaint();
    }
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
    rampPath.lineTo(inner.getMaxX(), inner.getMinY());
    rampPath.closePath();

    // knob with top at top of inner rect; diameter is full inner height
    knobPath = new Ellipse2D.Double(inner.x, inner.y, inner.height,
        inner.height);
  }
  
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    g2.setColor(rampColor);
    g2.fill(rampPath);

    g2.translate(getValue() * getTrackPixels(), 0);
    g2.setColor(knobColor);
    g2.fill(knobPath);
  }
}
