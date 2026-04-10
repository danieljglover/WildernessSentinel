package com.WildernessSentinel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class TeleportHighlightOverlay extends Overlay {
  private static final Color TELEPORT_HIGHLIGHT = new Color(0, 255, 100, 120);
  private static final Color TELEPORT_BORDER = new Color(0, 255, 100, 220);

  private final Client client;
  private final WildernessSentinelPlugin plugin;
  private final WildernessSentinelConfig config;

  @Inject
  private TeleportHighlightOverlay(Client client, WildernessSentinelPlugin plugin,
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
    if (!config.showEscapeRoute()) {
      return null;
    }

    // Only highlight when alarm is active
    if (plugin.getThreateningPlayers().isEmpty()) {
      return null;
    }

    if (client.getVarbitValue(Varbits.IN_WILDERNESS) != 1) {
      return null;
    }

    int wildernessLevel = plugin.getCurrentWildernessLevel();
    boolean teleblocked = client.getVarbitValue(4163) > 0;
    Set<Integer> teleportIds = plugin.getEscapeRouteManager().getAvailableTeleportIds(wildernessLevel, teleblocked);

    if (teleportIds.isEmpty()) {
      return null;
    }

    // Highlight matching items in inventory
    Widget inventory = client.getWidget(ComponentID.INVENTORY_CONTAINER);
    if (inventory != null) {
      highlightItems(graphics, inventory, teleportIds);
    }

    return null;
  }

  private void highlightItems(Graphics2D graphics, Widget container, Set<Integer> itemIds) {
    Widget[] children = container.getDynamicChildren();
    if (children == null) {
      return;
    }
    for (Widget child : children) {
      if (child != null && itemIds.contains(child.getItemId())) {
        Rectangle bounds = child.getBounds();
        if (bounds != null && bounds.width > 0) {
          graphics.setColor(TELEPORT_HIGHLIGHT);
          graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
          graphics.setColor(TELEPORT_BORDER);
          graphics.setStroke(new BasicStroke(2));
          graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
      }
    }
  }
}
