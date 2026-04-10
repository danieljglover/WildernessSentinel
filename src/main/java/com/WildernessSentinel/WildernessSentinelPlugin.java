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
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameTick;
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

@Slf4j
@PluginDescriptor(name = "Wilderness Sentinel")
public class WildernessSentinelPlugin extends Plugin {
  @Inject private Client client;

  @Inject private WildernessSentinelConfig config;

  @Inject private OverlayManager overlayManager;

  @Inject private AlarmOverlay overlay;

  @Inject private Notifier notifier;

  private boolean overlayOn = false;

  private final Set<String> customIgnores = new HashSet<>();

  private static final Splitter CONFIG_SPLITTER =
      Splitter.onPattern("([,\n])").omitEmptyStrings().trimResults();

  private static final Pattern WILDERNESS_LEVEL_PATTERN = Pattern.compile("Level: (\\d+)");

  private final HashMap<String, Integer> playerNameToTimeInRange = new HashMap<>();

  private final SafeZoneHelper zoneHelper = new SafeZoneHelper();

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
      return;
    }

    int wildernessLevel = isInWilderness ? getWildernessLevel() : -1;
    boolean shouldAlarm =
        getPlayersInRange().anyMatch(player -> shouldPlayerTriggerAlarm(player, isInWilderness, wildernessLevel));

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

    return true;
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
  }

  @Override
  protected void shutDown() throws Exception {
    if (overlayOn) {
      removeOverlay();
    }
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
    }
  }

  @Provides
  WildernessSentinelConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(WildernessSentinelConfig.class);
  }
}
