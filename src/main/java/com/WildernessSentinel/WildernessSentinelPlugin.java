package com.WildernessSentinel;

import com.google.common.base.Splitter;
import com.google.inject.Provides;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.ItemID;
import net.runelite.api.SkullIcon;
import net.runelite.api.kit.KitType;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(name = "Wilderness Sentinel")
public class WildernessSentinelPlugin extends Plugin {
  @Inject private Client client;

  @Inject private WildernessSentinelConfig config;

  @Inject private OverlayManager overlayManager;

  @Inject private AlarmOverlay overlay;

  @Inject private Notifier notifier;
  @Inject private ConfigManager configManager;

  private static final String IGNORE_OPTION = "Sentinel Ignore";

  @Inject private ThreatHighlightOverlay threatHighlightOverlay;
  @Inject private ThreatMinimapOverlay threatMinimapOverlay;
  @Inject private EscapeTileOverlay escapeTileOverlay;
  @Inject private EscapeMinimapOverlay escapeMinimapOverlay;
  @Inject private TeleportHighlightOverlay teleportHighlightOverlay;

  @Getter
  private final EscapeRouteManager escapeRouteManager = new EscapeRouteManager();

  @Getter
  private EscapeOption currentEscape;
  @Getter
  private net.runelite.api.coords.WorldPoint escapeDestination;
  @Getter
  private java.util.List<net.runelite.api.coords.WorldPoint> escapePath;
  @Getter
  private int currentWildernessLevel;

  @Getter
  private final List<Player> threateningPlayers = new ArrayList<>();

  private boolean overlayOn = false;

  private final Set<String> customIgnores = new HashSet<>();

  private static final Splitter CONFIG_SPLITTER =
      Splitter.onPattern("([,\n])").omitEmptyStrings().trimResults();

  private static final Pattern WILDERNESS_LEVEL_PATTERN = Pattern.compile("Level: (\\d+)");

  private final HashMap<String, Integer> playerNameToTimeInRange = new HashMap<>();

  private final SafeZoneHelper zoneHelper = new SafeZoneHelper();

  private final Set<Integer> customAlertItemIds = new HashSet<>();

  private static final Set<Integer> DANGEROUS_WEAPON_IDS = Set.of(
      // Melee - Whips
      ItemID.ABYSSAL_WHIP, ItemID.ABYSSAL_WHIP_4178, ItemID.VOLCANIC_ABYSSAL_WHIP,
      ItemID.FROZEN_ABYSSAL_WHIP, ItemID.ABYSSAL_WHIP_OR, ItemID.ABYSSAL_WHIP_20405,
      // Melee - Dragon claws
      ItemID.DRAGON_CLAWS, ItemID.DRAGON_CLAWS_OR, ItemID.DRAGON_CLAWS_CR,
      ItemID.DRAGON_CLAWS_20784, ItemID.CORRUPTED_DRAGON_CLAWS,
      // Melee - Granite maul
      ItemID.GRANITE_MAUL, ItemID.GRANITE_MAUL_12848, ItemID.GRANITE_MAUL_20557,
      ItemID.GRANITE_MAUL_24225, ItemID.GRANITE_MAUL_24227,
      // Melee - Elder maul
      ItemID.ELDER_MAUL, ItemID.ELDER_MAUL_21205, ItemID.ELDER_MAUL_OR,
      // Melee - Godswords
      ItemID.ARMADYL_GODSWORD, ItemID.ARMADYL_GODSWORD_OR, ItemID.ARMADYL_GODSWORD_20593,
      ItemID.ARMADYL_GODSWORD_22665, ItemID.CORRUPTED_ARMADYL_GODSWORD,
      ItemID.ARMADYL_GODSWORD_DEADMAN,
      ItemID.BANDOS_GODSWORD, ItemID.BANDOS_GODSWORD_OR, ItemID.BANDOS_GODSWORD_20782,
      ItemID.BANDOS_GODSWORD_21060,
      ItemID.SARADOMIN_GODSWORD, ItemID.SARADOMIN_GODSWORD_OR,
      ItemID.ZAMORAK_GODSWORD, ItemID.ZAMORAK_GODSWORD_OR,
      ItemID.ANCIENT_GODSWORD, ItemID.ANCIENT_GODSWORD_27184,
      // Melee - Rapier
      ItemID.GHRAZI_RAPIER, ItemID.GHRAZI_RAPIER_23628, ItemID.HOLY_GHRAZI_RAPIER,
      // Melee - Dragon dagger
      ItemID.DRAGON_DAGGER, ItemID.DRAGON_DAGGERP, ItemID.DRAGON_DAGGERP_5680,
      ItemID.DRAGON_DAGGERP_5698, ItemID.DRAGON_DAGGER_20407,
      ItemID.DRAGON_DAGGER_CR, ItemID.DRAGON_DAGGER_PCR,
      ItemID.DRAGON_DAGGER_PCR_28023, ItemID.DRAGON_DAGGER_PCR_28025,
      // Melee - Dragon scimitar
      ItemID.DRAGON_SCIMITAR, ItemID.DRAGON_SCIMITAR_OR, ItemID.DRAGON_SCIMITAR_20406,
      ItemID.DRAGON_SCIMITAR_CR,
      // Melee - Other
      ItemID.VOIDWAKER, ItemID.VOIDWAKER_27869, ItemID.CORRUPTED_VOIDWAKER,
      ItemID.VOIDWAKER_DEADMAN,
      ItemID.VESTAS_LONGSWORD, ItemID.VESTAS_LONGSWORD_23615, ItemID.VESTAS_LONGSWORD_BH,
      ItemID.STATIUSS_WARHAMMER, ItemID.STATIUSS_WARHAMMER_23620, ItemID.STATIUSS_WARHAMMER_BH,
      ItemID.INQUISITORS_MACE, ItemID.INQUISITORS_MACE_27198,
      ItemID.URSINE_CHAINMACE,
      // Ranged
      ItemID.TOXIC_BLOWPIPE, ItemID.TOXIC_BLOWPIPE_EMPTY,
      ItemID.ARMADYL_CROSSBOW, ItemID.ARMADYL_CROSSBOW_23611,
      ItemID.DRAGON_CROSSBOW, ItemID.DRAGON_CROSSBOW_CR,
      ItemID.ZARYTE_CROSSBOW, ItemID.ZARYTE_CROSSBOW_27186,
      ItemID.HEAVY_BALLISTA, ItemID.HEAVY_BALLISTA_23630, ItemID.HEAVY_BALLISTA_OR,
      ItemID.MAGIC_SHORTBOW_I,
      ItemID.DARK_BOW, ItemID.DARK_BOW_12765, ItemID.DARK_BOW_12766,
      ItemID.DARK_BOW_12767, ItemID.DARK_BOW_12768, ItemID.DARK_BOW_20408,
      ItemID.DARK_BOW_BH, ItemID.CORRUPTED_DARK_BOW, ItemID.DARK_BOW_DEADMAN,
      // Magic
      ItemID.TOXIC_STAFF_OF_THE_DEAD, ItemID.TOXIC_STAFF_UNCHARGED,
      ItemID.TOXIC_STAFF_DEADMAN,
      ItemID.NIGHTMARE_STAFF, ItemID.VOLATILE_NIGHTMARE_STAFF,
      ItemID.ELDRITCH_NIGHTMARE_STAFF, ItemID.HARMONISED_NIGHTMARE_STAFF,
      ItemID.VOLATILE_NIGHTMARE_STAFF_25517, ItemID.CORRUPTED_VOLATILE_NIGHTMARE_STAFF,
      ItemID.VOLATILE_NIGHTMARE_STAFF_DEADMAN,
      ItemID.KODAI_WAND, ItemID.KODAI_WAND_23626,
      ItemID.TUMEKENS_SHADOW, ItemID.TUMEKENS_SHADOW_UNCHARGED,
      ItemID.CORRUPTED_TUMEKENS_SHADOW, ItemID.CORRUPTED_TUMEKENS_SHADOW_UNCHARGED,
      ItemID.SANGUINESTI_STAFF, ItemID.SANGUINESTI_STAFF_UNCHARGED,
      ItemID.HOLY_SANGUINESTI_STAFF, ItemID.HOLY_SANGUINESTI_STAFF_UNCHARGED
  );

  @Subscribe
  public void onPlayerDespawned(PlayerDespawned event) {
    playerNameToTimeInRange.remove(event.getPlayer().getName());
  }

  @Subscribe
  public void onGameTick(GameTick event) {
    boolean isInWilderness = client.getVarbitValue(Varbits.IN_WILDERNESS) == 1;
    boolean isInDangerousPvpArea = config.pvpWorldAlerts() && isInPvp();
    if (!isInWilderness && !isInDangerousPvpArea) {
      if (overlayOn) {
        removeOverlay();
      }
      threateningPlayers.clear();
      currentEscape = null;
      escapeDestination = null;
      escapePath = null;
      return;
    }

    int wildernessLevel = isInWilderness ? getWildernessLevel() : -1;
    currentWildernessLevel = wildernessLevel;
    threateningPlayers.clear();
    boolean shouldAlarm = false;
    for (Player player : getPlayersInRange().collect(java.util.stream.Collectors.toList())) {
      if (shouldPlayerTriggerAlarm(player, isInWilderness, wildernessLevel)) {
        shouldAlarm = true;
        threateningPlayers.add(player);
      }
    }

    // Keep track of how long players have been in range if timeout is enabled
    if (config.timeoutToIgnore() > 0) {
      updatePlayersInRange();
    }

    if (shouldAlarm && !overlayOn) {
      if (config.customizableNotification().isEnabled()) {
        notifier.notify(config.customizableNotification(), "Player spotted!");
      }
      addOverlay();
    }

    if (!shouldAlarm) {
      removeOverlay();
    }

    // Update escape route - only when being attacked
    boolean beingAttacked = false;
    for (Player player : client.getTopLevelWorldView().players()) {
      if (player != null && player != client.getLocalPlayer()
          && player.getInteracting() == client.getLocalPlayer()) {
        beingAttacked = true;
        break;
      }
    }
    if (isInWilderness && (beingAttacked || config.alwaysShowEscape())) {
      currentEscape = escapeRouteManager.getBestEscape(client, wildernessLevel);
      escapeDestination = escapeRouteManager.getBestRunDestination(client);
      if (currentEscape.getType() == EscapeOption.EscapeType.RUN) {
        escapePath = escapeRouteManager.generateEscapePath(client);
      } else {
        escapePath = null;
      }
    } else {
      currentEscape = null;
      escapeDestination = null;
      escapePath = null;
    }
  }

  private Stream<? extends Player> getPlayersInRange() {
    LocalPoint currentPosition = client.getLocalPlayer().getLocalLocation();
    int alarmRadius = config.alarmRadius();
    return client.getTopLevelWorldView().players().stream()
        .filter(
            player -> (player.getLocalLocation().distanceTo(currentPosition) / 128) <= alarmRadius);
  }

  private boolean shouldPlayerTriggerAlarm(Player player, boolean inWilderness, int wildernessLevel) {
    if (player.getId() == client.getLocalPlayer().getId()) {
      return false;
    }

    if (config.ignoreClan() && player.isClanMember()) {
      return false;
    }

    if (config.ignoreFriends() && player.isFriend()) {
      return false;
    }

    if (config.ignoreFriendsChat() && player.isFriendsChatMember()) {
      return false;
    }

    if (config.ignoreIgnored()
        && client.getIgnoreContainer().findByName(player.getName()) != null) {
      return false;
    }

    if (customIgnores.contains(player.getName().toLowerCase())) {
      return false;
    }

    if (config.timeoutToIgnore() > 0) {
      int timePlayerIsOnScreen = playerNameToTimeInRange.getOrDefault(player.getName(), 0);
      if (timePlayerIsOnScreen > config.timeoutToIgnore() * 1000) {
        return false;
      }
    }

    if (inWilderness && zoneHelper.PointInsideFerox(player.getWorldLocation())) {
      return false;
    }

    if (config.onlyAlarmAttackable() && inWilderness && wildernessLevel > 0) {
      int myCombat = client.getLocalPlayer().getCombatLevel();
      int theirCombat = player.getCombatLevel();
      if (theirCombat < myCombat - wildernessLevel || theirCombat > myCombat + wildernessLevel) {
        return false;
      }
    }

    if (config.onlyAlarmSkulled() && player.getSkullIcon() == SkullIcon.NONE) {
      return false;
    }

    if (config.onlyAlarmDangerousWeapons() && !hasDangerousEquipment(player)) {
      return false;
    }

    return true;
  }

  private boolean hasDangerousEquipment(Player player) {
    if (player.getPlayerComposition() == null) {
      return false;
    }
    int weaponId = player.getPlayerComposition().getEquipmentId(KitType.WEAPON);
    if (DANGEROUS_WEAPON_IDS.contains(weaponId)) {
      return true;
    }
    if (!customAlertItemIds.isEmpty()) {
      for (KitType slot : KitType.values()) {
        int itemId = player.getPlayerComposition().getEquipmentId(slot);
        if (customAlertItemIds.contains(itemId)) {
          return true;
        }
      }
    }
    return false;
  }

  private void resetCustomAlertItemIds() {
    customAlertItemIds.clear();
    for (String s : config.customAlertItemIds().split(",")) {
      String trimmed = s.trim();
      if (!trimmed.isEmpty()) {
        try {
          customAlertItemIds.add(Integer.parseInt(trimmed));
        } catch (NumberFormatException ignored) {
        }
      }
    }
  }

  private void updatePlayersInRange() {
    Collection<String> playerNames = new HashSet<>();
    getPlayersInRange()
        .forEach(
            player -> {
              String playerName = player.getName();
              playerNames.add(playerName);
              playerNameToTimeInRange.merge(playerName, Constants.GAME_TICK_LENGTH, Integer::sum);
            });

    playerNameToTimeInRange.keySet().removeIf(name -> !playerNames.contains(name));
  }

  private void resetCustomIgnores() {
    customIgnores.clear();
    customIgnores.addAll(CONFIG_SPLITTER.splitToList(config.customIgnoresList().toLowerCase()));
  }

  private boolean isInPvp() {
    boolean pvp =
        WorldType.isPvpWorld(client.getWorldType())
            && (client.getVarbitValue(Varbits.PVP_SPEC_ORB) == 1);
    Widget widget = client.getWidget(ComponentID.PVP_WILDERNESS_LEVEL);
    if (widget == null) {
      return pvp;
    }
    String widgetText = widget.getText();
    pvp &= !widgetText.startsWith("Protection");
    pvp &= !widgetText.startsWith("Guarded");
    return pvp;
  }

  private int getWildernessLevel() {
    Widget widget = client.getWidget(ComponentID.PVP_WILDERNESS_LEVEL);
    if (widget == null) {
      return -1;
    }
    String text = widget.getText();
    if (text == null) {
      return -1;
    }
    Matcher matcher = WILDERNESS_LEVEL_PATTERN.matcher(text);
    if (matcher.find()) {
      return Integer.parseInt(matcher.group(1));
    }
    return -1;
  }

  private void addOverlay() {
    overlayOn = true;
    overlayManager.add(overlay);
  }

  private void removeOverlay() {
    overlayOn = false;
    overlayManager.remove(overlay);
  }

  @Override
  protected void startUp() {
    overlay.setLayer(config.flashLayer().getLayer());
    resetCustomIgnores();
    resetCustomAlertItemIds();
    overlayManager.add(threatHighlightOverlay);
    overlayManager.add(threatMinimapOverlay);
    overlayManager.add(escapeTileOverlay);
    overlayManager.add(escapeMinimapOverlay);
    overlayManager.add(teleportHighlightOverlay);
  }

  @Override
  protected void shutDown() throws Exception {
    if (overlayOn) {
      removeOverlay();
    }
    threateningPlayers.clear();
    overlayManager.remove(threatHighlightOverlay);
    overlayManager.remove(threatMinimapOverlay);
    overlayManager.remove(escapeTileOverlay);
    overlayManager.remove(escapeMinimapOverlay);
    overlayManager.remove(teleportHighlightOverlay);
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged event) {
    if (event.getGroup().equals("WildernessSentinel")) {
      if ("timeoutToIgnore".equals(event.getKey()) && config.timeoutToIgnore() <= 0) {
        playerNameToTimeInRange.clear();
      }

      overlay.setLayer(config.flashLayer().getLayer());
      if (overlayOn) {
        removeOverlay();
        addOverlay();
      }
      resetCustomIgnores();
      resetCustomAlertItemIds();
    }
  }

  @Subscribe
  public void onMenuEntryAdded(MenuEntryAdded event) {
    if (event.getType() != MenuAction.PLAYER_FIRST_OPTION.getId()
        && event.getType() != MenuAction.PLAYER_SECOND_OPTION.getId()) {
      return;
    }

    MenuEntry entry = client.createMenuEntry(-1)
        .setOption(IGNORE_OPTION)
        .setTarget(event.getTarget())
        .setType(MenuAction.RUNELITE)
        .setDeprioritized(true)
        .onClick(e -> {
          String target = Text.removeTags(e.getTarget()).replaceAll("\\s*\\(.*\\)\\s*", "").trim();
          if (!target.isEmpty()) {
            String current = config.customIgnoresList();
            String updated = current.isEmpty() ? target : current + "," + target;
            configManager.setConfiguration("WildernessSentinel", "customIgnores", updated);
            resetCustomIgnores();
          }
        });
  }

  @Provides
  WildernessSentinelConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(WildernessSentinelConfig.class);
  }
}
