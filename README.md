# DonutSpawners

A remake of the Famous DonutSMP one

![Preview 1](https://i.imgur.com/76lRDdK.png)
![Preview 2](https://i.imgur.com/l4mN7g2.png)

## Features

*   **Virtual Storage**: Items and XP are stored inside the spawner instead of being dropped on the ground (Anti-Lag).
*   **Spawner Stacking**: Stack spawners to increase production rates in a single block.
*   **Economy Integration**: Sell items directly from the GUI (requires Essentials).
*   **Isolation Bonus**: Spawners produce faster if they are not placed directly next to others.
*   **Natural Spawners**: Configurable whether naturally generated spawners (dungeons) become virtual or remain vanilla.
*   **Fully Configurable**: All messages, GUI titles, prices, and intervals can be changed in `config.yml`.

## Installation

1.  Stop your server.
2.  Upload the `.jar` file from the `target` or the [Releases page](https://github.com/Jannes02/DonutSpawners/releases) then put it in the `plugins` folder.
3.  Ensure **EssentialX** is installed (required for the economy system). -> https://essentialsx.net/
4.  Start the server.
5.  Edit `config.yml` in the `plugins/DonutSpawners/` folder to adjust prices and messages.

## Commands & Permissions

### Commands
*   `/givespawner <Player> <Type> [Amount]`
    *   Gives a player a ready-to-use spawner.
    *   Example: `/givespawner Jannes IRON_GOLEM 1`

### Permissions
*   `donutspawners.admin`
    *   Allows using `/givespawner`.
    *   Default: OP only.

## Configuration (config.yml)

Here are the most important settings explained:

*   **settings**:
    *   `require_silk_touch`: `true` = You need Silk Touch to mine spawners.
    *   `natural_spawners_virtual`: `true` = Natural spawners become custom spawners when clicked.
    *   `production_interval`: How often (in ticks) items are generated (20 ticks = 1 second).
*   **messages**: You can translate all texts here, including GUI names.
*   **prices**: Set how much money you get per item when selling.
*   **xp**: Set how much XP is generated per cycle.

## Usage in Game

### Placing & Stacking
*   **Place**: Place a spawner to start production.
*   **Stacking**: Right-click an existing spawner with the same type of spawner in hand to stack them.
    *   **Shift + Right-click**: Adds the entire stack from your hand to the spawner.
    *   **Note**: You will see an action bar message confirming the stack count.

### GUI & Storage
*   **Open (Right-click)**: Opens the main menu (if not holding a spawner to stack).
    *   **Click Head**: Sells everything and collects XP.
    *   **Chest**: Opens the detailed storage (view drops).
    *   **XP Bottle**: Collects only XP.

### Breaking
*   **Break**: Mine the spawner (Default: requires Silk Touch).
    *   **Normal Break**: Reduces the stack by 1 and drops 1 spawner item.
    *   **Shift + Break**: Breaks up to 64 spawners from the stack at once.
    *   **Warning**: All stored items/XP inside the spawner are lost when broken!

MIT License
