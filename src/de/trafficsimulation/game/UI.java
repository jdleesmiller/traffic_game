package de.trafficsimulation.game;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.InputStream;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * Fonts, images, some general GUI settings.
 */
public class UI {
  public static final Font TITLE_FONT =
    loadTTF("SpottyFont.ttf").deriveFont(48f);
  
  public static final Font HEADER_FONT =
    loadTTF("Transport Heavy.ttf").deriveFont(28f);
  
  public static final Font BODY_FONT =
    new Font("Sans Serif", Font.BOLD, 24);
  
  /**
   * Default spacer size, in pixels.
   */
  public static final int PAD = 10;
  
  public static final Color RED = new Color(0xcc3333);
  
  public static final Color RED_HIGHLIGHT = RED.brighter();
  
  public static final Color DARK_RED = RED.darker();
  
  public static final Color AMBER = new Color(0xffcc33);
  
  public static final Color AMBER_HIGHLIGHT = AMBER.brighter();
  
  public static final Color GREEN = new Color(0x99cc00);
  
  public static final Color GREEN_HIGHLIGHT = GREEN.brighter();
  
  public static final String EM_DASH = "\u2014";

  /**
   * The main background color.
   */
  public static final Color BACKGROUND = GREEN;
  
  /**
   * A lighter shade of the main background color.
   */
  public static final Color BACKGROUND_HIGHLIGHT = BACKGROUND.brighter();
  
  /**
   * Triad complement to the background.
   */
  public static final Color PURPLE = new Color(0x500a91);
  
  /**
   * Brighter triad complement to the background.
   */
  public static final Color PURPLE_HIGHLIGHT =
    new Color(0x500a91).brighter().brighter();
  
  private static String getResourceName(String fileName) {
    return "/de/trafficsimulation/game/res/" + fileName;
  }

  private static Font loadTTF(String fileName) {
    try {
      InputStream is = UI.class.getResourceAsStream(
          getResourceName(fileName));
      return Font.createFont(Font.TRUETYPE_FONT, is);
    } catch (Exception ex) {
      Utility.log.warning("failed to load font: " + fileName);
      return new Font("serif", Font.PLAIN, 24);
    }
  }
  
  public static JPanel makeTrafficReportLabel(String report, Color color) {
    JPanel panel = new JPanel(); 
    panel.setBackground(Color.WHITE);
    panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    
    JLabel titleLabel = new JLabel("Traffic Report: ");
    titleLabel.setFont(HEADER_FONT);
    
    JLabel reportLabel = new JLabel(report);
    reportLabel.setFont(HEADER_FONT);
    reportLabel.setForeground(color);
    
    panel.add(titleLabel);
    panel.add(reportLabel);
    return panel;
  }
  
  /**
   * TODO allow multiple styles somehow... clickable links? can we use
   * JEditorPane and HTML?
   * 
   * @param pane
   * @param args text-style pairs
   */
  public static JTextPane makeStyledTextPane(String... args) {
    JTextPane pane = new JTextPane();
    pane.setFont(BODY_FONT);
    
    StyledDocument doc = pane.getStyledDocument();
    
    //
    // initialise regular style; maybe we could add more
    //
    Style def = StyleContext.getDefaultStyleContext().
                    getStyle(StyleContext.DEFAULT_STYLE);
    
    Style s = doc.addStyle("regular", def);
    StyleConstants.setFontFamily(s, UI.BODY_FONT.getFamily());
    
    // for paragraph spacing
    s = doc.addStyle("small", def);
    StyleConstants.setFontSize(s, UI.BODY_FONT.getSize() / 2);
    
    s = doc.addStyle("red", def);
    StyleConstants.setForeground(s, UI.RED);
    
    s = doc.addStyle("dark_red", def);
    StyleConstants.setBackground(s, UI.DARK_RED);
    StyleConstants.setForeground(s, Color.WHITE);
    
    s = doc.addStyle("amber", def);
    StyleConstants.setForeground(s, UI.AMBER);
    
    s = doc.addStyle("green", def);
    StyleConstants.setForeground(s, UI.GREEN);
    
    s = doc.addStyle("purple", def);
    StyleConstants.setBackground(s, UI.PURPLE);
    StyleConstants.setForeground(s, Color.WHITE);

    for (int i = 0; i < args.length; i += 2) {
      String text = args[i];
      String style = (i + 1 >= args.length) ? "regular" : args[i+1];
      try {
        doc.insertString(doc.getLength(), text, doc.getStyle(style));
      } catch (BadLocationException e) {
        Utility.log.severe("failed to initialise text pane");
        e.printStackTrace();
      }
    }
    
    pane.setEditable(false);
    
    return pane;
  }
}
