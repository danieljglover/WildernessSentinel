# Wilderness Sentinel

A Wilderness protection plugin for RuneLite that alerts you when threatening players are nearby, with configurable filters to reduce false alerts and an escape route system to help you get out alive.

![Screen Alert](screenshots/Screen_Alert.png)

---

## Features

### Threat Detection
Stack multiple filters to narrow down exactly which players trigger the alarm:

- **Only attackable players** - filters by your combat level range for the current wilderness level (e.g. level 60 in level 20 wilderness = only players between 40-80)
- **Only skulled players** - only alert on players who have initiated PvP combat
- **Only dangerous weapons** - only alert on players carrying known PK weapons (whips, claws, godswords, toxic staff, ballista, and 90+ more)
- **Custom alert item IDs** - add your own item IDs to the dangerous equipment list, checks all equipment slots

Enable all three together to only alert on skulled players within your combat range who are carrying PK weapons.

![Threat Detection](screenshots/threat_detection.png)

### Escape Route System
When you're under attack, the plugin calculates and displays your best escape option:

- **A* pathfinding** - draws a walkable path to the nearest safe zone using the game's live collision data, avoiding walls, water, and obstacles
- **Smart destination** - automatically picks the closest safe zone (wilderness ditch or Ferox Enclave)
- **Teleport awareness** - scans your inventory and equipment for usable teleports based on your current wilderness level
  - Level 1-20: ring of dueling, games necklace, combat bracelet, skills necklace, teleport tablets
  - Level 1-30: royal seed pod, amulet of glory, ring of wealth
- **Teleblock detection** - detects when you're teleblocked and only shows physical escape routes
- **Teleport highlighting** - usable teleport items in your inventory glow green when the alarm is active
- **Game world path** - green highlighted tiles with connecting lines showing the walkable escape route
- **Minimap path** - matching green line on the minimap for at-a-glance direction

![Escape Route](screenshots/escape_route.png)

![Teleport Highlight](screenshots/teleport_highlight.png)

### Player Highlights
Visual indicators on players who pass all your filters:

- **In-game outline** - coloured hull outline around threatening players
- **Overhead label** - shows combat level and skull status
- **Minimap dots** - coloured dots for threats on the minimap
- **Configurable** - colour, outline thickness, and dot size

![Player Highlights](screenshots/player_highlights.png)

### Smart Ignore List
Exclude players you trust:

- **Right-click "Sentinel Ignore"** - right-click any player to add them to your ignore list instantly
- Friends, clan members, and friends chat
- Blocked players from your in-game ignore list
- Custom ignore list with specific player names
- Player timeout - stop alerting after a player has been nearby for a set duration
- Ferox Enclave safe zone detection

![Sentinel Ignore](screenshots/sentinel_ignore.png)

### Screen Flash
Full-screen flash overlay when a threat is detected:

- Configurable colour with transparency
- Multiple flash speeds (off, slow, normal, fast, solid)
- Render layer control

### Notifications
RuneLite notification when a threat is detected - configure sound, tray popup, and flash independently.

### Additional
- **PvP world support** - alert everywhere on PvP and Deadman Mode worlds
- **Combat level formula** - uses the correct wilderness level +/- your combat level calculation
- **Ferox Enclave** - automatically excludes players inside the safe zone

---

## Configuration

| Section | Options |
|---------|---------|
| **General** | Alarm radius, player timeout, PvP world alerts |
| **Threat Detection** | Attackable filter, skull filter, weapon filter, custom item IDs |
| **Ignore List** | Friends, clan, friends chat, blocked, custom names |
| **Player Highlights** | In-game outline, overhead label, minimap dots, colour, thickness, dot size |
| **Escape Route** | Show escape route, always show in wilderness, show minimap line |
| **Notifications** | Player spotted notification |
| **Screen Flash** | Flash colour, speed, render layer |

![Config Panel](screenshots/config_panel.png)
