package com.WildernessSentinel;

import com.google.common.base.Splitter;
import com.google.inject.Provides;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.Player;
import net.runelite.api.SkullIcon;
import net.runelite.api.Varbits;
import net.runelite.api.WorldType;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(name = "Wilderness Sentinel")
public class WildernessSentinelPlugin extends Plugin {
  @Inject private Client client;

  @Inject private WildernessSentinelConfig config;

  @Inject private OverlayManager overlayManager;

  @Inject private AlarmOverlay overlay;

  @Inject private Notifier notifier;

  @Inject private ScheduledExecutorService executorService;

  @Inject private ConfigManager configManager;

  @Inject private PkerHighlightOverlay pkerHighlightOverlay;
  @Inject private PkerMinimapOverlay pkerMinimapOverlay;
  @Inject private PkerTeamOverlay pkerTeamOverlay;
  @Inject private MenuManager menuManager;
  @Inject private ClientToolbar clientToolbar;
  @Inject private PkerPanel pkerPanel;
  private NavigationButton navigationButton;

  private static final String REPORT_PKER_OPTION = "Report PKer";

  @Getter
  private PkerApiClient pkerApiClient;
  @Getter
  private Map<String, PkerInfo> knownPkers = new HashMap<>();
  private final Set<String> recentlyReportedAttackers = new HashSet<>();

  @Getter private int pkerTeamCount = 0;

  private boolean overlayOn = false;
  private boolean pkerNotificationFired = false;

  private final Set<String> customIgnores = new HashSet<>();

  private static final Splitter CONFIG_SPLITTER =
      Splitter.onPattern("([,\n])").omitEmptyStrings().trimResults();

  private static final Pattern WILDERNESS_LEVEL_PATTERN = Pattern.compile("Level: (\\d+)");

  private final HashMap<String, Integer> playerNameToTimeInRange = new HashMap<>();

  private final HashMap<String, Long> lastSeenUpdateTimes = new HashMap<>();

  private final SafeZoneHelper zoneHelper = new SafeZoneHelper();

  @Subscribe
  public void onPlayerDespawned(PlayerDespawned event) {
    playerNameToTimeInRange.remove(event.getPlayer().getName());
    recentlyReportedAttackers.remove(event.getPlayer().getName().toLowerCase());
  }

  @Subscribe
  public void onGameTick(GameTick event) {
    boolean isInWilderness = client.getVarbitValue(Varbits.IN_WILDERNESS) == 1;
    boolean isInDangerousPvpArea = config.pvpWorldAlerts() && isInPvp();
    if (!isInWilderness && !isInDangerousPvpArea) {
      if (overlayOn) {
        removeOverlay();
      }
      pkerTeamCount = 0;
      return;
    }

    int wildernessLevel = isInWilderness ? getWildernessLevel() : -1;
    boolean shouldAlarm = false;
    boolean knownPkerInRange = false;
    int pkerCount = 0;
    for (Player player : getPlayersInRange().collect(java.util.stream.Collectors.toList())) {
      if (shouldPlayerTriggerAlarm(player, isInWilderness, wildernessLevel)) {
        shouldAlarm = true;
        if (knownPkers.containsKey(player.getName().toLowerCase())) {
          knownPkerInRange = true;
          pkerCount++;
        }
      }
    }
    pkerTeamCount = pkerCount;

    // Keep track of how long players have been in range if timeout is enabled
    if (config.timeoutToIgnore() > 0) {
      updatePlayersInRange();
    }

    if (shouldAlarm && !overlayOn) {
      if (knownPkerInRange && config.pkerNotification().isEnabled()) {
        notifier.notify(config.pkerNotification(), "Known PKer spotted!");
        pkerNotificationFired = true;
      } else if (config.customizableNotification().isEnabled()) {
        notifier.notify(config.customizableNotification(), "Player spotted!");
      }
      addOverlay();
    } else if (shouldAlarm && overlayOn && knownPkerInRange && !pkerNotificationFired) {
      if (config.pkerNotification().isEnabled()) {
        notifier.notify(config.pkerNotification(), "Known PKer spotted!");
        pkerNotificationFired = true;
      }
    }

    if (!shouldAlarm) {
      removeOverlay();
      pkerNotificationFired = false;
    }

    // Update last seen for known PKers that are skulled or attacking
    updateKnownPkerLastSeen();
  }

  private Stream<? extends Player> getPlayersInRange() {
    LocalPoint currentPosition = client.getLocalPlayer().getLocalLocation();
    int alarmRadius = config.alarmRadius();
    return client.getTopLevelWorldView().players().stream()
        .filter(
            player -> (player.getLocalLocation().distanceTo(currentPosition) / 128) <= alarmRadius);
  }

  private boolean shouldPlayerTriggerAlarm(Player player, boolean inWilderness, int wildernessLevel) {
    // Don't trigger for yourself
    if (player.getId() == client.getLocalPlayer().getId()) {
      return false;
    }

    // Don't trigger for clan members if option is selected
    if (config.ignoreClan() && player.isClanMember()) {
      return false;
    }

    // Don't trigger for friends if option is selected
    if (config.ignoreFriends() && player.isFriend()) {
      return false;
    }

    // Don't trigger for friends if option is selected
    if (config.ignoreFriendsChat() && player.isFriendsChatMember()) {
      return false;
    }

    // Don't trigger for ignored players if option is selected
    if (config.ignoreIgnored()
        && client.getIgnoreContainer().findByName(player.getName()) != null) {
      return false;
    }

    // Don't trigger for players in the custom ignore list
    if (customIgnores.contains(player.getName().toLowerCase())) {
      return false;
    }

    // Ignore players that have been on screen longer than the timeout
    if (config.timeoutToIgnore() > 0) {
      int timePlayerIsOnScreen = playerNameToTimeInRange.getOrDefault(player.getName(), 0);
      if (timePlayerIsOnScreen > config.timeoutToIgnore() * 1000) {
        return false;
      }
    }

    // Don't trigger for players inside Ferox Enclave (short-circuit to only check from wildy)
    if (inWilderness && zoneHelper.PointInsideFerox(player.getWorldLocation())) {
      return false;
    }

    // Only alarm for players within attackable combat level range (wilderness only)
    if (config.onlyAlarmAttackable() && inWilderness && wildernessLevel > 0) {
      int myCombat = client.getLocalPlayer().getCombatLevel();
      int theirCombat = player.getCombatLevel();
      if (theirCombat < myCombat - wildernessLevel || theirCombat > myCombat + wildernessLevel) {
        return false;
      }
    }

    // Only alarm for known PKers if option is enabled (allow skulled unknowns through if configured)
    if (config.onlyAlarmKnownPkers()
        && !knownPkers.containsKey(player.getName().toLowerCase())) {
      if (!config.alarmOnSkulledPlayers() || player.getSkullIcon() == SkullIcon.NONE) {
        return false;
      }
    }

    return true;
  }

  private void updatePlayersInRange() {
    // Update players that are still in range
    Collection<String> playerNames = new HashSet<>();
    getPlayersInRange()
        .forEach(
            player -> {
              String playerName = player.getName();
              playerNames.add(playerName);
              playerNameToTimeInRange.merge(playerName, Constants.GAME_TICK_LENGTH, Integer::sum);
            });

    // Remove players that are out of range
    playerNameToTimeInRange.keySet().removeIf(name -> !playerNames.contains(name));
  }

  private void updateKnownPkerLastSeen() {
    if (knownPkers.isEmpty() || pkerApiClient == null) {
      return;
    }
    int currentWorld = client.getWorld();
    long now = System.currentTimeMillis();
    long throttleMs = 5 * 60 * 1000; // 5 minutes

    for (Player player : client.getTopLevelWorldView().players()) {
      if (player == null || player == client.getLocalPlayer() || player.getName() == null) {
        continue;
      }
      String nameLower = player.getName().toLowerCase();
      if (!knownPkers.containsKey(nameLower)) {
        continue;
      }

      boolean isSkulled = player.getSkullIcon() != SkullIcon.NONE;
      boolean isAttackingPlayer = player.getInteracting() instanceof Player;

      if (isSkulled || isAttackingPlayer) {
        Long lastUpdate = lastSeenUpdateTimes.get(nameLower);
        if (lastUpdate == null || (now - lastUpdate) > throttleMs) {
          lastSeenUpdateTimes.put(nameLower, now);
          String name = player.getName();
          executorService.submit(() -> pkerApiClient.updateLastSeen(name, currentWorld));
        }
      }
    }
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

    // Initialize PKer API client
    pkerApiClient = new PkerApiClient(config.pkerServerUrl());
    String apiKey = config.pkerApiKey();
    if (!apiKey.isEmpty()) {
      pkerApiClient.setApiKey(apiKey);
    }
    // Register PKer highlight overlays
    overlayManager.add(pkerHighlightOverlay);
    overlayManager.add(pkerMinimapOverlay);
    overlayManager.add(pkerTeamOverlay);
    // Register report menu
    if (config.showReportPkerMenu()) {
      menuManager.addPlayerMenuItem(REPORT_PKER_OPTION);
    }
    // Register sidebar panel
    navigationButton = NavigationButton.builder()
        .icon(ImageUtil.loadImageResource(getClass(), "/com/WildernessSentinel/icon.png"))
        .tooltip("PKer Database")
        .panel(pkerPanel)
        .priority(10)
        .build();
    clientToolbar.addNavigation(navigationButton);
    // Start background sync
    executorService.scheduleWithFixedDelay(this::syncPkerList, 0, 10, TimeUnit.SECONDS);
  }

  @Override
  protected void shutDown() throws Exception {
    if (overlayOn) {
      removeOverlay();
    }
    knownPkers.clear();
    recentlyReportedAttackers.clear();
    lastSeenUpdateTimes.clear();
    overlayManager.remove(pkerHighlightOverlay);
    overlayManager.remove(pkerMinimapOverlay);
    overlayManager.remove(pkerTeamOverlay);
    menuManager.removePlayerMenuItem(REPORT_PKER_OPTION);
    clientToolbar.removeNavigation(navigationButton);
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

      if ("pkerServerUrl".equals(event.getKey())) {
        pkerApiClient = new PkerApiClient(config.pkerServerUrl());
        String apiKey = config.pkerApiKey();
        if (!apiKey.isEmpty()) {
          pkerApiClient.setApiKey(apiKey);
        }
        executorService.submit(this::syncPkerList);
      }
      if ("pkerDatabaseMode".equals(event.getKey())) {
        executorService.submit(this::syncPkerList);
      }
      if ("showReportPkerMenu".equals(event.getKey())) {
        if (config.showReportPkerMenu()) {
          menuManager.addPlayerMenuItem(REPORT_PKER_OPTION);
        } else {
          menuManager.removePlayerMenuItem(REPORT_PKER_OPTION);
        }
      }
    }
  }

  @Subscribe
  public void onMenuOptionClicked(MenuOptionClicked event) {
    if (!REPORT_PKER_OPTION.equals(event.getMenuOption())) {
      return;
    }
    if (client.getVarbitValue(Varbits.IN_WILDERNESS) != 1) {
      return;
    }

    String target = Text.removeTags(event.getMenuTarget()).replaceAll("\\s*\\(.*\\)\\s*", "").trim();
    if (target != null && !target.isEmpty() && pkerApiClient != null) {
      int wildernessLevel = getWildernessLevel();
      knownPkers.put(target.toLowerCase(), new PkerInfo(1, client.getWorld()));
      int currentWorld = client.getWorld();
      executorService.submit(() ->
          pkerApiClient.reportAttacker(target, Math.max(wildernessLevel, 0), currentWorld));
    }
  }

  private void syncPkerList() {
    try {
      // Auto-register if no API key
      if (config.pkerApiKey().isEmpty()) {
        String newKey = pkerApiClient.register();
        if (newKey != null) {
          pkerApiClient.setApiKey(newKey);
          configManager.setConfiguration("WildernessSentinel", "pkerApiKey", newKey);
        }
        return;
      }
      Map<String, PkerInfo> fetched = pkerApiClient.fetchPkers(config.pkerDatabaseMode());
      if (!fetched.isEmpty()) {
        knownPkers = fetched;
        SwingUtilities.invokeLater(() -> pkerPanel.updatePkerList(knownPkers));
      }
      java.util.List<HotspotEntry> hotspots = pkerApiClient.fetchHotspots();
      SwingUtilities.invokeLater(() -> pkerPanel.updateHotspots(hotspots));
    } catch (Exception e) {
      log.warn("Failed to sync PKer list", e);
    }
  }

  @Subscribe
  public void onHitsplatApplied(HitsplatApplied event) {
    if (event.getActor() != client.getLocalPlayer()) {
      return;
    }
    if (client.getVarbitValue(Varbits.IN_WILDERNESS) != 1) {
      return;
    }

    Hitsplat hitsplat = event.getHitsplat();
    if (!hitsplat.isMine()) {
      return;
    }

    int wildernessLevel = getWildernessLevel();
    for (Player player : client.getTopLevelWorldView().players()) {
      if (player != null
          && player != client.getLocalPlayer()
          && player.getInteracting() == client.getLocalPlayer()) {
        String attackerName = player.getName();
        if (attackerName != null
            && pkerApiClient != null
            && !recentlyReportedAttackers.contains(attackerName.toLowerCase())) {
          recentlyReportedAttackers.add(attackerName.toLowerCase());
          knownPkers.put(attackerName.toLowerCase(), new PkerInfo(1, client.getWorld()));
          int currentWorld = client.getWorld();
          executorService.submit(() ->
              pkerApiClient.reportAttacker(attackerName, Math.max(wildernessLevel, 0), currentWorld));
        }
      }
    }
  }

  @Provides
  WildernessSentinelConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(WildernessSentinelConfig.class);
  }
}
