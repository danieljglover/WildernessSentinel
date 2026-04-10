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

  // -- General --

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

  // -- Alarm Filters --

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
      keyName = "ignoreFriends",
      name = "Ignore friends",
      description = "Do not alarm for players on your friends list.",
      section = filtersSection,
      position = 12)
  default boolean ignoreFriends() {
    return true;
  }

  @ConfigItem(
      keyName = "ignoreClan",
      name = "Ignore clan members",
      description = "Do not alarm for players in your clan.",
      section = filtersSection,
      position = 13)
  default boolean ignoreClan() {
    return true;
  }

  @ConfigItem(
      keyName = "ignoreFriendsChat",
      name = "Ignore friends chat",
      description = "Do not alarm for players in the same friends chat.",
      section = filtersSection,
      position = 14)
  default boolean ignoreFriendsChat() {
    return false;
  }

  @ConfigItem(
      keyName = "ignoreIgnored",
      name = "Ignore blocked players",
      description = "Do not alarm for players on your in-game ignore list.",
      section = filtersSection,
      position = 15)
  default boolean ignoreIgnored() {
    return false;
  }

  @ConfigItem(
      keyName = "customIgnores",
      name = "Custom ignore list",
      description = "Comma-separated list of player names that should never trigger the alarm (case-insensitive).",
      section = filtersSection,
      position = 16)
  default String customIgnoresList() {
    return "";
  }

  // -- Notifications --

  @ConfigItem(
      keyName = "customizableNotification",
      name = "Player spotted notification",
      description = "Notification triggered when an alarming player is detected. Configure sound, tray popup, and flash independently.",
      section = notificationsSection,
      position = 21)
  default Notification customizableNotification() {
    return new Notification();
  }

  // -- Screen Flash --

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
}
