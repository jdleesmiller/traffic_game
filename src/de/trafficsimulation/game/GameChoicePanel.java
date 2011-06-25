package de.trafficsimulation.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameChoicePanel extends JPanel {

  private static final float BIG_FONT_SIZE = 48f;

  private static final long serialVersionUID = 1L;

  public GameChoicePanel(boolean back, String thisText, boolean next) {
    super(new GridLayout(1, 3, UI.PAD, 0));
    GridBagConstraints c = new GridBagConstraints();

    setBackground(Color.BLACK);
    setBorder(BorderFactory.createEmptyBorder(UI.PAD, UI.PAD, UI.PAD, UI.PAD));

    //
    // back button
    //
    c.gridx = 0;
    c.weightx = 0.2;
    if (back) {
      JPanel backPanel = new JPanel(new BorderLayout()) {
        private static final long serialVersionUID = 1L;
        {
          setBackground(Color.BLACK);
          addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
              onBackClicked();
            }
          });

          JLabel arrow = new JLabel("<back");
          arrow.setForeground(Color.WHITE);
          arrow.setFont(UI.TITLE_FONT.deriveFont(BIG_FONT_SIZE));
          arrow
              .setBorder(BorderFactory.createEmptyBorder(UI.PAD, 0, UI.PAD, 0));
          add(arrow, BorderLayout.WEST);
        }
      };
      add(backPanel, c);
    } else {
      add(new JLabel(""), c);
    }

    //
    // title
    //
    if (thisText != null) {
      JLabel thisLabel = new JLabel(thisText);
      thisLabel.setForeground(Color.WHITE);
      thisLabel.setHorizontalAlignment(JLabel.CENTER);
      thisLabel.setFont(UI.TITLE_FONT.deriveFont(BIG_FONT_SIZE));
      thisLabel
          .setBorder(BorderFactory.createEmptyBorder(UI.PAD, 0, UI.PAD, 0));
      c.gridx = 1;
      c.weightx = 0.6;
      add(thisLabel, c);
    }

    //
    // next button
    //
    c.gridx = 2;
    c.weightx = 0.2;
    if (next) {
      JPanel nextPanel = new JPanel(new BorderLayout()) {
        private static final long serialVersionUID = 1L;
        {
          setBackground(Color.BLACK);
          addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
              onNextClicked();
            }
          });

          JLabel arrow = new JLabel("next>");
          arrow.setForeground(Color.WHITE);
          arrow.setFont(UI.TITLE_FONT.deriveFont(BIG_FONT_SIZE));
          arrow
              .setBorder(BorderFactory.createEmptyBorder(UI.PAD, 0, UI.PAD, 0));
          add(arrow, BorderLayout.EAST);
        }
      };
      add(nextPanel, c);
    } else {
      add(new JLabel(""), c);
    }
  }

  public void onNextClicked() {
  }
  
  public void onBackClicked() {
  }
}
