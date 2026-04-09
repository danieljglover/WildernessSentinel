package com.WildernessSentinel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class PkerHighlightOverlay extends Overlay {
  private final Client client;
  private final WildernessSentinelPlugin plugin;
  private final WildernessSentinelConfig config;

  @Inject
  private PkerHighlightOverlay(Client client, WildernessSentinelPlugin plugin,
      WildernessSentinelConfig config) {
    this.client = client;
    this.plugin = plugin;
    this.config = config;
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_SCENE);
    setPriority(OverlayPriority.HIGH);
  }

  @Override
  public Dimension render(Graphics2D graphics) {
    if (!config.enablePkerHighlight()) return null;

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
        renderPkerHighlight(graphics, player, pkerInfo);
      }
    }

    return null;
  }

  private void renderPkerHighlight(Graphics2D graphics, Player player, PkerInfo pkerInfo) {
    Shape hull = player.getConvexHull();
    if (hull != null) {
      graphics.setColor(PkerTierColors.getHullColor(pkerInfo.getReportCount(), config));
      graphics.setStroke(new BasicStroke(config.pkerHighlightStroke()));
      graphics.draw(hull);
    }

    if (config.enablePkerLabel()) {
      String label = "PKer (Lv-" + player.getCombatLevel() + " | W" + pkerInfo.getLastSeenWorld() + " | x" + pkerInfo.getReportCount() + ")";
      Point textLocation = player.getCanvasTextLocation(graphics, label, player.getLogicalHeight() + 40);
      if (textLocation != null) {
        graphics.setColor(Color.BLACK);
        graphics.drawString(label, textLocation.getX() + 1, textLocation.getY() + 1);
        graphics.setColor(PkerTierColors.getTextColor(pkerInfo.getReportCount(), config));
        graphics.drawString(label, textLocation.getX(), textLocation.getY());
      }
    }
  }
}
