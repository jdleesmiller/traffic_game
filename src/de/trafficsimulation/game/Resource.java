package de.trafficsimulation.game;

import java.awt.Color;
import java.awt.Font;
import java.io.InputStream;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * Fonts, images, some general GUI settings.
 */
public class Resource {
  public static final Font TITLE_FONT =
    loadTTF("SpottyFont.ttf").deriveFont(48f);
  
  public static final Font HEADER_FONT =
    loadTTF("Transport Heavy.ttf").deriveFont(24f);
  
  public static final Font BODY_FONT =
    loadTTF("Transport Medium.ttf").deriveFont(18f);

  /**
   * The main background color.
   */
  public static final Color BACKGROUND = new Color(0x99cc00);
  
  /**
   * A lighter shade of the main background color.
   */
  public static final Color BACKGROUND_HIGHLIGHT = BACKGROUND.brighter();
  
  private static String getResourceName(String fileName) {
    return "/de/trafficsimulation/game/res/" + fileName;
  }

  private static Font loadTTF(String fileName) {
    try {
      InputStream is = Resource.class.getResourceAsStream(
          getResourceName(fileName));
      return Font.createFont(Font.TRUETYPE_FONT, is);
    } catch (Exception ex) {
      Utility.log.warning("failed to load font: " + fileName);
      return new Font("serif", Font.PLAIN, 24);
    }
  }
  
  /**
   * TODO allow multiple styles somehow... clickable links? can we use
   * JEditorPane and HTML?
   * 
   * @param pane
   * @param text
   */
  public static JTextPane makeStyledTextPane(String text) {
    JTextPane pane = new JTextPane();
    StyledDocument doc = pane.getStyledDocument();
    
    //
    // initialise regular style; maybe we could add more
    //
    Style def = StyleContext.getDefaultStyleContext().
                    getStyle(StyleContext.DEFAULT_STYLE);
    doc.addStyle("regular", def);
    
    pane.setFont(BODY_FONT);
    
    try {
      doc.insertString(doc.getLength(), text, doc.getStyle("regular"));
    } catch (BadLocationException e) {
      Utility.log.severe("failed to initialise text pane");
      e.printStackTrace();
    }
    
    pane.setEditable(false);
    
    return pane;
  }
}
