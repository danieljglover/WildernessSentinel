package com.WildernessSentinel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class EscapeMinimapOverlay extends Overlay {
  private static final Color LINE_COLOR = new Color(0, 255, 100, 200);

  private final Client client;
  private final WildernessSentinelPlugin plugin;
  private final WildernessSentinelConfig config;

  @Inject
  private EscapeMinimapOverlay(Client client, WildernessSentinelPlugin plugin,
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
    if (!config.showEscapeArrow()) {
      return null;
    }

    EscapeOption escape = plugin.getCurrentEscape();
    if (escape == null || escape.getType() != EscapeOption.EscapeType.RUN) {
      return null;
    }

    List<WorldPoint> path = plugin.getEscapePath();
    if (path == null || path.isEmpty()) {
      return null;
    }

    graphics.setColor(LINE_COLOR);
    graphics.setStroke(new BasicStroke(2));

    Point prev = null;
    for (WorldPoint point : path) {
      LocalPoint lp = LocalPoint.fromWorld(client.getTopLevelWorldView(), point);
      if (lp == null) {
        continue;
      }
      Point minimapPoint = Perspective.localToMinimap(client, lp);
      if (minimapPoint == null) {
        continue;
      }
      if (prev != null) {
        graphics.drawLine(prev.getX(), prev.getY(), minimapPoint.getX(), minimapPoint.getY());
      }
      prev = minimapPoint;
    }

    return null;
  }
}
