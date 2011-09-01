package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * The default (game start) panel.
 */
public class CoverPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private final RingRoadCanvas ringRoadCanvas;

  public CoverPanel() {
    super(new BorderLayout());

    ringRoadCanvas = new RingRoadCanvas();
    ringRoadCanvas.setBorder(BorderFactory.createEmptyBorder(UI.PAD, UI.PAD,
        UI.PAD, UI.PAD));
    ringRoadCanvas.setLayout(new GridBagLayout()); // float child in center
    add(ringRoadCanvas, BorderLayout.CENTER);

    //
    // intro screen panel
    //
    // note: the font metrics for the title font are wrong, so we have to
    // put some extra padding in.
    //
    RoundedButton playButton = new RoundedButton("touch to play",
        UI.TITLE_FONT.deriveFont(48f), 40) {
      private static final long serialVersionUID = 1L;

      @Override
      public void click() {
        onPlayClicked();
      }
    };
    ringRoadCanvas.add(playButton);
    
    //
    // credit
    //
    JLabel creditLabel = new JLabel(
        "based on an applet by Martin Treiber (www.mtreiber.de)");
    creditLabel.setFont(UI.BODY_FONT);
    creditLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    add(creditLabel, BorderLayout.SOUTH);
  }

  public void start() {
    ringRoadCanvas.start();
  }

  public void stop() {
    ringRoadCanvas.stop();
  }

  /**
   * Called when the user clicks the play button.
   */
  public void onPlayClicked() {

  }
}
