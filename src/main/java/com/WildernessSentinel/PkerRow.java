package com.WildernessSentinel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PkerRow extends JPanel {
    private static final Color BACKGROUND_DARK = new Color(30, 30, 30);
    private static final Color BACKGROUND_LIGHT = new Color(40, 40, 40);

    public PkerRow(String name, PkerInfo info, boolean alternate, Color nameColor) {
        setLayout(new BorderLayout());
        setBackground(alternate ? BACKGROUND_DARK : BACKGROUND_LIGHT);
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        setPreferredSize(new Dimension(0, 28));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(nameColor);
        add(nameLabel, BorderLayout.WEST);

        String infoText = "W" + info.getLastSeenWorld() + " | x" + info.getReportCount();
        JLabel infoLabel = new JLabel(infoText);
        infoLabel.setForeground(Color.LIGHT_GRAY);
        add(infoLabel, BorderLayout.EAST);
    }
}
