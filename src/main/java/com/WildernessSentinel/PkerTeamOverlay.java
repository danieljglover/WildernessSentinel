package com.WildernessSentinel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class PkerTeamOverlay extends Overlay {
    private final Client client;
    private final WildernessSentinelPlugin plugin;
    private final WildernessSentinelConfig config;
    @Inject
    private PkerTeamOverlay(Client client, WildernessSentinelPlugin plugin,
        WildernessSentinelConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGHEST);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        int threshold = config.teamWarningThreshold();
        int count = plugin.getPkerTeamCount();
        if (threshold <= 0 || count < threshold) return null;

        String warning = "PKer TEAM (" + count + ")";
        Font teamFont = new Font("Arial", Font.BOLD, config.teamWarningFontSize());
        graphics.setFont(teamFont);
        FontMetrics fm = graphics.getFontMetrics();
        int x = (client.getCanvasWidth() - fm.stringWidth(warning)) / 2;
        int y = 60;

        graphics.setColor(Color.BLACK);
        graphics.drawString(warning, x + 2, y + 2);
        graphics.setColor(config.teamWarningColor());
        graphics.drawString(warning, x, y);

        return null;
    }
}
