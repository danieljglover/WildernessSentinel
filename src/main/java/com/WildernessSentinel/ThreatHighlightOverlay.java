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
import net.runelite.api.SkullIcon;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class ThreatHighlightOverlay extends Overlay {
  private final Client client;
  private final WildernessSentinelPlugin plugin;
  private final WildernessSentinelConfig config;

  @Inject
  private ThreatHighlightOverlay(Client client, WildernessSentinelPlugin plugin,
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
    if (!config.highlightThreats()) {
      return null;
    }

    for (Player player : plugin.getThreateningPlayers()) {
      renderHighlight(graphics, player);
    }

    return null;
  }

  private void renderHighlight(Graphics2D graphics, Player player) {
    Shape hull = player.getConvexHull();
    if (hull != null) {
      graphics.setColor(config.highlightColor());
      graphics.setStroke(new BasicStroke(config.highlightStroke()));
      graphics.draw(hull);
    }

    if (config.highlightLabel()) {
      String skull = player.getSkullIcon() != SkullIcon.NONE ? " (skulled)" : "";
      String label = "Lv-" + player.getCombatLevel() + skull;
      Point textLocation = player.getCanvasTextLocation(graphics, label, player.getLogicalHeight() + 40);
      if (textLocation != null) {
        graphics.setColor(Color.BLACK);
        graphics.drawString(label, textLocation.getX() + 1, textLocation.getY() + 1);
        Color textColor = config.highlightColor();
        graphics.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue()));
        graphics.drawString(label, textLocation.getX(), textLocation.getY());
      }
    }
  }
}
