package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameChoicePanel extends JPanel {

  private static final float BIG_FONT_SIZE = 48f;

  private static final float SMALL_FONT_SIZE = 24f;
  
  private static final int BUTTON_WIDTH = 250; // px

  private static final long serialVersionUID = 1L;

  public GameChoicePanel(final boolean back, String thisText, final boolean next) {
    super(new BorderLayout());

    setBackground(Color.BLACK);

    //
    // back button
    //
    JPanel backPanel = new JPanel(new BorderLayout()) {
      private static final long serialVersionUID = 1L;
      {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(BUTTON_WIDTH, 1));
        addMouseListener(new MouseAdapter() {
          @Override
          public void mouseReleased(MouseEvent e) {
            onBackClicked();
          }
        });

        JLabel arrow = new JLabel("< back");
        arrow.setForeground(Color.WHITE);
        arrow.setFont(UI.TITLE_FONT.deriveFont(SMALL_FONT_SIZE));
        arrow.setBorder(BorderFactory.createEmptyBorder(UI.PAD, UI.PAD, UI.PAD,
            0));
        arrow.setVisible(back);
        add(arrow, BorderLayout.WEST);
      }
    };
    add(backPanel, BorderLayout.WEST);

    //
    // title
    //
    if (thisText != null) {
      JLabel thisLabel = new JLabel(thisText);
      thisLabel.setForeground(Color.WHITE);
      thisLabel.setHorizontalAlignment(JLabel.CENTER);
      thisLabel.setFont(UI.TITLE_FONT.deriveFont(BIG_FONT_SIZE));
      thisLabel.setBorder(BorderFactory.createEmptyBorder(2 * UI.PAD, 0,
          2 * UI.PAD, 0));
      add(thisLabel, BorderLayout.CENTER);
    }

    //
    // next button
    //
    JPanel nextPanel = new JPanel(new BorderLayout()) {
      private static final long serialVersionUID = 1L;
      {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(BUTTON_WIDTH, 1));
        addMouseListener(new MouseAdapter() {
          @Override
          public void mouseReleased(MouseEvent e) {
            onNextClicked();
          }
        });

        JLabel arrow = new JLabel("next >");
        arrow.setForeground(Color.WHITE);
        arrow.setFont(UI.TITLE_FONT.deriveFont(SMALL_FONT_SIZE));
        arrow.setBorder(BorderFactory.createEmptyBorder(UI.PAD, 0, UI.PAD,
            UI.PAD));
        arrow.setVisible(next);
        add(arrow, BorderLayout.EAST);
      }
    };
    add(nextPanel, BorderLayout.EAST);
  }

  public void onNextClicked() {
  }

  public void onBackClicked() {
  }
}
