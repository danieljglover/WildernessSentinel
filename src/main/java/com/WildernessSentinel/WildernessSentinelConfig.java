package com.WildernessSentinel;

import java.awt.Color;
import net.runelite.client.config.*;

@ConfigGroup("WildernessSentinel")
public interface WildernessSentinelConfig extends Config {

  // ── Section declarations ──────────────────────────────────────────

  @ConfigSection(
      name = "General",
      description = "Core alarm settings",
      position = 0)
  String generalSection = "generalSection";

  @ConfigSection(
      name = "Alarm Filters",
      description = "Control which players trigger the alarm",
      position = 10)
  String filtersSection = "filtersSection";

  @ConfigSection(
      name = "Notifications",
      description = "Sound and system notifications",
      position = 20)
  String notificationsSection = "notificationsSection";

  @ConfigSection(
      name = "Screen Flash",
      description = "Full-screen flash overlay settings",
      position = 30)
  String flashSection = "flashSection";

  @ConfigSection(
      name = "PKer Database",
      description = "PKer tracking database settings",
      position = 40)
  String databaseSection = "databaseSection";

  @ConfigSection(
      name = "PKer Highlights",
      description = "In-game PKer highlight overlays",
      position = 50)
  String pkerHighlightSection = "pkerHighlightSection";

  @ConfigSection(
      name = "Minimap",
      description = "Minimap PKer indicators",
      position = 60)
  String minimapSection = "minimapSection";

  @ConfigSection(
      name = "Team Warning",
      description = "PKer team detection overlay",
      position = 70)
  String teamWarningSection = "teamWarningSection";

  @ConfigSection(
      name = "Threat Tiers",
      description = "Colour-coding based on report count",
      position = 80)
  String tierSettingsSection = "tierSettingsSection";

  // ── General ───────────────────────────────────────────────────────

  @Range(min = 0, max = 30)
  @ConfigItem(
      keyName = "alarmRadius",
      name = "Alarm radius (tiles)",
      description = "Distance in tiles for a player to trigger the alarm. Players beyond render distance will not be detected.",
      section = generalSection,
      position = 1)
  default int alarmRadius() {
    return 15;
  }

  @ConfigItem(
      keyName = "timeoutToIgnore",
      name = "Player timeout (seconds)",
      description = "Stop alarming for a player after they have been nearby for this many seconds. 0 to disable.",
      section = generalSection,
      position = 2)
  default int timeoutToIgnore() {
    return 0;
  }

  @ConfigItem(
      keyName = "pvpWorldAlerts",
      name = "PvP world alerts",
      description = "Trigger alerts everywhere when on a PvP or Deadman Mode world, not just in the Wilderness.",
      section = generalSection,
      position = 3)
  default boolean pvpWorldAlerts() {
    return false;
  }

  // ── Alarm Filters ─────────────────────────────────────────────────

  @ConfigItem(
      keyName = "onlyAlarmAttackable",
      name = "Only attackable players",
      description = "Only alarm for players within your attackable combat level range based on the current wilderness level. Has no effect in PvP worlds.",
      section = filtersSection,
      position = 11)
  default boolean onlyAlarmAttackable() {
    return false;
  }

  @ConfigItem(
      keyName = "onlyAlarmKnownPkers",
      name = "Only known PKers",
      description = "Only alarm for players found in the PKer database.",
      section = filtersSection,
      position = 12)
  default boolean onlyAlarmKnownPkers() {
    return false;
  }

  @ConfigItem(
      keyName = "alarmOnSkulledPlayers",
      name = "Include skulled unknowns",
      description = "When 'Only known PKers' is enabled, also alarm for unknown players who are skulled. Respects the attackable combat level filter.",
      section = filtersSection,
      position = 13)
  default boolean alarmOnSkulledPlayers() {
    return false;
  }

  @ConfigItem(
      keyName = "ignoreFriends",
      name = "Ignore friends",
      description = "Do not alarm for players on your friends list.",
      section = filtersSection,
      position = 14)
  default boolean ignoreFriends() {
    return true;
  }

  @ConfigItem(
      keyName = "ignoreClan",
      name = "Ignore clan members",
      description = "Do not alarm for players in your clan.",
      section = filtersSection,
      position = 15)
  default boolean ignoreClan() {
    return true;
  }

  @ConfigItem(
      keyName = "ignoreFriendsChat",
      name = "Ignore friends chat",
      description = "Do not alarm for players in the same friends chat.",
      section = filtersSection,
      position = 16)
  default boolean ignoreFriendsChat() {
    return false;
  }

  @ConfigItem(
      keyName = "ignoreIgnored",
      name = "Ignore blocked players",
      description = "Do not alarm for players on your in-game ignore list.",
      section = filtersSection,
      position = 17)
  default boolean ignoreIgnored() {
    return false;
  }

  @ConfigItem(
      keyName = "customIgnores",
      name = "Custom ignore list",
      description = "Comma-separated list of player names that should never trigger the alarm (case-insensitive).",
      section = filtersSection,
      position = 18)
  default String customIgnoresList() {
    return "";
  }

  // ── Notifications ─────────────────────────────────────────────────

  @ConfigItem(
      keyName = "customizableNotification",
      name = "Player spotted notification",
      description = "Notification triggered when any alarming player is detected. Configure sound, tray popup, and flash independently.",
      section = notificationsSection,
      position = 21)
  default Notification customizableNotification() {
    return new Notification();
  }

  @ConfigItem(
      keyName = "pkerNotification",
      name = "Known PKer notification",
      description = "Separate notification for known PKers. Overrides the general notification when a known PKer triggers the alarm.",
      section = notificationsSection,
      position = 22)
  default Notification pkerNotification() {
    return new Notification();
  }

  // ── Screen Flash ──────────────────────────────────────────────────

  @Alpha
  @ConfigItem(
      keyName = "flashColor",
      name = "Flash colour",
      description = "Colour and transparency of the full-screen alarm flash.",
      section = flashSection,
      position = 31)
  default Color flashColor() {
    return new Color(255, 255, 0, 70);
  }

  @ConfigItem(
      keyName = "flashControl",
      name = "Flash speed",
      description = "How fast the screen flashes when the alarm triggers.",
      section = flashSection,
      position = 32)
  default FlashSpeed flashControl() {
    return FlashSpeed.NORMAL;
  }

  @ConfigItem(
      keyName = "flashLayer",
      name = "Flash render layer",
      description = "Which rendering layer the flash overlay appears on. Change if the flash conflicts with other overlays.",
      section = flashSection,
      position = 33)
  default FlashLayer flashLayer() {
    return FlashLayer.ABOVE_SCENE;
  }

  // ── PKer Database ─────────────────────────────────────────────────

  @ConfigItem(
      keyName = "pkerDatabaseMode",
      name = "Database mode",
      description = "Community: shared PKer list from all users. My Reports: only PKers you have personally reported.",
      section = databaseSection,
      position = 41)
  default PkerDatabaseMode pkerDatabaseMode() {
    return PkerDatabaseMode.COMMUNITY;
  }

  @ConfigItem(
      keyName = "showReportPkerMenu",
      name = "Right-click 'Report PKer'",
      description = "Add a right-click menu option on players in the wilderness to manually report them as a PKer.",
      section = databaseSection,
      position = 42)
  default boolean showReportPkerMenu() {
    return true;
  }

  @ConfigItem(
      keyName = "pkerServerUrl",
      name = "Server URL",
      description = "URL of the PKer database server.",
      section = databaseSection,
      hidden = true,
      position = 43)
  default String pkerServerUrl() {
    return "http://178.104.148.172:8080";
  }

  @ConfigItem(
      keyName = "pkerApiKey",
      name = "API key",
      description = "API key for the PKer database server. Auto-generated on first use.",
      section = databaseSection,
      hidden = true,
      position = 44)
  default String pkerApiKey() {
    return "";
  }

  // ── PKer Highlights ───────────────────────────────────────────────

  @ConfigItem(
      keyName = "enablePkerHighlight",
      name = "Enable hull outline",
      description = "Draw a coloured outline around known PKers in the game world.",
      section = pkerHighlightSection,
      position = 51)
  default boolean enablePkerHighlight() {
    return true;
  }

  @Range(min = 1, max = 5)
  @ConfigItem(
      keyName = "pkerHighlightStroke",
      name = "Outline thickness",
      description = "Stroke width of the PKer hull outline in pixels.",
      section = pkerHighlightSection,
      position = 52)
  default int pkerHighlightStroke() {
    return 2;
  }

  @ConfigItem(
      keyName = "enablePkerLabel",
      name = "Enable info label",
      description = "Show a text label above known PKers with their combat level, world, and report count.",
      section = pkerHighlightSection,
      position = 53)
  default boolean enablePkerLabel() {
    return true;
  }

  // ── Minimap ───────────────────────────────────────────────────────

  @ConfigItem(
      keyName = "enableMinimapDots",
      name = "Enable PKer dots",
      description = "Show coloured dots on the minimap for nearby known PKers.",
      section = minimapSection,
      position = 61)
  default boolean enableMinimapDots() {
    return true;
  }

  @Range(min = 2, max = 12)
  @ConfigItem(
      keyName = "minimapDotSize",
      name = "Dot size (pixels)",
      description = "Diameter of the minimap PKer dots.",
      section = minimapSection,
      position = 62)
  default int minimapDotSize() {
    return 6;
  }

  // ── Team Warning ──────────────────────────────────────────────────

  @Range(min = 0, max = 10)
  @ConfigItem(
      keyName = "teamWarningThreshold",
      name = "Team size threshold",
      description = "Show a prominent on-screen warning when this many known PKers are nearby. 0 to disable.",
      section = teamWarningSection,
      position = 71)
  default int teamWarningThreshold() {
    return 2;
  }

  @ConfigItem(
      keyName = "teamWarningColor",
      name = "Warning colour",
      description = "Text colour for the PKer team warning overlay.",
      section = teamWarningSection,
      position = 72)
  default Color teamWarningColor() {
    return new Color(255, 0, 0);
  }

  @Range(min = 12, max = 48)
  @ConfigItem(
      keyName = "teamWarningFontSize",
      name = "Warning font size",
      description = "Font size for the PKer team warning text.",
      section = teamWarningSection,
      position = 73)
  default int teamWarningFontSize() {
    return 24;
  }

  // ── Threat Tiers ──────────────────────────────────────────────────

  @Range(min = 2, max = 50)
  @ConfigItem(
      keyName = "tierMediumThreshold",
      name = "Medium threat (reports)",
      description = "Number of unique reports needed to classify a PKer as medium threat.",
      section = tierSettingsSection,
      position = 81)
  default int tierMediumThreshold() {
    return 3;
  }

  @Range(min = 3, max = 100)
  @ConfigItem(
      keyName = "tierHighThreshold",
      name = "High threat (reports)",
      description = "Number of unique reports needed to classify a PKer as high threat.",
      section = tierSettingsSection,
      position = 82)
  default int tierHighThreshold() {
    return 10;
  }

  @Alpha
  @ConfigItem(
      keyName = "tierLowColor",
      name = "Low threat colour",
      description = "Highlight colour for PKers below the medium threshold.",
      section = tierSettingsSection,
      position = 83)
  default Color tierLowColor() {
    return new Color(255, 255, 0, 128);
  }

  @Alpha
  @ConfigItem(
      keyName = "tierMediumColor",
      name = "Medium threat colour",
      description = "Highlight colour for PKers at or above the medium threshold.",
      section = tierSettingsSection,
      position = 84)
  default Color tierMediumColor() {
    return new Color(255, 165, 0, 128);
  }

  @Alpha
  @ConfigItem(
      keyName = "tierHighColor",
      name = "High threat colour",
      description = "Highlight colour for PKers at or above the high threshold.",
      section = tierSettingsSection,
      position = 85)
  default Color tierHighColor() {
    return new Color(255, 0, 0, 128);
  }
}
