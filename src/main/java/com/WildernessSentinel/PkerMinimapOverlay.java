package com.WildernessSentinel;

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

public class PkerMinimapOverlay extends Overlay {
  private final Client client;
  private final WildernessSentinelPlugin plugin;
  private final WildernessSentinelConfig config;

  @Inject
  private PkerMinimapOverlay(Client client, WildernessSentinelPlugin plugin,
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
    if (!config.enableMinimapDots()) return null;

    if (plugin.getKnownPkers().isEmpty()) {
      return null;
    }

    for (Player player : client.getTopLevelWorldView().players()) {
      if (player == null || player == client.getLocalPlayer() || player.getName() == null) {
        continue;
      }

      String nameLower = player.getName().toLowerCase();
      PkerInfo pkerInfo = plugin.getKnownPkers().get(nameLower);
      if (pkerInfo != null) {
        Point minimapLocation = player.getMinimapLocation();
        if (minimapLocation != null) {
          int dotSize = config.minimapDotSize();
          graphics.setColor(PkerTierColors.getDotColor(pkerInfo.getReportCount(), config));
          graphics.fillOval(
              minimapLocation.getX() - dotSize / 2,
              minimapLocation.getY() - dotSize / 2,
              dotSize,
              dotSize);
        }
      }
    }

    return null;
  }
}
