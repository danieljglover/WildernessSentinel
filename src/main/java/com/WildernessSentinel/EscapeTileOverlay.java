package com.WildernessSentinel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
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

public class EscapeTileOverlay extends Overlay {
  private static final Color PATH_COLOR = new Color(0, 255, 100, 100);
  private static final Color PATH_BORDER_COLOR = new Color(0, 255, 100, 200);

  private final Client client;
  private final WildernessSentinelPlugin plugin;
  private final WildernessSentinelConfig config;

  @Inject
  private EscapeTileOverlay(Client client, WildernessSentinelPlugin plugin,
      WildernessSentinelConfig config) {
    this.client = client;
    this.plugin = plugin;
    this.config = config;
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_SCENE);
    setPriority(OverlayPriority.LOW);
  }

  @Override
  public Dimension render(Graphics2D graphics) {
    if (!config.showEscapeRoute()) {
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

    int plane = client.getLocalPlayer().getWorldLocation().getPlane();

    // Draw path tiles
    graphics.setStroke(new BasicStroke(1));
    Point prevCenter = null;
    for (WorldPoint point : path) {
      if (point.getPlane() != plane) {
        continue;
      }
      LocalPoint lp = LocalPoint.fromWorld(client.getTopLevelWorldView(), point);
      if (lp == null) {
        continue;
      }
      Polygon poly = Perspective.getCanvasTilePoly(client, lp);
      if (poly == null) {
        continue;
      }
      graphics.setColor(PATH_COLOR);
      graphics.fillPolygon(poly);
      graphics.setColor(PATH_BORDER_COLOR);
      graphics.drawPolygon(poly);

      // Draw connecting line between consecutive tiles
      Point center = getCenterPoint(poly);
      if (prevCenter != null && center != null) {
        graphics.setColor(PATH_BORDER_COLOR);
        graphics.setStroke(new BasicStroke(2));
        graphics.drawLine(prevCenter.getX(), prevCenter.getY(), center.getX(), center.getY());
        graphics.setStroke(new BasicStroke(1));
      }
      prevCenter = center;
    }

    return null;
  }

  private Point getCenterPoint(Polygon poly) {
    int cx = 0;
    int cy = 0;
    for (int i = 0; i < poly.npoints; i++) {
      cx += poly.xpoints[i];
      cy += poly.ypoints[i];
    }
    return new Point(cx / poly.npoints, cy / poly.npoints);
  }
}
