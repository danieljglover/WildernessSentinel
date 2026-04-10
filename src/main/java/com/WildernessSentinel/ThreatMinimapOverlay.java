package com.WildernessSentinel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class ThreatMinimapOverlay extends Overlay {
  private final Client client;
  private final WildernessSentinelPlugin plugin;
  private final WildernessSentinelConfig config;

  @Inject
  private ThreatMinimapOverlay(Client client, WildernessSentinelPlugin plugin,
      WildernessSentinelConfig config) {
    this.client = client;
    this.plugin = plugin;
    this.config = config;
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_WIDGETS);
    setPriority(OverlayPriority.HIGH);
  }

  @Override
  public Dimension render(Graphics2D graphics) {
    if (!config.highlightMinimap()) {
      return null;
    }

    int dotSize = config.minimapDotSize();
    Color dotColor = config.highlightColor();
    Color opaque = new Color(dotColor.getRed(), dotColor.getGreen(), dotColor.getBlue());

    for (Player player : plugin.getThreateningPlayers()) {
      Point minimapLocation = player.getMinimapLocation();
      if (minimapLocation != null) {
        graphics.setColor(opaque);
        graphics.fillOval(
            minimapLocation.getX() - dotSize / 2,
            minimapLocation.getY() - dotSize / 2,
            dotSize,
            dotSize);
      }
    }

    return null;
  }
}
