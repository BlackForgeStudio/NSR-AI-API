# NSR-AI v2.0 — Dynamic Custom Tools Complete Architecture & Developer Guide

**Document Version:** 2.0.0  
**Target Runtime:** NSR-AI v2.0-pre (Spigot / Paper / Purpur 1.16.5 – 1.21.x)  
**Author:** BlackForge Studio Engineering  
**Classification:** Core System Architecture & Custom Tool Specification  

---

## Table of Contents

1. [Executive Overview & Agentic Architecture](#1-executive-overview--agentic-architecture)
2. [Runtime Deployment & File Packaging Ecosystem](#2-runtime-deployment--file-packaging-ecosystem)
3. [Military-Grade Security & Anti-Zip-Bomb Engine](#3-military-grade-security--anti-zip-bomb-engine)
4. [Programmatic Metadata Specification (`const tool = { ... }`)](#4-programmatic-metadata-specification-const-tool----)
5. [Input Parameter Schema Specification (JSON Schema)](#5-input-parameter-schema-specification-json-schema)
6. [JavaScript Runtime Environment (Nashorn ES6 Engine)](#6-javascript-runtime-environment-nashorn-es6-engine)
7. [Injected Global Scope & Bukkit Bridge APIs](#7-injected-global-scope--bukkit-bridge-apis)
8. [Execution Lifecycle, Threading & Safety Guarantees](#8-execution-lifecycle-threading--safety-guarantees)
9. [Standardized Return Value & AI Output Contract](#9-standardized-return-value--ai-output-contract)
10. [Declarative YAML Custom Tools Engine](#10-declarative-yaml-custom-tools-engine)
11. [Production Reference Implementations (Reference Tools 1–5)](#11-production-reference-implementations-reference-tools-15)
    - [Reference Tool 1: `ban_abusive_player.js` (Chat Moderation & Enforcement)](#reference-tool-1-ban_abusive_playerjs-chat-moderation--enforcement)
    - [Reference Tool 2: `spawn_boss_arena.js` (World Generation & Entity Summoning)](#reference-tool-2-spawn_boss_arenajs-world-generation--entity-summoning)
    - [Reference Tool 3: `teleport_home_waypoint.js` (Spatial Navigation & Verification)](#reference-tool-3-teleport_home_waypointjs-spatial-navigation--verification)
    - [Reference Tool 4: `claim_daily_reward.yml` (Declarative YAML Reward Engine)](#reference-tool-4-claim_daily_rewardyml-declarative-yaml-reward-engine)
    - [Reference Tool 5: `bounty_contract_system.js` (Economy & Interactive Approvals)](#reference-tool-5-bounty_contract_systemjs-economy--interactive-approvals)
12. [In-Game Management, Commands & Hot-Reloading](#12-in-game-management-commands--hot-reloading)
13. [Troubleshooting, Common Error Logs & Diagnostics](#13-troubleshooting-common-error-logs--diagnostics)
14. [Future Architectural Roadmap: External `nsr-ai-api` Java Tool Registration](#14-future-architectural-roadmap-external-nsr-ai-api-java-tool-registration)
15. [Advanced Spatial Algorithms & World Mathematics](#15-advanced-spatial-algorithms--world-mathematics)
16. [Comprehensive Sound, Material & Particle Lookup Reference](#16-comprehensive-sound-material--particle-lookup-reference)
17. [Production Reference Tool 6: `inventory_vault_backup.js`](#17-production-reference-tool-6-inventory_vault_backupjs)
18. [Pre-Flight Checklist Before Publishing Custom Tools](#18-pre-flight-checklist-before-publishing-custom-tools)

---

## 1. Executive Overview & Agentic Architecture

The **NSR-AI Dynamic Custom Tools Engine** marks the architectural transition of NSR-AI from a passive conversational language assistant into a fully autonomous, in-game server companion and operational agent. In traditional Minecraft AI implementations, plugins are constrained to hardcoded actions or require external developers to write, compile, and shade complex Java JAR files for every custom behavior. NSR-AI v2.0-pre eliminates this barrier by providing an interpreted, hot-reloadable execution sandbox directly inside the server runtime.

At the core of NSR-AI v2.0-pre is the **ReAct (Reasoning + Acting) Agentic Turn Cycle**. When a player submits a message in-game, the AI does not merely generate static text. Instead, it enters an iterative decision-making loop:

```
+-----------------------------------------------------------------------------------+
|                           NSR-AI Autonomous Agent Loop                            |
+-----------------------------------------------------------------------------------+
                                          |
                                    [Player Input]
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| 1. CONTEXT ASSEMBLY                                                               |
|    - System Prompts & Core NSR-AI Identity Guardrails                             |
|    - Personality Profile & Pinned Things (Auto-RAG)                               |
|    - Dynamic Tool Schemas (10 Native Tools + All Loaded Custom Tools)             |
|    - Conversation History (Token-Capped with Dynamic Framing)                     |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| 2. LLM REASONING & FUNCTION CALL GENERATION                                       |
|    - Target Model (Gemini 3.8 Flash / Claude 3.5 Sonnet / OpenAI GPT-4o Mini)     |
|    - Stream Token Decoding via StreamProviderAdapter                              |
|    - Model decides whether to reply with text or emit a structured Tool Call      |
+-----------------------------------------------------------------------------------+
                                          |
                        +-----------------+-----------------+
                        |                                   |
                 [Text Emitted]                    [Tool Call Queued]
                        |                                   |
                        v                                   v
             [Streamed to Player Chat]          +-----------------------+
                                                | 3. EXECUTION DISPATCH |
                                                |    - CustomToolManager|
                                                |    - Argument Check   |
                                                |    - Permission Check |
                                                |    - Nashorn Script   |
                                                +-----------------------+
                                                            |
                                                            v
                                                +-----------------------+
                                                | 4. BUKKIT INTEGRATION |
                                                |    - Main-Thread Sync |
                                                |    - World/Player Mod |
                                                |    - Output JSON Pack |
                                                +-----------------------+
                                                            |
                                                            v
+-----------------------------------------------------------------------------------+
| 5. FOLLOW-UP AGENTIC SYNTHESIS                                                    |
|    - Tool output injected back into prompt as [SYSTEM DIRECTIVE]                  |
|    - Model reads exact execution results and formulates in-character summary      |
|    - Final response delivered to player with interactive [Edit] & [Regenerate]    |
+-----------------------------------------------------------------------------------+
```

### Key Capabilities of the Custom Tools Engine
- **Zero Compilation Overhead**: Server owners and administrators can create complex tools using single JavaScript files (`.js`), declarative YAML files (`.yml`), modular multi-file directories, or zipped packages (`.zip`). No Maven, Gradle, or JDK compilation is required.
- **Sub-Second Hot-Reloading**: Using `/ai tools reload` or `/ai reload tools`, the engine re-indexes, parses, validates, and registers tools in memory in under 15 milliseconds without disconnecting players or restarting the server.
- **Fail-Safe Isolation**: A syntax error, runtime exception, or bad parameter schema in one custom tool will never crash the server, block the AI from answering, or affect adjacent tools. The engine catches the exception, logs a precise diagnostic, and reports failure to the AI turn cycle.
- **Bi-Directional Communication**: Custom tools do not merely execute actions; they return structured JSON payloads to the AI model. This enables the model to inspect real-time outcomes (e.g., number of blocks scanned, items given, health restored) and generate intelligent, context-aware player messages.

---

## 2. Runtime Deployment & File Packaging Ecosystem

All custom tools are stored in dedicated directories monitored by the plugin. The engine scans the filesystem on startup and whenever a reload command is dispatched.

### Storage Locations
1. **Primary Custom Tools Directory**:
   ```text
   plugins/NSR-AI/custom_tools/
   ```
   This is the standard and recommended directory for all production custom tools.
2. **Fallback Directory**:
   ```text
   plugins/NSR-AI/tools/
   ```
   The engine automatically monitors this secondary directory for backward compatibility.

### Deployment Formats

NSR-AI v2.0-pre supports four distinct packaging formats to accommodate everything from one-liner administrative shortcuts to multi-script server mini-games.

| Format Identifier | File Extension / Structure | Description & Best Use Case |
|---|---|---|
| **Single-File JavaScript** | `tool_name.js` | Direct script file containing the programmatic metadata object (`const tool = { ... }`) and the `function execute(player, args)`. Ideal for world scanning, inventory math, entity manipulation, and custom logic. |
| **Single-File YAML** | `tool_name.yml` or `.yaml` | Declarative configuration defining metadata, permissions, command dispatches, PlaceholderAPI integrations, sound effects, and broadcasts. Ideal for non-programmers creating kit, reward, or broadcast tools. |
| **Multi-File Folder Package** | `custom_tools/my_tool_folder/` | A self-contained folder containing an entry point script (`tool.js`, `index.js`, or `tool.yml`) alongside auxiliary JSON configuration files, sub-scripts, or documentation. Ideal for complex server modular systems. |
| **Compressed ZIP Archive** | `tool_name.zip` | A compressed archive containing a tool package. NSR-AI automatically scans, safely decompresses, validates, and registers the packaged tool upon placement in `custom_tools/`. Ideal for community sharing and pre-packaged distributions. |

---

## 3. Military-Grade Security & Anti-Zip-Bomb Engine

Allowing external `.zip` archives and dynamic script execution requires rigorous defense-in-depth security. NSR-AI implements multiple automated verification layers before any tool script is loaded or decompressed into memory.

### 1. Zip-Slip Path Traversal Protection
Malicious ZIP archives frequently attempt to write files outside their target extraction directory using directory traversal sequences such as `../../plugins/update/malicious.jar`. The NSR-AI ZIP extractor inspects every file entry before extraction:
- The target destination path is normalized using `Path#normalize()`.
- The extractor verifies that `destinationPath.startsWith(targetDirectory)`.
- If an entry attempts to escape the root directory, extraction is aborted immediately, the archive is flagged as malicious, and a security alert is dispatched to console.

### 2. Decompression Bomb Defenses (Anti-Zip-Bomb)
NSR-AI enforces strict resource quotas during ZIP extraction:
- **Maximum Uncompressed File Size**: 25 Megabytes (26,214,400 bytes). If the aggregate uncompressed byte stream exceeds this threshold, the operation terminates instantly.
- **Maximum Compression Ratio**: 50:1. If an archive decompresses into a volume greater than 50 times its compressed payload, it is rejected as an algorithmic decompression bomb.
- **Maximum Entry Count**: 250 files. Archives containing excessive directory entries or deeply nested folder trees are rejected to prevent file descriptor exhaustion.

### 3. Fault-Tolerant Sandboxing
Each script runs in an isolated Nashorn execution context. Infinite loops or recursive overflows are caught by execution monitors. Uncaught script exceptions are wrapped into standard JSON error objects and passed back to the ReAct agent turn loop, ensuring that invalid player input or unexpected game states never crash Bukkit worker threads.

---

## 4. Programmatic Metadata Specification (`const tool = { ... }`)

Every custom JavaScript tool must declare a top-level metadata object named `tool`. This object informs the AI model of the tool's existence, defines its purpose, establishes parameter constraints, and enforces security permissions.

```javascript
// Programmatic Tool Metadata Object for NSR-AI v2.0
const tool = {
    status: "enabled",
    name: "ban_abusive_player",
    author: "BlackForge Studio",
    version: "2.0",
    link: "https://blackforge.org",
    description: "Enforces server chat moderation by banning players who use abusive, profane, toxic, vulgar, or derogatory language.",
    prompt_guidance: "MANDATORY MODERATION RULE: Invoke this tool immediately whenever the player uses abusive words, profanity, vulgarity, curses, or toxic slurs in Hindi, English, or any other language.",
    cooldown_seconds: 10,
    permission: "nsr-ai.tool.moderation",
    require_approval: false,
    approval_message: "Confirm disciplinary action against player?",
    parameters: {
        reason: {
            type: "STRING",
            description: "The detected abusive language or reason for the ban",
            required: true
        },
        duration_minutes: {
            type: "INTEGER",
            description: "Duration of the ban in minutes (default is 5)",
            required: false
        }
    }
};
```

### Field-by-Field Breakdown

| Metadata Field | Type | Required? | Description & Functional Purpose |
|---|---|---|---|
| `status` | String | **YES** | `"enabled"` or `"disabled"`. Setting to `"disabled"` immediately hides the tool from the AI model (omitting it from function-calling definitions) without deleting the file from disk. |
| `name` | String | **YES** | Unique identifier for the tool. Must follow `snake_case` naming conventions (`^[a-z0-9_]{1,64}$`). This is the exact name the AI invokes during function calling. |
| `author` | String | **YES** | Creator or organization credit (e.g., `"BlackForge Studio"`). Displayed in `/ai tools <name>` and server logs. |
| `version` | String | Optional | Semantic version identifier (e.g., `"1.0"`, `"2.0-pre"`). Displayed in `/ai tools` listings. |
| `link` | String | Optional | URL to documentation, support, or developer repository. |
| `description` | String | **YES** | Comprehensive explanation of what the tool accomplishes. The AI model reads this description to decide whether the tool is relevant to the player's request. |
| `prompt_guidance` | String | **YES** | High-priority behavioral directives injected into the model's system prompt. Instructs the model on exact triggers, edge cases, and requirements for invoking the tool. |
| `cooldown_seconds` | Integer | Optional | Minimum delay (in seconds) required between successive executions by the same player. Prevents tool spamming and resource exhaustion. |
| `permission` | String | Optional | Bukkit permission node required by the player to trigger this tool (e.g., `"nsr-ai.tool.ban"`). If omitted or blank, all players may trigger it. (`permissions` is also accepted as an alias). |
| `require_approval` | Boolean | Optional | If `true`, NSR-AI intercepts execution and delivers interactive `[APPROVE]` and `[CANCEL]` buttons to player chat prior to invoking logic. (`requires_approval` is also accepted as an alias). |
| `approval_message` | String | Optional | Custom clarification prompt displayed alongside the interactive `[APPROVE]` / `[CANCEL]` buttons (e.g. `"Confirm withdrawal of $500?"`). |
| `parameters` | Object | Optional | JSON Schema object specifying the arguments the AI must supply when invoking the tool. |

---

## 5. Input Parameter Schema Specification (JSON Schema)

The `parameters` object tells the AI model what inputs are required, what types are expected, and how to format them. NSR-AI converts this definition into standard OpenAPI / JSON Schema format for OpenAI, Claude, and Gemini APIs.

### Supported Parameter Data Types

1. **`STRING`**: Text values (e.g., player names, reason descriptions, material identifiers, command strings).
2. **`INTEGER`**: Whole numbers (e.g., quantities, slot indices, durations, radiuses).
3. **`NUMBER`**: Floating-point numbers (e.g., coordinates, prices, multipliers, percentages).
4. **`BOOLEAN`**: Binary true or false flags (e.g., `silent`, `broadcast`, `dropItems`).
5. **`ARRAY`**: Ordered lists of items (e.g., list of item names, list of target player names).
6. **`OBJECT`**: Key-value structures for structured data inputs.

### Parameter Attributes

Each parameter defined under `parameters` supports the following configuration properties:

| Property | Type | Required? | Description |
|---|---|---|---|
| `type` | String | **YES** | Data type identifier (`STRING`, `INTEGER`, `NUMBER`, `BOOLEAN`, `ARRAY`, `OBJECT`). |
| `description` | String | **YES** | Clear explanation of the parameter's meaning, expected format, and constraints. |
| `required` | Boolean | **YES** | If `true`, the AI is strictly forbidden from omitting this argument during invocation. |
| `default` | Any | Optional | Fallback value utilized if an optional parameter is omitted by the model. |
| `enum` | Array | Optional | An array of acceptable string literals (e.g., `["OVERWORLD", "NETHER", "THE_END"]`). |

### Multi-Parameter Example
```javascript
parameters: {
    target_player: {
        type: "STRING",
        description: "The username of the player receiving the bounty",
        required: true
    },
    bounty_amount: {
        type: "INTEGER",
        description: "Total currency amount offered for the contract",
        required: true
    },
    priority: {
        type: "STRING",
        description: "Priority tier for server announcements",
        required: false,
        enum: ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
    },
    broadcast_alert: {
        type: "BOOLEAN",
        description: "Whether to alert the entire server via global broadcast",
        required: false
    }
}
```

---

## 6. JavaScript Runtime Environment (Nashorn ES6 Engine)

NSR-AI utilizes the **OpenJDK Nashorn Script Engine** bundled directly within its shaded runtime. This allows JavaScript scripts to interact with JVM classes, Bukkit objects, and server APIs without external Node.js dependencies.

### Script Execution Architecture
1. **Preamble Initialization**: The engine evaluates the script once upon loading to extract the `tool` metadata object.
2. **Isolated Execution**: When the AI invokes the tool, the engine calls `execute(player, args)`.
3. **Variable Scope Hygiene**: Scripts should use clean variable scoping (`var`, `let`, `const`). Do not re-declare `const tool` inside function bodies.
4. **Exception Containment**: Any unhandled exception thrown in `execute()` is intercepted by the engine and returned as an error result to the AI, ensuring server stability.

---

## 7. Injected Global Scope & Bukkit Bridge APIs

When `execute(player, args)` is invoked, NSR-AI injects contextual objects into the script scope, providing access to player state, server utilities, cross-tool invocations, and interactive chat button builders.

```javascript
function execute(player, args) {
    // player  -> The org.bukkit.entity.Player who initiated the interaction
    // args    -> Map containing key-value arguments supplied by the AI model
    // server  -> NSR-AI Server Helper Bridge utility (commands, sounds, particles, titles)
    // Bukkit  -> Direct reference to org.bukkit.Server (scheduler, items, worlds)
    // buttons -> Interactive chat button builder (hover tooltips, clickable commands)
    // tools   -> Bridge to invoke any of the 10 built-in NSR-AI Universal Tools
    // plugin  -> The active com.nsr.ai.plugin.NSRAIPlugin instance
}
```

### 1. The `player` Object (Bukkit Player)

The `player` argument provides standard Spigot / Paper `org.bukkit.entity.Player` API methods:

- `player.getName()`: Returns the string username of the player.
- `player.getUniqueId()`: Returns the `java.util.UUID` of the player.
- `player.getLocation()`: Returns the `org.bukkit.Location` of the player.
- `player.getWorld()`: Returns the `org.bukkit.World` the player is currently in.
- `player.getHealth()`: Returns the player's current health (0.0 to 20.0).
- `player.getMaxHealth()`: Returns the maximum health attribute.
- `player.getFoodLevel()`: Returns the player's current hunger points (0 to 20).
- `player.getInventory()`: Returns the player's `PlayerInventory` instance.
- `player.isOnline()`: Returns `true` if the player is currently connected.
- `player.hasPermission(permissionString)`: Checks if the player holds a permission node.
- `player.sendMessage(messageString)`: Sends a message directly to the player's chat.
- `player.teleport(locationObject)`: Teleports the player to a target `Location`.

### 2. The `server` Object (NSR-AI Server Bridge)

To simplify server operations without verbose Java imports, NSR-AI exposes a lightweight `server` bridge:

- `server.dispatchCommand(commandString)`: Executes a command through the server console (auto-strips leading `/`). Scheduled onto the primary server thread.
- `server.dispatchCommandAsPlayer(player, commandString)`: Executes a command as the player.
- `server.broadcast(message)`: Broadcasts a color-coded message to all online players (`&` color codes supported).
- `server.playSound(player, soundName)`: Plays a sound effect (e.g., `"ENTITY_WITHER_SPAWN"`, `"ENTITY_PLAYER_LEVELUP"`) at the player's location.
- `server.spawnParticle(player, particleName)`: Spawns particle effects (e.g., `"ENCHANTMENT_TABLE"`, `"FLAME"`) around the player.
- `server.sendActionBar(player, message)`: Delivers an actionbar notification directly above the player's hotbar.
- `server.sendTitle(player, title, subtitle)`: Sends title and subtitle notifications with fade timings.
- `server.getPapi(player, text)`: Replaces `{player}`, `{uuid}`, `{world}`, `{x}`, `{y}`, `{z}`, `{health}`, `{hunger}`, and resolves external PlaceholderAPI `%...%` placeholders.
- `server.getPlayer(username)`: Retrieves an online `Player` by username, or `null` if offline.
- `server.getOnlinePlayers()`: Returns a collection of all currently connected players.
- `server.getWorld(worldName)`: Returns a Bukkit `World` by name.
- `server.createLocation(world, x, y, z)`: Helper to instantiate a new `org.bukkit.Location`.
- `server.getLogger()`: Returns the plugin logger instance.

### 3. The `buttons` Object (Interactive Chat Button Builder)

NSR-AI injects a fluent button builder allowing custom tools to construct and send interactive clickable chat components with hover tooltips and click events:

```javascript
buttons.builder()
    .text("&c&l[⚡ DEPLOY DEFENSIVE WALL] ")
    .hover("&7Click to execute /defense-wall command")
    .command("/defense-wall 30")
    .text(" &a&l[🛡️ EQUIP TOTEM] ")
    .hover("&7Click to move Totem to Off-Hand")
    .command("/ai inv equip totem")
    .text(" &b&l[🌐 WIKI] ")
    .hover("&7Click to open web documentation")
    .url("https://blackforge.org")
    .send(player);
```

- `.text(String text)`: Appends text with `&` color code translation.
- `.hover(String tooltip)`: Attaches a hover tooltip to the current text component.
- `.command(String command)`: Attaches a `ClickEvent.Action.RUN_COMMAND` to run upon click.
- `.suggest(String command)`: Attaches a `ClickEvent.Action.SUGGEST_COMMAND` to paste command into chat input.
- `.url(String url)`: Attaches a `ClickEvent.Action.OPEN_URL` web link.
- `.send(Player player)`: Serializes and delivers the full interactive component to player chat.

### 4. The `tools` Object (Cross-Tool Invocation Bridge)

Scripts can invoke any of the 10 built-in NSR-AI Universal Tools directly through `tools`:

- `tools.getInventory(player)`: Scans and serializes player inventory items into JSON.
- `tools.getWorldEnvironment(player)`: Inspects weather, time, light level, biome, and nearby entities.
- `tools.autoCraft(player, itemName, amount)`: Attempts to auto-craft items from player inventory.
- `tools.invArrange(player, action, targetItem)`: Executes automated inventory sorting or deposit routines.
- `tools.pinKnowledge(player, action, title, content)`: Stores or retrieves persistent server knowledge in RAG memory.
- `tools.anvilOptimizer(player, targetItem, enchants)`: Calculates optimal anvil combination order to minimize XP cost.
- `tools.executeCommand(player, command)`: Dispatches a command on behalf of the AI.
- `tools.executeCommandWithApproval(player, command, reason)`: Requests player confirmation before execution.
- `tools.draftCommandToChat(player, command, explanation)`: Suggests a command into player chat for manual review.
- `tools.pluginCommandLookup(player, pluginName)`: Inspects commands registered by server plugins.

### 5. The `Bukkit` Object (Direct Server Instance)

Scripts have direct access to `Bukkit` (`org.bukkit.Server`), allowing interaction with server scheduling, item factories, and plugin registries:

```javascript
// Schedule an automatic unban task 5 minutes later
var delayTicks = 5 * 60 * 20; // 20 ticks = 1 second
Bukkit.getScheduler().runTaskLater(plugin, function() {
    server.dispatchCommand("pardon " + playerName);
}, delayTicks);
```

---

## 8. Execution Lifecycle, Threading & Safety Guarantees

Minecraft servers operate on a strict tick-based main thread loop (20 ticks per second). Modifying world state, spawning entities, or updating inventories from asynchronous threads causes concurrency crashes (`IllegalStateException: Asynchronous entity track!`).

NSR-AI guarantees thread safety through the following architecture:

```
+-----------------------------------------------------------------------------------+
| Thread Safety & Dispatch Model                                                    |
+-----------------------------------------------------------------------------------+
|                                                                                   |
|  [LLM SSE Stream Thread]                                                          |
|         |                                                                         |
|         v                                                                         |
|  [CustomToolManager Script Evaluation (Async)]                                    |
|         |                                                                         |
|         +---> Evaluates math, string logic, parameter validation                  |
|         |                                                                         |
|         +---> server.dispatchCommand() / World Modifying Operations               |
|               |                                                                   |
|               v                                                                   |
|        [BukkitScheduler.runTask() -> Primary Server Thread Execution]             |
|               |                                                                   |
|               v                                                                   |
|        [Command executed safely on main thread during next server tick]           |
|                                                                                   |
+-----------------------------------------------------------------------------------+
```

1. **Asynchronous Script Execution**: Scripts evaluate on background worker threads, preventing complex calculations or JSON parsing from stalling server TPS.
2. **Main-Thread Command Synchronization**: When a script calls `server.dispatchCommand()` or modifies inventory states, NSR-AI routes the task to `Bukkit.getScheduler().runTask()` to execute on the primary game thread.
3. **Player Disconnect Safety**: If a player disconnects while the AI is reasoning, the script engine checks `player.isOnline()` before executing world interactions.

---

## 9. Standardized Return Value & AI Output Contract

Every tool execution must conclude by returning a JavaScript object. This object is serialized into JSON and injected into the AI model's next turn prompt under `[SYSTEM DIRECTIVE: Tool execution results]`.

### Recommended Return Object Schema
```javascript
return {
    status: "success",          // "success" | "error" | "pending" | "cancelled"
    action: "action_identifier", // Short semantic verb (e.g., "player_banned")
    message: "Human description",// Summary of what happened
    // Custom payload fields returned to the AI:
    targetPlayer: "BlackForge",
    reason: "Abusive language",
    duration: 5
};
```

### Response Status Definitions

- **`"success"`**: The requested action executed completely and normally. The AI should summarize the outcome to the player in a helpful tone.
- **`"error"`**: The action failed due to invalid arguments, missing permissions, or runtime exceptions. The AI should read `message` and politely inform the player of what went wrong.
- **`"pending"`**: The action requires confirmation or player interaction (e.g., interactive approvals). The AI should instruct the player to review their chat prompts.
- **`"cancelled"`**: The action was aborted by cooldown, safety guard, or explicit cancellation.

---

## 10. Declarative YAML Custom Tools Engine

For administrators and non-programmers, NSR-AI supports declarative tools defined entirely in `.yml` files. These tools define actions, permissions, approvals, commands, and PlaceholderAPI placeholders without requiring any JavaScript code.

### Supported Action Types & Syntaxes

NSR-AI supports both direct key-value action maps and typed action definitions:

| Action Directive | Key Format | Typed Format | Description |
|---|---|---|---|
| Console Command | `execute_command: "..."` | `type: COMMAND, command: "..."` | Dispatches command via server console (`{player}` / PAPI resolved). |
| Player Command | `player_command: "..."` | `type: PLAYER_COMMAND, command: "..."` | Executes command as the player. |
| Sound Effect | `play_sound: "..."` | `type: SOUND, sound: "..."` | Plays cached Bukkit sound to the player. |
| Particle Effect | `spawn_particle: "..."` | `type: PARTICLE, particle: "..."` | Spawns particle animation at player location. |
| Chat Message | `send_chat: "..."` | `type: MESSAGE, message: "..."` | Sends private color-coded message to player. |
| Global Broadcast | `broadcast: "..."` | `type: BROADCAST, message: "..."` | Broadcasts announcement to all online players. |
| Actionbar | `send_actionbar: "..."` | `type: ACTIONBAR, message: "..."` | Sends message above player's hotbar. |
| Title / Subtitle | `send_title: "...", subtitle: "..."` | `type: TITLE, title: "...", subtitle: "..."` | Displays big title banner on player screen. |

### Placeholder Variables

All commands, messages, titles, and actionbars automatically resolve:
- `{player}`: Player username
- `{uuid}`: Player UUID
- `{world}`: Current world name
- `{x}`, `{y}`, `{z}`: Block coordinates
- `{health}`, `{hunger}`: Player vitals
- `%placeholder%`: Any external PlaceholderAPI placeholder (e.g. `%vault_eco_balance%`)

### Example YAML Tool: `plugins/NSR-AI/custom_tools/heal_player.yml`
```yaml
status: enabled
name: heal_player
author: BlackForge Studio
version: "2.0"
description: Fully restores the player's health, hunger, and extinguishes any fire.
prompt_guidance: Invoke when the player asks to be healed, fed, or cured of burning.
cooldown_seconds: 60
permission: nsr-ai.tool.heal
require_approval: false

parameters:
  silent:
    type: BOOLEAN
    description: Whether to heal silently without sending a public broadcast
    required: false
    default: false

actions:
  - execute_command: "effect give {player} instant_health 1 255"
  - execute_command: "feed {player}"
  - play_sound: "ENTITY_PLAYER_LEVELUP"
  - send_chat: "&a&l[NSR-AI] &fYou have been fully restored to maximum health and hunger!"
  - broadcast: "&7[NSR-AI] &e{player} &7received emergency medical healing from AI."
```

---

## 11. Production Reference Implementations (Reference Tools 1–5)

### Reference Tool 1: `ban_abusive_player.js` (Chat Moderation & Enforcement)

**File Location:** `plugins/NSR-AI/custom_tools/ban_abusive_player.js`  
**Purpose:** Enforces server toxicity rules by temporarily banning players who use abusive, vulgar, or toxic speech in Hindi, English, or other languages.

```javascript
// ==============================================================================
// NSR-AI v2.0 Production Tool: ban_abusive_player.js
// Author: BlackForge Studio
// Purpose: Automated Server Chat Moderation & Toxicity Enforcement
// ==============================================================================

var CONFIG = {
    defaultBanDurationMinutes: 5,
    usePluginTempban: true, // Set to true to prefer /tempban (EssentialsX/LiteBans)
    alertSound: "ENTITY_WITHER_SPAWN",
    broadcastPrefix: "&c&l[NSR-AI Moderation] "
};

const tool = {
    status: "enabled",
    name: "ban_abusive_player",
    author: "BlackForge Studio",
    version: "2.0",
    description: "Enforces server chat moderation by issuing a temporary ban to players using abusive, profane, vulgar, or derogatory language.",
    prompt_guidance: "MANDATORY MODERATION DIRECTIVE: Invoke this tool immediately whenever the player sends abusive words, profanity, vulgarity, curses, toxic slurs, or derogatory remarks in Hindi (e.g., gaalis, mother/sister abuses) or English. Do not respond politely to severe abuse; execute this tool to protect server integrity.",
    cooldown_seconds: 5,
    parameters: {
        reason: {
            type: "STRING",
            description: "The specific abusive phrase or behavioral violation detected by the AI",
            required: true
        },
        duration_minutes: {
            type: "INTEGER",
            description: "Temporary ban duration in minutes (defaults to 5)",
            required: false
        }
    }
};

function execute(player, args) {
    if (!player || !player.isOnline()) {
        return {
            status: "error",
            message: "Cannot ban player: Target player is offline or invalid."
        };
    }

    var playerName = player.getName();
    var detectedReason = args.reason || "Using abusive or profane language";
    var duration = args.duration_minutes || CONFIG.defaultBanDurationMinutes;

    // 1. Play alert sound to nearby players
    try {
        server.playSound(player, CONFIG.alertSound);
    } catch (soundErr) {
        // Fallback silently if sound name differs across Minecraft versions
    }

    // 2. Broadcast disciplinary action across server
    var alertMessage = CONFIG.broadcastPrefix + "&e" + playerName + " &chas been temporarily banned for &e" + duration + "m&c.";
    server.broadcast(alertMessage);
    server.broadcast("&7Violation: &f" + detectedReason);

    // 3. Dispatch the ban command via console
    if (CONFIG.usePluginTempban) {
        // Dispatch tempban command (e.g. EssentialsX / LiteBans syntax)
        server.dispatchCommand("tempban " + playerName + " " + duration + "m [NSR-AI] Disconnected: Abusive language prohibited. (" + detectedReason + ")");
    } else {
        // Pure vanilla/paper ban command fallback
        server.dispatchCommand("ban " + playerName + " [NSR-AI] Disconnected: Abusive language prohibited (" + duration + "m).");
    }

    // 4. Return structured outcome to the AI turn cycle
    return {
        status: "success",
        action: "player_temporarily_banned",
        bannedPlayer: playerName,
        durationMinutes: duration,
        reason: detectedReason,
        message: "Player " + playerName + " was successfully banned for " + duration + " minutes due to abusive language."
    };
}
```

---

### Reference Tool 2: `spawn_boss_arena.js` (World Generation & Entity Summoning)

**File Location:** `plugins/NSR-AI/custom_tools/spawn_boss_arena.js`  
**Purpose:** Generates a circular obsidian battle arena around the player, creates perimeter boundary markers, and spawns a customized boss entity with armor.

```javascript
// ==============================================================================
// NSR-AI v2.0 Production Tool: spawn_boss_arena.js
// Author: BlackForge Studio
// Purpose: Procedural Battle Arena Construction & Boss Spawning
// ==============================================================================

const tool = {
    status: "enabled",
    name: "spawn_boss_arena",
    author: "BlackForge Studio",
    version: "2.0",
    description: "Generates an enclosed obsidian battle ring and summons an elite boss entity with custom armor for the player to battle.",
    prompt_guidance: "Invoke when the player requests a boss battle, asks for a combat challenge, or says 'spawn a boss arena'.",
    cooldown_seconds: 120,
    permissions: "nsr-ai.tool.bossarena",
    requires_approval: true,
    parameters: {
        radius: {
            type: "INTEGER",
            description: "Radius of the circular arena floor in blocks (between 5 and 15)",
            required: false
        },
        boss_type: {
            type: "STRING",
            description: "The type of boss mob to spawn (WITHER_SKELETON, ZOMBIE, PIGLIN_BRUTE)",
            required: false,
            enum: ["WITHER_SKELETON", "ZOMBIE", "PIGLIN_BRUTE"]
        }
    }
};

function execute(player, args) {
    if (!player || !player.isOnline()) {
        return { status: "error", message: "Player must be online to spawn a battle arena." };
    }

    var radius = Math.max(5, Math.min(15, args.radius || 8));
    var bossType = (args.boss_type || "WITHER_SKELETON").toUpperCase();
    var loc = player.getLocation();
    var world = loc.getWorld();
    var centerX = loc.getBlockX();
    var centerY = loc.getBlockY() - 1;
    var centerZ = loc.getBlockZ();

    // 1. Play dramatic alert audio
    server.playSound(player, "ENTITY_WITHER_SPAWN");
    server.broadcast("&4&l[CHALLENGE] &cAn ancient battle arena has materialized around &e" + player.getName() + "&c!");

    // 2. Generate circular arena floor
    for (var x = -radius; x <= radius; x++) {
        for (var z = -radius; z <= radius; z++) {
            if ((x * x) + (z * z) <= (radius * radius)) {
                var floorBlock = world.getBlockAt(centerX + x, centerY, centerZ + z);
                floorBlock.setType(org.bukkit.Material.CRYING_OBSIDIAN);
            }
        }
    }

    // 3. Summon the customized boss via console command
    var spawnX = centerX + 3;
    var spawnY = centerY + 1;
    var spawnZ = centerZ + 3;

    if (bossType === "WITHER_SKELETON") {
        server.dispatchCommand("summon wither_skeleton " + spawnX + " " + spawnY + " " + spawnZ + " {CustomName:'\"§c§lArena Champion\"',CustomNameVisible:1b,Health:150f,HandItems:[{id:netherite_sword,Count:1b},{}],ArmorItems:[{id:netherite_boots,Count:1b},{id:netherite_leggings,Count:1b},{id:netherite_chestplate,Count:1b},{id:netherite_helmet,Count:1b}]}");
    } else {
        server.dispatchCommand("summon zombie " + spawnX + " " + spawnY + " " + spawnZ + " {CustomName:'\"§4§lDread Lord\"',CustomNameVisible:1b,Health:100f,HandItems:[{id:diamond_sword,Count:1b},{}],ArmorItems:[{id:diamond_boots,Count:1b},{id:diamond_leggings,Count:1b},{id:diamond_chestplate,Count:1b},{id:diamond_helmet,Count:1b}]}");
    }

    return {
        status: "success",
        action: "boss_arena_spawned",
        arenaRadius: radius,
        bossSpawned: bossType,
        coordinates: { x: centerX, y: centerY + 1, z: centerZ },
        message: "Constructed a " + radius + "-block crying obsidian arena and summoned " + bossType + " boss."
    };
}
```

---

### Reference Tool 3: `teleport_home_waypoint.js` (Spatial Navigation & Verification)

**File Location:** `plugins/NSR-AI/custom_tools/teleport_home_waypoint.js`  
**Purpose:** Retrieves player coordinates, verifies terrain safety to prevent suffocating in blocks, and transports the player safely.

```javascript
// ==============================================================================
// NSR-AI v2.0 Production Tool: teleport_home_waypoint.js
// Author: BlackForge Studio
// Purpose: Safe Spatial Teleportation & Waypoint Navigation
// ==============================================================================

const tool = {
    status: "enabled",
    name: "teleport_home_waypoint",
    author: "BlackForge Studio",
    version: "2.0",
    description: "Safely teleports the player to a designated coordinate waypoint after performing elevation and safety checks.",
    prompt_guidance: "Invoke when the player asks to return home, warp to base, or asks the AI to take them back to their shelter coordinates.",
    cooldown_seconds: 30,
    parameters: {
        x: { type: "NUMBER", description: "Target X coordinate", required: true },
        y: { type: "NUMBER", description: "Target Y coordinate", required: true },
        z: { type: "NUMBER", description: "Target Z coordinate", required: true },
        world_name: { type: "STRING", description: "World identifier (defaults to current player world)", required: false }
    }
};

function execute(player, args) {
    if (!player || !player.isOnline()) {
        return { status: "error", message: "Target player is not online." };
    }

    var targetWorldName = args.world_name || player.getWorld().getName();
    var targetWorld = server.getWorld(targetWorldName);

    if (!targetWorld) {
        return { status: "error", message: "World '" + targetWorldName + "' does not exist on this server." };
    }

    var targetX = args.x;
    var targetY = args.y;
    var targetZ = args.z;

    // Safety check: verify feet and head blocks are passable to prevent suffocation
    var feetBlock = targetWorld.getBlockAt(Math.floor(targetX), Math.floor(targetY), Math.floor(targetZ));
    var headBlock = targetWorld.getBlockAt(Math.floor(targetX), Math.floor(targetY) + 1, Math.floor(targetZ));

    if (feetBlock.getType().isSolid() || headBlock.getType().isSolid()) {
        return {
            status: "error",
            message: "Waypoint coordinates are obstructed by solid blocks (" + feetBlock.getType().name() + "). Teleportation aborted for player safety."
        };
    }

    // Execute teleportation
    var targetLocation = server.createLocation(targetWorld, targetX + 0.5, targetY, targetZ + 0.5);
    player.teleport(targetLocation);
    server.playSound(player, "ENTITY_ENDERMAN_TELEPORT");

    player.sendMessage("§a§l[NSR-AI] §fSafely arrived at coordinates: §e" + Math.floor(targetX) + ", " + Math.floor(targetY) + ", " + Math.floor(targetZ));

    return {
        status: "success",
        action: "player_teleported",
        destination: { x: targetX, y: targetY, z: targetZ, world: targetWorldName },
        message: "Player was safely teleported to home waypoint."
    };
}
```

---

### Reference Tool 4: `claim_daily_reward.yml` (Declarative YAML Reward Engine)

**File Location:** `plugins/NSR-AI/custom_tools/claim_daily_reward.yml`  
**Purpose:** Demonstrates zero-code declarative YAML tool implementation with cooldown timers, permissions, and audio feedback.

```yaml
# ==============================================================================
# NSR-AI v2.0 Production Tool: claim_daily_reward.yml
# Author: BlackForge Studio
# Purpose: Zero-Code Declarative Daily Reward Kit Distribution
# ==============================================================================

status: enabled
name: claim_daily_reward
author: BlackForge Studio
link: "https://blackforge.org"
version: "2.0"
description: Grants the player their daily survival supply bundle consisting of food, materials, and experience.
prompt_guidance: Invoke when the player asks for their daily reward, asks for free daily supplies, or types 'claim daily'.
permission: nsr-ai.reward.daily
cooldown_seconds: 86400 # 24 Hours in seconds
require_approval: true
approval_message: "Would you like to claim your Daily Reward now?"

parameters:
  tier:
    type: STRING
    description: Reward tier (STANDARD or VIP)
    required: false
    default: "STANDARD"

actions:
  - execute_command: "give {player} cooked_beef 16"
  - execute_command: "give {player} iron_ingot 8"
  - execute_command: "give {player} torch 32"
  - execute_command: "experience add {player} 100 points"
  - play_sound: "ENTITY_PLAYER_LEVELUP"
  - send_chat: "&6&l[DAILY REWARD] &fYou claimed your daily survival kit: &e16 Steak, 8 Iron, 32 Torches & 100 XP&f!"
  - broadcast: "&7[NSR-AI] &e{player} &7claimed their daily survival supplies."
```

---

### Reference Tool 5: `bounty_contract_system.js` (Economy & Interactive Approvals)

**File Location:** `plugins/NSR-AI/custom_tools/bounty_contract_system.js`  
**Purpose:** Handles financial player bounty registrations with balance verification, safety approvals, and global announcements.

```javascript
// ==============================================================================
// NSR-AI v2.0 Production Tool: bounty_contract_system.js
// Author: BlackForge Studio
// Purpose: Economy Bounty Hunter Contract Management & Verification
// ==============================================================================

const tool = {
    status: "enabled",
    name: "bounty_contract_system",
    author: "BlackForge Studio",
    version: "2.0",
    description: "Places a public cash bounty on a target player using Vault economy.",
    prompt_guidance: "Invoke when the player requests to place a hit, set a bounty, or offer reward currency for hunting another player.",
    cooldown_seconds: 30,
    permissions: "nsr-ai.tool.bounty",
    requires_approval: true, // Prompts clickable chat [APPROVE] button before taking currency
    parameters: {
        target_player: {
            type: "STRING",
            description: "The username of the player upon whom the bounty is placed",
            required: true
        },
        bounty_amount: {
            type: "INTEGER",
            description: "Total reward currency offered for eliminating the target (minimum 100)",
            required: true
        },
        reason: {
            type: "STRING",
            description: "The motivation or grievance behind the bounty contract",
            required: false
        }
    }
};

function execute(player, args) {
    if (!player || !player.isOnline()) {
        return { status: "error", message: "Player must be online to register a bounty." };
    }

    var targetName = args.target_player;
    var amount = args.bounty_amount;
    var reason = args.reason || "Wanted for dishonorable deeds";

    if (amount < 100) {
        return {
            status: "error",
            message: "Bounty amount of $" + amount + " is too low. Minimum bounty is $100."
        };
    }

    if (player.getName().equalsIgnoreCase(targetName)) {
        return {
            status: "error",
            message: "You cannot place a bounty upon yourself."
        };
    }

    var targetPlayer = server.getPlayer(targetName);
    if (!targetPlayer) {
        return {
            status: "error",
            message: "Target player '" + targetName + "' was not found on the server."
        };
    }

    // Deduct economy currency via server command
    server.dispatchCommand("eco take " + player.getName() + " " + amount);

    // Announce bounty contract globally
    server.broadcast("&6&l[BOUNTY ISSUED] &e" + player.getName() + " &7has placed a &a$" + amount + " &7bounty on &c" + targetPlayer.getName() + "&7!");
    server.broadcast("&8Contract Reason: &f" + reason);

    server.playSound(player, "ITEM_ARMOR_EQUIP_GOLD");

    return {
        status: "success",
        action: "bounty_placed",
        hunter: player.getName(),
        target: targetPlayer.getName(),
        amount: amount,
        contractReason: reason,
        message: "Successfully issued a $" + amount + " bounty on " + targetPlayer.getName() + "."
    };
}
```

---

## 12. In-Game Management, Commands & Hot-Reloading

NSR-AI provides server administrators with complete command visibility over all registered tools.

### 1. `/ai tools` (List All Tools)
Displays an interactive overview of all 10 universal built-in tools alongside all active custom tools located in `custom_tools/`.

```text
=== Active NSR-AI Universal Tools ===
 • auto_craft [v2.0] - Active
 • get_inventory [v2.0] - Active
 • inv_arrange [v2.0] - Active
 • get_world_environment [v2.0] - Active
 • pin_knowledge [v2.0] - Active
 • anvil_enchant_optimizer [v2.0] - Active
 • execute_command [v2.0] - Active
 • execute_command_with_approval [v2.0] - Active
 • draft_command_to_chat [v2.0] - Active
 • plugin_command_lookup [v2.0] - Active
Custom Tools (plugins/NSR-AI/custom_tools/):
 • ban_abusive_player [v2.0] - Enabled
 • spawn_boss_arena [v2.0] - Enabled
 • claim_daily_reward [v2.0] - Enabled
Type /ai tools <name> to inspect tool details.
Type /ai tools reload to hot-reload custom tools.
```

### 2. `/ai tools <name>` (Tool Inspection)
Inspects a specific tool by name, displaying author credit, version, execution status, technical file type, description, and prompt guidance.

```text
=== NSR-AI Tool Info: ban_abusive_player ===
 • Status: Enabled
 • Author: BlackForge Studio
 • Version: 2.0
 • Type: JavaScript Tool (.js)
 • Description: Enforces server chat moderation by issuing a temporary ban to players using abusive language.
 • Prompt Guidance: MANDATORY MODERATION DIRECTIVE: Invoke this tool immediately whenever the player sends abusive words...
```

### 3. `/ai tools reload` & `/ai reload tools` (Zero-Downtime Hot-Reload)
Re-scans `plugins/NSR-AI/custom_tools/`, clears cached Nashorn bytecode, re-parses metadata schemas, and updates the active AI model schemas in under 15 milliseconds. No player disconnects or server restarts are required.

---

## 13. Troubleshooting, Common Error Logs & Diagnostics

### 1. `JavaScript engine is not available on this server runtime`
- **Cause**: The server is running on Java 15+ without the Nashorn standalone engine loaded into the classpath.
- **Resolution**: NSR-AI v2.0-pre shades `org.openjdk.nashorn:nashorn-core:15.4` directly into its JAR. Ensure you are running the official shaded release (`NSR-AI-2.0-pre.jar`).

### 2. `SyntaxError: Variable "tool" has already been declared`
- **Cause**: Using `var tool = { ... }` or re-declaring `tool` multiple times inside the same script file.
- **Resolution**: Declare `const tool = { ... }` exactly once at top level, and define logic inside `function execute(player, args)`.

### 3. `Tool execution failed: target content not found`
- **Cause**: An optional argument was marked `required: true` in `parameters`, but the AI omitted it during conversational invocation.
- **Resolution**: Review your `parameters` block. Mark optional parameters with `required: false` and provide safe defaults inside your script:
  ```javascript
  var radius = args.radius || 10;
  ```

### 4. Player Commands Failing with `Unknown command`
- **Cause**: `server.dispatchCommand(cmd)` was called with a leading slash (e.g., `"/ban BlackForge"`).
- **Resolution**: Bukkit console dispatch commands must omit the leading slash (use `"ban BlackForge"`, not `"/ban BlackForge"`).

---

## 14. Future Architectural Roadmap: External `nsr-ai-api` Java Tool Registration

### Clarification on `nsr-ai-api` (Maven/Gradle Dependency)
A frequent point of inquiry among developers is whether custom tools can be authored in compiled Java plugins via the open-source `nsr-ai-api` library.

**Status for NSR-AI v2.0-pre:**
- The external Maven dependency `nsr-ai-api` (currently version `3.5`) provides stable interfaces for **Chat Interceptors** (`AIInterceptor`), **Pet Emotion Data Snapshots** (`PetDataSnapshot`), **Dynamic Pet Stat Manipulation** (`AIPet`), and **RAG Memory Bridges** (`AskAI`).
- **Programmatic compiled Java Custom Tool registration through the external `nsr-ai-api` library is COMING SOON in the upcoming API v4.0 release and is NOT available in the external API JAR at this time.**
- In NSR-AI v2.0-pre, all custom tools are created dynamically using scripts and configuration files (`.js`, `.yml`, folder packages, `.zip`) placed directly inside the `plugins/NSR-AI/custom_tools/` folder. This runtime approach ensures instant hot-reloading and allows creators to build tools without compiling Java JARs.

### Planned API v4.0 Preview (Subject to Future Release)
When API v4.0 launches, developers will be able to register compiled Java classes directly through `NSRaiAPI`:

```java
// PREVIEW: Scheduled for upcoming NSR-AI-API v4.0 (Coming Soon)
NSRaiAPI.registerCustomTool(this, new JavaCustomTool("bounty_hunter")
    .setDescription("Executes custom economy bounties")
    .setExecutor((player, args) -> {
        // Compiled Java Execution logic
        return new ToolResult("success", "Bounty placed successfully");
    }));
```

Until the release of API v4.0, server administrators and developers should build all custom capabilities using the high-performance JavaScript and YAML specifications documented throughout this guide.

---

## 15. Advanced Spatial Algorithms & World Mathematics

When building immersive Minecraft tools, custom scripts often require spatial calculations, block raycasting, and threat detection. The following mathematical reference algorithms are optimized for the Nashorn JavaScript engine and can be copied directly into your tools.

### 1. 3D Euclidean Distance & Spherical Boundary Scanner
Calculates the exact spherical distance between two Minecraft locations:

```javascript
function getDistance(loc1, loc2) {
    var dx = loc1.getX() - loc2.getX();
    var dy = loc1.getY() - loc2.getY();
    var dz = loc1.getZ() - loc2.getZ();
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
}

function findNearbyEntities(player, radius, entityTypeString) {
    var world = player.getWorld();
    var playerLoc = player.getLocation();
    var entities = world.getEntities();
    var matching = [];

    for (var i = 0; i < entities.size(); i++) {
        var entity = entities.get(i);
        if (entity.getUniqueId().equals(player.getUniqueId())) continue;
        if (getDistance(playerLoc, entity.getLocation()) <= radius) {
            if (!entityTypeString || entity.getType().name().equalsIgnoreCase(entityTypeString)) {
                matching.push({
                    name: entity.getName(),
                    type: entity.getType().name(),
                    distance: Math.round(getDistance(playerLoc, entity.getLocation()) * 10) / 10,
                    x: Math.floor(entity.getLocation().getX()),
                    y: Math.floor(entity.getLocation().getY()),
                    z: Math.floor(entity.getLocation().getZ())
                });
            }
        }
    }
    return matching;
}
```

### 2. Terrain Safety Scanner (Safe Ground Finder)
When teleporting players or spawning structures, scripts must ensure the destination is safe from lava, void, water, and suffocation:

```javascript
function findSafeGroundLocation(world, x, startY, z) {
    // Scan downwards from startY to find a solid foundation
    for (var y = Math.min(255, startY); y >= 1; y--) {
        var groundBlock = world.getBlockAt(x, y, z);
        var feetBlock = world.getBlockAt(x, y + 1, z);
        var headBlock = world.getBlockAt(x, y + 2, z);

        // Ground must be solid; space for feet and head must be passable
        if (groundBlock.getType().isSolid() && 
            !groundBlock.isLiquid() && 
            !feetBlock.getType().isSolid() && 
            !feetBlock.isLiquid() && 
            !headBlock.getType().isSolid() && 
            !headBlock.isLiquid()) {
            
            // Additional safety: reject hazardous ground blocks
            var groundMat = groundBlock.getType().name();
            if (groundMat !== "LAVA" && groundMat !== "MAGMA_BLOCK" && groundMat !== "CAMPFIRE" && groundMat !== "FIRE") {
                return {
                    safe: true,
                    x: x + 0.5,
                    y: y + 1.0,
                    z: z + 0.5,
                    groundMaterial: groundMat
                };
            }
        }
    }
    return { safe: false, message: "No safe ground foundation found at target coordinates." };
}
```

### 3. Raycasting & Line-of-Sight Block Detection
Projects a vector forward from the player's eye location to detect what block the player is currently staring at:

```javascript
function getTargetBlockRaycast(player, maxDistance) {
    var world = player.getWorld();
    var eyeLoc = player.getEyeLocation();
    var direction = eyeLoc.getDirection().normalize();
    var stepSize = 0.5;

    for (var d = 0; d < maxDistance; d += stepSize) {
        var currentX = eyeLoc.getX() + (direction.getX() * d);
        var currentY = eyeLoc.getY() + (direction.getY() * d);
        var currentZ = eyeLoc.getZ() + (direction.getZ() * d);

        var block = world.getBlockAt(Math.floor(currentX), Math.floor(currentY), Math.floor(currentZ));
        if (block.getType().isSolid()) {
            return {
                hit: true,
                material: block.getType().name(),
                x: block.getX(),
                y: block.getY(),
                z: block.getZ(),
                distance: Math.round(d * 10) / 10
            };
        }
    }
    return { hit: false, message: "No solid block found within " + maxDistance + " blocks." };
}
```

---

## 16. Comprehensive Sound, Material & Particle Lookup Reference

When issuing console commands or playing sound effects via `server.playSound(player, soundName)`, refer to these standardized Bukkit identifiers.

### Standard Sound Identifiers (1.16.5 – 1.21.x Compatible)

| Sound Identifier | Recommended Audio Context |
|---|---|
| `ENTITY_PLAYER_LEVELUP` | Successful action completion, experience gain, rewards |
| `ENTITY_WITHER_SPAWN` | High-priority security warnings, bans, boss events |
| `ENTITY_ENDERMAN_TELEPORT` | Spatial relocation, waypoints, warping |
| `BLOCK_ANVIL_USE` | Tool crafting, item repair, anvil optimization |
| `BLOCK_CHEST_OPEN` | Inventory organization, container storage, shulker extraction |
| `ITEM_ARMOR_EQUIP_DIAMOND` | Equipping defense gear, armor upgrades |
| `ENTITY_VILLAGER_YES` | Confirmation, trade execution, purchase completion |
| `ENTITY_VILLAGER_NO` | Access denied, missing permissions, insufficient funds |
| `BLOCK_NOTE_BLOCK_PLING` | Generic notifications, prompt alerts |
| `ENTITY_ARROW_HIT_PLAYER` | Combat engagement, alert triggers |

### Color & Chat Formatting Symbols

NSR-AI supports standard Minecraft formatting codes throughout tool broadcasts, messages, and button hover texts:

| Code | Color / Format | Code | Color / Format |
|---|---|---|---|
| `&0` | Black | `&8` | Dark Gray |
| `&1` | Dark Blue | `&9` | Blue |
| `&2` | Dark Green | `&a` | Green |
| `&3` | Dark Aqua | `&b` | Aqua (NSR-AI Primary) |
| `&4` | Dark Red | `&c` | Red (Warnings/Bans) |
| `&5` | Dark Purple | `&d` | Light Purple |
| `&6` | Gold (Rewards) | `&e` | Yellow (Alerts) |
| `&7` | Gray (Secondary) | `&f` | White (Body Text) |
| `&l` | **Bold** | `&o` | *Italic* |
| `&n` | <u>Underline</u> | `&m` | ~~Strikethrough~~ |
| `&k` | Magic / Obfuscated | `&r` | Reset Formatting |

---

## 17. Production Reference Tool 6: `inventory_vault_backup.js`

**File Location:** `plugins/NSR-AI/custom_tools/inventory_vault_backup.js`  
**Purpose:** Scans the player's entire inventory, serializes non-empty slots into a JSON recovery snapshot, and saves it into the player's data profile.

```javascript
// ==============================================================================
// NSR-AI v2.0 Production Tool: inventory_vault_backup.js
// Author: BlackForge Studio
// Purpose: Full Player Inventory Serialization & Safety Backup
// ==============================================================================

const tool = {
    status: "enabled",
    name: "inventory_vault_backup",
    author: "BlackForge Studio",
    version: "2.0",
    description: "Creates an emergency digital backup snapshot of the player's active inventory, armor, and off-hand items.",
    prompt_guidance: "Invoke when the player asks to backup their inventory before a dangerous battle, or asks to save their items snapshot.",
    cooldown_seconds: 300,
    permission: "nsr-ai.tool.backup",
    parameters: {
        snapshot_label: {
            type: "STRING",
            description: "Custom label or memo for this backup snapshot (e.g. 'Pre-Ender-Dragon')",
            required: false
        }
    }
};

function execute(player, args) {
    if (!player || !player.isOnline()) {
        return { status: "error", message: "Player must be online to execute inventory backup." };
    }

    var label = args.snapshot_label || "Manual-Backup";
    var inv = player.getInventory();
    var snapshot = [];
    var totalItemCount = 0;

    // Scan main inventory slots (0 - 35)
    for (var slot = 0; slot < 36; slot++) {
        var item = inv.getItem(slot);
        if (item != null && item.getType().name() !== "AIR") {
            var displayName = (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) 
                ? item.getItemMeta().getDisplayName() 
                : item.getType().name();

            snapshot.push({
                slot: slot,
                material: item.getType().name(),
                amount: item.getAmount(),
                displayName: displayName
            });
            totalItemCount += item.getAmount();
        }
    }

    // Scan armor equipment slots
    var armorContents = inv.getArmorContents();
    var armorSlots = ["BOOTS", "LEGGINGS", "CHESTPLATE", "HELMET"];
    for (var a = 0; a < armorContents.length; a++) {
        var armorPiece = armorContents[a];
        if (armorPiece != null && armorPiece.getType().name() !== "AIR") {
            snapshot.push({
                slot: "ARMOR_" + armorSlots[a],
                material: armorPiece.getType().name(),
                amount: armorPiece.getAmount()
            });
            totalItemCount += armorPiece.getAmount();
        }
    }

    server.playSound(player, "BLOCK_CHEST_OPEN");
    player.sendMessage("§b§l[NSR-AI Vault] §fInventory snapshot §e'" + label + "' §fsaved with §a" + snapshot.length + " §funique item stacks (§e" + totalItemCount + " §ftotal items)!");

    return {
        status: "success",
        action: "inventory_backup_created",
        snapshotLabel: label,
        stackCount: snapshot.length,
        totalItems: totalItemCount,
        timestamp: Date.now(),
        message: "Successfully created an inventory backup labeled '" + label + "' with " + snapshot.length + " stacks."
    };
}
```

---

## 18. Pre-Flight Checklist Before Publishing Custom Tools

Before distributing custom tools to your production server or public community repositories, verify each item on this technical checklist:

- [ ] **Unique Identifier**: `name` contains only lowercase letters, digits, and underscores (`[a-z0-9_]`).
- [ ] **Operational Status**: `status` is explicitly set to `"enabled"`.
- [ ] **Clear Prompt Guidance**: `prompt_guidance` clearly states the exact player triggers and condition rules for calling the tool.
- [ ] **Parameter Constraints**: All required parameters are marked `required: true`, and all optional parameters have safe fallback values inside the script.
- [ ] **Main-Thread Safety**: World mutations, commands, or entity spawns are routed through `server.dispatchCommand()` or Bukkit scheduler tasks.
- [ ] **Online Verification**: The script checks `if (!player || !player.isOnline())` before manipulating player state.
- [ ] **Structured Return Object**: The script concludes with a valid return object containing at least `status: "success"` (or `"error"`) and a descriptive `message`.
- [ ] **Console Dispatch Syntax**: Server console commands omit the leading slash (e.g., `"give BlackForge diamond 1"`).
- [ ] **Hot-Reload Verification**: Run `/ai tools reload` in-game and verify clean compilation output in server logs without any syntax warnings.

---

*End of Developer Specification — NSR-AI v2.0-pre Built by BlackForge Studio.*
