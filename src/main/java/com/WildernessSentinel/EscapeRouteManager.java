package com.WildernessSentinel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;

public class EscapeRouteManager {

  private static final int TELEBLOCK_VARBIT = 4163;
  private static final int WILDERNESS_DITCH_Y = 3523;
  private static final WorldPoint FEROX_ENTRANCE = new WorldPoint(3141, 3627, 0);

  private static final Set<Integer> LEVEL_30_TELEPORT_IDS = Set.of(
      // Royal seed pod
      ItemID.ROYAL_SEED_POD,
      // Amulet of glory (charged)
      ItemID.AMULET_OF_GLORY1, ItemID.AMULET_OF_GLORY2, ItemID.AMULET_OF_GLORY3,
      ItemID.AMULET_OF_GLORY4, ItemID.AMULET_OF_GLORY5, ItemID.AMULET_OF_GLORY6,
      // Amulet of glory (trimmed)
      ItemID.AMULET_OF_GLORY_T1, ItemID.AMULET_OF_GLORY_T2, ItemID.AMULET_OF_GLORY_T3,
      ItemID.AMULET_OF_GLORY_T4, ItemID.AMULET_OF_GLORY_T5, ItemID.AMULET_OF_GLORY_T6,
      // Amulet of eternal glory
      ItemID.AMULET_OF_ETERNAL_GLORY,
      // Ring of wealth (charged)
      ItemID.RING_OF_WEALTH_1, ItemID.RING_OF_WEALTH_2, ItemID.RING_OF_WEALTH_3,
      ItemID.RING_OF_WEALTH_4, ItemID.RING_OF_WEALTH_5,
      // Ring of wealth (imbued)
      ItemID.RING_OF_WEALTH_I, ItemID.RING_OF_WEALTH_I1, ItemID.RING_OF_WEALTH_I2,
      ItemID.RING_OF_WEALTH_I3, ItemID.RING_OF_WEALTH_I4, ItemID.RING_OF_WEALTH_I5
  );

  private static final Set<Integer> LEVEL_20_TELEPORT_IDS = Set.of(
      // Ring of dueling
      ItemID.RING_OF_DUELING1, ItemID.RING_OF_DUELING2, ItemID.RING_OF_DUELING3,
      ItemID.RING_OF_DUELING4, ItemID.RING_OF_DUELING5, ItemID.RING_OF_DUELING6,
      ItemID.RING_OF_DUELING7, ItemID.RING_OF_DUELING8,
      // Games necklace
      ItemID.GAMES_NECKLACE1, ItemID.GAMES_NECKLACE2, ItemID.GAMES_NECKLACE3,
      ItemID.GAMES_NECKLACE4, ItemID.GAMES_NECKLACE5, ItemID.GAMES_NECKLACE6,
      ItemID.GAMES_NECKLACE7, ItemID.GAMES_NECKLACE8,
      // Combat bracelet
      ItemID.COMBAT_BRACELET1, ItemID.COMBAT_BRACELET2, ItemID.COMBAT_BRACELET3,
      ItemID.COMBAT_BRACELET4, ItemID.COMBAT_BRACELET5, ItemID.COMBAT_BRACELET6,
      // Skills necklace
      ItemID.SKILLS_NECKLACE1, ItemID.SKILLS_NECKLACE2, ItemID.SKILLS_NECKLACE3,
      ItemID.SKILLS_NECKLACE4, ItemID.SKILLS_NECKLACE5, ItemID.SKILLS_NECKLACE6,
      // Ring of returning
      ItemID.RING_OF_RETURNING1, ItemID.RING_OF_RETURNING2, ItemID.RING_OF_RETURNING3,
      ItemID.RING_OF_RETURNING4, ItemID.RING_OF_RETURNING5,
      // Teleport tablets
      ItemID.VARROCK_TELEPORT, ItemID.LUMBRIDGE_TELEPORT,
      ItemID.FALADOR_TELEPORT, ItemID.CAMELOT_TELEPORT
  );

  public EscapeOption getBestEscape(Client client, int wildernessLevel) {
    boolean teleblocked = client.getVarbitValue(TELEBLOCK_VARBIT) > 0;

    if (!teleblocked) {
      if (wildernessLevel <= 30) {
        String item = findTeleportItem(client, LEVEL_30_TELEPORT_IDS);
        if (item != null) {
          return new EscapeOption(EscapeOption.EscapeType.TELEPORT, item, false);
        }
      }
      if (wildernessLevel <= 20) {
        String item = findTeleportItem(client, LEVEL_20_TELEPORT_IDS);
        if (item != null) {
          return new EscapeOption(EscapeOption.EscapeType.TELEPORT, item, false);
        }
      }
    }

    int tilesToDitch = calculateTilesToDitch(client);
    int tilesToFerox = calculateTilesToFerox(client);

    if (tilesToFerox < tilesToDitch) {
      return new EscapeOption(EscapeOption.EscapeType.RUN,
          "Ferox Enclave (" + tilesToFerox + " tiles)", teleblocked);
    }
    return new EscapeOption(EscapeOption.EscapeType.RUN,
        "Run south (" + tilesToDitch + " tiles)", teleblocked);
  }

  public WorldPoint getBestRunDestination(Client client) {
    int tilesToDitch = calculateTilesToDitch(client);
    int tilesToFerox = calculateTilesToFerox(client);
    if (tilesToFerox < tilesToDitch) {
      return FEROX_ENTRANCE;
    }
    WorldPoint player = client.getLocalPlayer().getWorldLocation();
    return new WorldPoint(player.getX(), WILDERNESS_DITCH_Y, player.getPlane());
  }

  private String findTeleportItem(Client client, Set<Integer> ids) {
    ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
    if (equipment != null) {
      for (Item item : equipment.getItems()) {
        if (ids.contains(item.getId())) {
          return client.getItemDefinition(item.getId()).getName();
        }
      }
    }
    ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
    if (inventory != null) {
      for (Item item : inventory.getItems()) {
        if (ids.contains(item.getId())) {
          return client.getItemDefinition(item.getId()).getName();
        }
      }
    }
    return null;
  }

  public List<WorldPoint> generateEscapePath(Client client) {
    WorldPoint player = client.getLocalPlayer().getWorldLocation();
    WorldPoint dest = getBestRunDestination(client);
    return EscapePathfinder.findPath(client, player, dest);
  }

  public Set<Integer> getAvailableTeleportIds(int wildernessLevel, boolean teleblocked) {
    if (teleblocked) {
      return Set.of();
    }
    Set<Integer> ids = new HashSet<>();
    if (wildernessLevel <= 30) {
      ids.addAll(LEVEL_30_TELEPORT_IDS);
    }
    if (wildernessLevel <= 20) {
      ids.addAll(LEVEL_20_TELEPORT_IDS);
    }
    return ids;
  }

  private int calculateTilesToDitch(Client client) {
    int playerY = client.getLocalPlayer().getWorldLocation().getY();
    return Math.max(0, playerY - WILDERNESS_DITCH_Y);
  }

  private int calculateTilesToFerox(Client client) {
    WorldPoint player = client.getLocalPlayer().getWorldLocation();
    return player.distanceTo(FEROX_ENTRANCE);
  }
}
