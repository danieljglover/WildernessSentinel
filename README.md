# Wilderness Sentinel

A Wilderness protection plugin for RuneLite that alerts you when another player enters your vicinity while in the Wilderness or on PvP worlds.

---

## Features

### Player Detection & Alerts
Automatically detects when another player enters your vicinity in the Wilderness and triggers configurable alerts - screen flash and sound notifications.

- **Configurable alarm radius** (0-30 tiles)
- **Combat level filtering** - only alert for players within your attackable combat range based on the current wilderness level
- **PvP world support** - trigger alerts everywhere when on a PvP or Deadman Mode world

![Screen Alert](screenshots/Screen_Alert.png)

### Smart Filtering
Fine-grained control over which players trigger the alarm.

- **Friends, clan, and friends chat** - automatically ignored
- **Blocked players** - optionally ignore your in-game block list
- **Custom ignore list** - comma-separated player names to never alert on
- **Player timeout** - stop alerting for a player after they have been nearby for a set duration
- **Ferox Enclave safe zone** - players inside Ferox Enclave are automatically excluded

### Configurable Screen Flash
Full-screen flash overlay with customisable appearance.

- **Flash colour** - choose any colour with transparency control
- **Flash speed** - off, slow, normal, fast, or solid
- **Render layer** - control which layer the flash appears on

### Notifications
Configure RuneLite notifications with sound, tray popup, and flash options independently.

---

## Configuration

All settings are organised into logical sections in the RuneLite plugin configuration panel:

| Section | Options |
|---------|---------|
| **General** | Alarm radius, player timeout, PvP world alerts |
| **Alarm Filters** | Combat level filter, friend/clan/chat ignores, custom ignore list |
| **Notifications** | Player spotted notification |
| **Screen Flash** | Flash colour, speed, render layer |

---

## How It Works

1. **Detection** - every game tick, the plugin scans for players within your alarm radius
2. **Filtering** - each player is checked against your configured filters (friends, combat level, etc.)
3. **Alerting** - if a player passes all filters, the alarm triggers with your configured notifications and flash overlay
