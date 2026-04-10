package com.WildernessSentinel;

import java.awt.Color;
import net.runelite.client.config.*;

@ConfigGroup("WildernessSentinel")
public interface WildernessSentinelConfig extends Config {

  // -- Section declarations --

  @ConfigSection(
      name = "General",
      description = "Core alarm settings",
      position = 0)
  String generalSection = "generalSection";

  @ConfigSection(
      name = "Threat Detection",
      description = "Stack filters to reduce false alerts",
      position = 10)
  String threatSection = "threatSection";

  @ConfigSection(
      name = "Ignore List",
      description = "Players to exclude from alerts",
      position = 20)
  String ignoreSection = "ignoreSection";

  @ConfigSection(
      name = "Player Highlights",
      description = "Visual indicators on filtered threats",
      position = 30)
  String highlightSection = "highlightSection";

  @ConfigSection(
      name = "Escape Route",
      description = "Best escape option based on inventory and wilderness level",
      position = 40)
  String escapeSection = "escapeSection";

  @ConfigSection(
      name = "Notifications",
      description = "Sound and system notifications",
      position = 50)
  String notificationsSection = "notificationsSection";

  @ConfigSection(
      name = "Screen Flash",
      description = "Full-screen flash overlay",
      position = 60)
  String flashSection = "flashSection";

  // -- General --

  @Range(min = 0, max = 30)
  @ConfigItem(
      keyName = "alarmRadius",
      name = "Alarm radius (tiles)",
      description = "Detection range in tiles",
      section = generalSection,
      position = 1)
  default int alarmRadius() {
    return 15;
  }

  @ConfigItem(
      keyName = "timeoutToIgnore",
      name = "Player timeout (seconds)",
      description = "Ignore a player after this many seconds nearby. 0 to disable.",
      section = generalSection,
      position = 2)
  default int timeoutToIgnore() {
    return 0;
  }

  @ConfigItem(
      keyName = "pvpWorldAlerts",
      name = "PvP world alerts",
      description = "Alert everywhere on PvP/Deadman worlds",
      section = generalSection,
      position = 3)
  default boolean pvpWorldAlerts() {
    return false;
  }

  // -- Threat Detection --

  @ConfigItem(
      keyName = "onlyAlarmAttackable",
      name = "Only attackable players",
      description = "Only players within your combat range for the current wilderness level",
      section = threatSection,
      position = 11)
  default boolean onlyAlarmAttackable() {
    return false;
  }

  @ConfigItem(
      keyName = "onlyAlarmSkulled",
      name = "Only skulled players",
      description = "Only players with a skull overhead",
      section = threatSection,
      position = 12)
  default boolean onlyAlarmSkulled() {
    return false;
  }

  @ConfigItem(
      keyName = "onlyAlarmDangerousWeapons",
      name = "Only dangerous weapons",
      description = "Only players carrying known PK weapons",
      section = threatSection,
      position = 13)
  default boolean onlyAlarmDangerousWeapons() {
    return false;
  }

  @ConfigItem(
      keyName = "customAlertItemIds",
      name = "Custom alert item IDs",
      description = "Additional item IDs to treat as dangerous (comma-separated)",
      section = threatSection,
      position = 14)
  default String customAlertItemIds() {
    return "";
  }

  // -- Ignore List --

  @ConfigItem(
      keyName = "ignoreFriends",
      name = "Ignore friends",
      description = "Never alarm for friends",
      section = ignoreSection,
      position = 21)
  default boolean ignoreFriends() {
    return true;
  }

  @ConfigItem(
      keyName = "ignoreClan",
      name = "Ignore clan members",
      description = "Never alarm for clan members",
      section = ignoreSection,
      position = 22)
  default boolean ignoreClan() {
    return true;
  }

  @ConfigItem(
      keyName = "ignoreFriendsChat",
      name = "Ignore friends chat",
      description = "Never alarm for friends chat members",
      section = ignoreSection,
      position = 23)
  default boolean ignoreFriendsChat() {
    return false;
  }

  @ConfigItem(
      keyName = "ignoreIgnored",
      name = "Ignore blocked players",
      description = "Never alarm for players on your ignore list",
      section = ignoreSection,
      position = 24)
  default boolean ignoreIgnored() {
    return false;
  }

  @ConfigItem(
      keyName = "customIgnores",
      name = "Custom ignore list",
      description = "Player names to ignore (comma-separated, case-insensitive)",
      section = ignoreSection,
      position = 25)
  default String customIgnoresList() {
    return "";
  }

  // -- Player Highlights --

  @ConfigItem(
      keyName = "highlightThreats",
      name = "Highlight in game world",
      description = "Outline threatening players in the 3D world",
      section = highlightSection,
      position = 31)
  default boolean highlightThreats() {
    return false;
  }

  @Alpha
  @ConfigItem(
      keyName = "highlightColor",
      name = "Highlight colour",
      description = "Colour for outlines and minimap dots",
      section = highlightSection,
      position = 32)
  default Color highlightColor() {
    return new Color(255, 0, 0, 128);
  }

  @Range(min = 1, max = 5)
  @ConfigItem(
      keyName = "highlightStroke",
      name = "Outline thickness",
      description = "Stroke width in pixels",
      section = highlightSection,
      position = 33)
  default int highlightStroke() {
    return 2;
  }

  @ConfigItem(
      keyName = "highlightLabel",
      name = "Show overhead label",
      description = "Show combat level and skull status above the player",
      section = highlightSection,
      position = 34)
  default boolean highlightLabel() {
    return true;
  }

  @ConfigItem(
      keyName = "highlightMinimap",
      name = "Highlight on minimap",
      description = "Show coloured dots on the minimap for threats",
      section = highlightSection,
      position = 35)
  default boolean highlightMinimap() {
    return false;
  }

  @Range(min = 2, max = 12)
  @ConfigItem(
      keyName = "minimapDotSize",
      name = "Minimap dot size",
      description = "Dot diameter in pixels",
      section = highlightSection,
      position = 36)
  default int minimapDotSize() {
    return 6;
  }

  // -- Escape Route --

  @ConfigItem(
      keyName = "showEscapeRoute",
      name = "Show escape route",
      description = "Show best escape option when threats are nearby",
      section = escapeSection,
      position = 41)
  default boolean showEscapeRoute() {
    return true;
  }

  @ConfigItem(
      keyName = "alwaysShowEscape",
      name = "Always show in wilderness",
      description = "Show escape route at all times in wilderness",
      section = escapeSection,
      position = 42)
  default boolean alwaysShowEscape() {
    return false;
  }

  @ConfigItem(
      keyName = "showEscapeArrow",
      name = "Show minimap arrow",
      description = "Arrow pointing to nearest safe zone on minimap",
      section = escapeSection,
      position = 43)
  default boolean showEscapeArrow() {
    return true;
  }

  // -- Notifications --

  @ConfigItem(
      keyName = "customizableNotification",
      name = "Player spotted notification",
      description = "Notification when a threat is detected",
      section = notificationsSection,
      position = 51)
  default Notification customizableNotification() {
    return new Notification();
  }

  // -- Screen Flash --

  @Alpha
  @ConfigItem(
      keyName = "flashColor",
      name = "Flash colour",
      description = "Colour and transparency of the alarm flash",
      section = flashSection,
      position = 61)
  default Color flashColor() {
    return new Color(255, 255, 0, 70);
  }

  @ConfigItem(
      keyName = "flashControl",
      name = "Flash speed",
      description = "How fast the screen flashes",
      section = flashSection,
      position = 62)
  default FlashSpeed flashControl() {
    return FlashSpeed.NORMAL;
  }

  @ConfigItem(
      keyName = "flashLayer",
      name = "Flash render layer",
      description = "Rendering layer for the flash overlay",
      section = flashSection,
      position = 63)
  default FlashLayer flashLayer() {
    return FlashLayer.ABOVE_SCENE;
  }
}
