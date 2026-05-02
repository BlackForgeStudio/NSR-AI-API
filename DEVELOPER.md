# NSR-AI Developer Guide

Welcome, addon developers! This guide provides essential information for creating powerful and integrated addons for the **NSR-AI Minecraft plugin**, now featuring an **expanded Open-Source Addon API**. This API serves as a robust and secure layer, enabling your addons to interact deeply with NSR-AI's core functionalities, offering unprecedented possibilities for customization and extension.

## 1. API Versioning and Compatibility

The NSR-AI API uses a strict versioning system to ensure compatibility between the core plugin and your addons.

*   **`NSRaiAPI.API_VERSION`**: This `public static final int` constant in the `NSRaiAPI` class indicates the current major API version. Addons should check this value to ensure compatibility.

### Checking API Compatibility

It is crucial to check the API version at your addon's `onEnable()` stage.

```java
import com.nsr.ai.api.NSRaiAPI;
import org.bukkit.plugin.Plugin; // Use generic Plugin for compatibility
import org.bukkit.plugin.java.JavaPlugin;

public class MyAddon extends JavaPlugin {

    // Define the API version your addon is built against.
    // This should match the API_VERSION constant in the NSR-AI API you are using.
    private static final int REQUIRED_API_VERSION = 3; 

    @Override
    public void onEnable() {
        // Always check the API version to ensure your addon is compatible with the running NSR-AI core.
        if (NSRaiAPI.getApiVersion() < REQUIRED_API_VERSION) {
            getLogger().severe("NSR-AI API version is too old! " 
                + "Required: " + REQUIRED_API_VERSION 
                + ", Found: " + NSRaiAPI.getApiVersion());
            getServer().getPluginManager().disablePlugin(this); // Disable your addon if incompatible
            return;
        }
        getLogger().info("NSR-AI API version " + NSRaiAPI.getApiVersion() + " detected. Addon enabled.");
        // Proceed with your addon's specific initialization logic here.
    }
}
```

## 2. Safely Detecting Missing Features

The NSR-AI core plugin may not have all features (like specific services) enabled or implemented in every version. The public API is designed to gracefully handle these situations.

*   **Conditional Features:** Methods for features that might not be present (e.g., `addKnowledgeEntry` if the knowledge base is disabled) will throw an `IllegalStateException` if the underlying service is not available in the core plugin.

### Example: Handling Conditional Features

Always wrap calls to conditional features in `try-catch` blocks to prevent your addon from crashing.

```java
import com.nsr.ai.api.NSRaiAPI;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class MyFeatureAddon {

    public void tryAddKnowledge(Player player, String keyword, String content) {
        try {
            NSRaiAPI.addKnowledgeEntry(keyword, "Generated Heading", content);
            player.sendMessage(ChatColor.GREEN + "Knowledge entry added!");
        } catch (IllegalStateException e) {
            player.sendMessage(ChatColor.RED + "NSR-AI Knowledge Base is not supported by this core version or is disabled.");
            NSRaiAPI.getLogger().warning("Could not add knowledge entry: " + e.getMessage());
        }
    }
}
```

*   **Optional Returns:** Methods that return data (e.g., `getPet`, `getPlayerPets`, `getSharedMemory`) will return `null` or empty lists if the feature is not supported or no data is available. 

```java
import com.nsr.ai.api.NSRaiAPI;
import com.nsr.ai.api.AIPet;
import org.bukkit.entity.Player;
import java.util.List;

public class MyPetHelper {

    public void displayPetData(Player player, String petName) {
        // Attempt to retrieve a specific pet.
        AIPet pet = NSRaiAPI.getPet(player, petName);
        if (pet != null) {
            player.sendMessage("Pet " + pet.getName() + " (Level " + pet.getLevel() + ") is feeling " + pet.getMood());
        } else {
            player.sendMessage("No pet found named " + petName);
        }
    }

    public void listAllPets(Player player) {
        List<AIPet> pets = NSRaiAPI.getPlayerPets(player);
        for (AIPet pet : pets) {
            player.sendMessage("- " + pet.getName() + " [" + pet.getType() + "]");
        }
    }
}
```

## 3. Modifying Pet Stats (API v3.0-pre+)
Addons can now programmatically influence the pet system to create training or RPG features.

```java
public class PetTrainer {
    public void trainPet(Player player, String petName) {
        // Increase bond by 5
        AIPet pet = NSRaiAPI.getPet(player, petName);
        if (pet != null) {
            NSRaiAPI.setPetBond(player, petName, pet.getBond() + 5);
            NSRaiAPI.setPetMood(player, petName, "HAPPY");
            player.sendMessage("You trained " + petName + "! Bond increased.");
        }
    }
}
```

## 3. Simple AI Interaction (The Easy Way)

The API now includes simplified methods to communicate with the AI. You no longer need to manually construct `AIMessage` and `AIResponse` objects.

### Example: Asking a question
```java
import com.nsr.ai.api.NSRaiAPI;
import org.bukkit.entity.Player;

public class MyAIHelper {
    public void askQuestion(Player player) {
        // You can use .usePlayerApiKey(player) or .useGlobalApiKey()
        // If neither is specified, it runs in Dual Mode (tries player first, then global).
        NSRaiAPI.askAI("What is the capital of France?")
                .useGlobalApiKey()
                .execute()
                .thenAccept(response -> {
                    System.out.println("AI Response: " + response);
                });
    }
}
```

### Example: Sending a message for a Player
```java
import com.nsr.ai.api.NSRaiAPI;
import org.bukkit.entity.Player;

public class MyAIHelper {
    public void triggerAI(Player player) {
        // Simulates the player sending a chat message to the AI.
        // It will automatically reply back to the player in-game!
        NSRaiAPI.sendMsg(player, "Translate 'Hello' to Spanish.")
                .usePlayerApiKey() // Forces the API to ONLY use the player's personal API key
                .setTemperature(0.5f) // Make it more focused
                .setTopP(0.9f)
                .setMaxTokens(500)
                .execute()
                .thenAccept(v -> {
                    player.sendMessage("Conversation finished!");
                });
    }
}
```

### The Async Engine
Every AI interaction in v3.0-pre is powered by a non-blocking engine. When you call `.execute()`, NSR-AI:
1. Validates the request on the current thread (fast).
2. Hands the request to a dedicated background worker.
3. Immediately returns control to your plugin.
4. Completes the `CompletableFuture` once the AI response is delivered.

**Why this matters:** Even if an AI provider takes 10 seconds to respond, your server's TPS will never drop.

### Advanced: Custom Settings
If you need a setting that isn't built into the API (like a specific model parameter), you can add it manually:
```java
NSRaiAPI.askAI("Write a story.")
        .setCustomSetting("presence_penalty", 0.6)
        .setCustomSetting("frequency_penalty", 0.3)
        .execute();
```

## 5. Model & Provider Management
NSR-AI v1.4 allows addons to control which AI engine is used at three different levels of priority:

1.  **Request Level (Builder)**: Use `.setModel()` or `.setProvider()` in the `AskAI` or `SendChat` builder. This only affects that specific message.
2.  **Player Level**: Use `NSRaiAPI.setPlayerModel(player, "model")` or `NSRaiAPI.setPlayerApiProvider(player, "provider")`. This updates the player's currently active API key settings.
3.  **Global Level**: Use `NSRaiAPI.setModel("model")` or `NSRaiAPI.setApiProvider("provider")`. This updates the server's default fallback settings.

### Key Search Priority:
The core plugin uses an intelligent "Player-First" priority system:
1.  **Player API Key**: If the player has a personal API key, it is used first (using the player's model settings).
2.  **Global API Key**: If the player has no keys, the plugin falls back to the server's global API keys.

## 4. Chat Interceptor System (Middleware)

## 4. Knowledge Base Access
Addons can now read and modify the core NSR-AI knowledge base (`knowledge.yml`).
```java
List<String> keywords = NSRaiAPI.getKnowledgeKeywords();
NSRaiAPI.addKnowledge("rules", "1. No griefing. 2. Be nice.");
NSRaiAPI.removeKnowledge("old_event");
```
*Requires `read-knowledge` and `edit-knowledge` permissions.*

## 3. Command Routing & Aliases
NSR-AI v1.4 introduces a structured command system for addons.

### How it works:
1. **The Alias**: By default, the alias is your addon name (lowercase with dashes). You can override this by implementing `getAlias()` in `AIAddon` or using `.setAlias()` in `SimpleAddon`.
2. **The Namespace**:
   - `/aia <alias> <subcommands...>`: **Strictly reserved for addons.** Use this to route directly to your code.
   - `/ai ...`: **Reserved for main plugin features** (Chat, Pets, Admin). Addons do not route through this command.
3. **Tab Completion**: The API automatically includes your alias in the tab suggestions for `/aia`. If a player types your alias, the subsequent tab suggestions are fetched from your `onTabComplete` implementation.

### Example (SimpleAddon):
```java
NSRaiAPI.registerAddon(new SimpleAddon("AdvancedPlayerStats", "1.0", "Author", "Desc")
    .setAlias("aps") // Users type /aia aps stats
    .addCommand("stats", "View your AI stats")
    .onCommandExecution((player, args) -> {
        if (args.length > 0 && args[0].equalsIgnoreCase("stats")) {
            return "&aFetching your stats...";
        }
        return null;
    })
    .onTabCompleteHandler((player, args) -> Arrays.asList("stats"))
);
```

### Example (Standard AIAddon):
```java
public class MyAddon implements AIAddon {
    @Override
    public String getAlias() { return "aps"; }

    @Override
    public String onCommand(Player player, String[] args) {
        // args[0] will be 'stats' if user typed /aia aps stats
        return "Command received!";
    }
}
```

## 6. AI Interceptors (Middleware)
Interceptors allow you to read, modify, or cancel AI inputs and outputs.
```java
public class MyMiddleware implements AIInterceptor {
    @Override
    public String onPlayerInput(Player player, String input) {
        return input.replace("badword", "***");
    }

    @Override
    public String onSystemPrompt(Player player, String systemPrompt) {
        return systemPrompt + "\nExtra context for the AI.";
    }

    @Override
    public String onAIResponse(Player player, String response) {
        // Add custom branding to every AI response
        return response + "\n\n[Generated via MyAddon]";
    }
}
```

### Registering the Interceptor
Register your interceptor once during your addon's `onEnable()`.

```java
NSRaiAPI.registerInterceptor(new MyChatMiddleware());
```

> [!IMPORTANT]
> All interceptors are subject to the server's `permissions.yml`. Admins can globally disable chat reading or editing for specific addons. Make sure your addon handles these cases gracefully!

## 4. Async Data Persistence
You can now save data to files without worrying about disk lag using the async `SaveMsg` system.

```java
NSRaiAPI.saveMsg(plugin, "Some log data", "logs", "ai_chat", SaveMsg.Format.TXT, false)
    .thenAccept(success -> {
        if (success) {
            System.out.println("File saved successfully in the background!");
        }
    });
```

## 5. Session & Memory Management
In v1.4, you have full control over the AI's conversation state.

### Summarization (Memory Compression)
```java
// Summarize and keep in context (AI remembers past)
NSRaiAPI.summarizeConversation(player);

// Summarize and clear active session
NSRaiAPI.summarizeAndClear(player);
```

### Session Control
```java
// Cache Refresh: Reload session from disk
NSRaiAPI.refreshConversation(player);

// Cache Clear: Start a brand new session (AI forgets everything)
NSRaiAPI.clearConversation(player);

// Get current token usage
int tokens = NSRaiAPI.getConversationTokens(player);
```

### Cache & Shared Memory
```java
// Read/Write long-term player summary (AI's memory of the player)
Optional<String> summary = NSRaiAPI.getPlayerSummary(player);
NSRaiAPI.updatePlayerSummary(player, "The player likes diamonds.");

// Global shared memory between addons
NSRaiAPI.updateSharedMemory("global_event", "active");
Optional<String> status = NSRaiAPI.getSharedMemory("global_event");
```

## 6. Flexible Component Reloading
You can reload specific parts of the plugin without a full restart.
```java
// Options: FULL, CONFIG, KNOWLEDGE, FEATURES
NSRaiAPI.reload(NSRaiAPI.ReloadType.KNOWLEDGE);
```

## 7. Dynamic Model & Provider Management
Addons can now dynamically query or override the AI model and provider.

### Global Control
```java
// Check what the server is currently using
String model = NSRaiAPI.getCurrentModel();
String provider = NSRaiAPI.getApiProvider();

// Change settings for the entire server
NSRaiAPI.setModel("gemini-2.5-pro");
NSRaiAPI.setApiProvider("GEMINI");
```

### Per-Request Overrides
You can use a specific model for a single request without changing global settings:
```java
NSRaiAPI.askAI("Write a complex plugin...")
    .setModel("gemini-2.5-pro")
    .setProvider("GEMINI")
    .execute();
```


## 7. V1 Legacy Support (Backward Compatibility)
NSR-AI 1.4 is designed to be fully backward compatible with older chat addons. If you have code from v1.2 or v1.3, it will still work using these legacy classes.

<details>
<summary><b>Click to expand V1 Legacy Methods</b></summary>

### Legacy Chat API
If your addon uses the old `AIMessage` and `AIResponse` classes, those operations are still supported and run asynchronously.

```java
import com.nsr.ai.api.NSRaiAPI;
import com.nsr.ai.api.AIMessage;
import com.nsr.ai.api.AIResponse;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MyAIInteraction {

    public void askAI(Player player, String question) {
        AIMessage userMessage = new AIMessage(question, player.getUniqueId());
        // Call the AI asynchronously and handle the response when it's ready.
        NSRaiAPI.getAIResponse(userMessage)
                .thenAccept(aiResponse -> {
                    // This block executes on the main thread after the AI responds.
                    if (aiResponse.isSuccess()) {
                        player.sendMessage("AI says: " + aiResponse.getResponse());
                    } else {
                        player.sendMessage("AI failed to respond: " 
                        + aiResponse.getResponse());
                    }
                })
                .exceptionally(ex -> {
                    // Handle any exceptions that occurred during the asynchronous operation.
                    player.sendMessage("An error occurred while getting AI response: " 
                        + ex.getMessage());
                    ex.printStackTrace(); // Log the full stack trace for debugging
                    return null; // Return null to complete the CompletableFuture exceptionally
                });
    }
}
```

### Legacy History
```java
List<AIMessage> history = NSRaiAPI.getConversationHistory(player);
```
</details>


## 5. Complete Addon Registration Guide

To successfully integrate your addon with NSR-AI, there are now two ways:
1. **The Modern Way (Programmatic):** Build a standard plugin and register via API.
2. **The Legacy Way (addon.yml):** Build a specific JAR and place it in the addons folder.

### Step 1: Programmatic Registration (Recommended)

If you are developing a standard Bukkit plugin, you can easily register an AI integration using the `SimpleAddon` builder and `NSRaiAPI.registerPlugin()` in your `onEnable()`.

```java
import com.nsr.ai.api.NSRaiAPI;
import com.nsr.ai.api.SimpleAddon;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Build your addon in just a few lines
        SimpleAddon myAddon = new SimpleAddon("MyPluginAddon", "1.0", "YourName", "Integration description")
            .addFeature("keyword", "Description of what this feature does")
            .addCommand("mycmd", "What this command does")
            .onCommandExecution((player, args) -> {
                player.sendMessage("AI Command executed successfully!");
                return "Handled successfully!";
            });

        // Register it natively via the API as a Plugin Integration!
        NSRaiAPI.registerPlugin(this, myAddon);
    }
}
```

This bypasses the need for an `addon.yml` entirely. Your users can install your jar directly in the `plugins/` folder!
If you are developing a standalone addon rather than a plugin integration, you can use `NSRaiAPI.registerAddon(myAddon)` instead.

### Step 2: Legacy Addon Registration (Implementing AIAddon & addon.yml)

If you prefer the old method, your addon's main class must implement the `com.nsr.ai.api.AIAddon` interface. This interface defines the contract for how your addon interacts with the NSR-AI core plugin. Because of Java 8 default methods, you only need to override what you actually use.

Here's a basic example of an `AIAddon` implementation:

```java
package com.example.myaddon; // Your addon's package

import com.nsr.ai.api.AIAddon;
import com.nsr.ai.api.NSRaiAPI; // Import the API for core functionalities
import org.bukkit.entity.Player;
import org.bukkit.ChatColor; // For sending colored messages
import org.bukkit.plugin.Plugin; // Use generic Plugin for compatibility

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class MySimpleAddon implements AIAddon {

    /**
     * Called when your addon is enabled by the NSR-AI core plugin.
     * Perform initialization logic here (e.g., registering event listeners).
     *
     * @param plugin The main instance of the Bukkit Plugin that loaded this addon.
     *               Use NSRaiAPI static methods to access core functionalities.
     */
    @Override
    public void onEnable(Plugin plugin) {
        NSRaiAPI.getLogger().info(getName() + " v" + getVersion() + " by " + getAuthor() + " enabled!");
        // Example: Register a Bukkit event listener
        // plugin.getServer().getPluginManager().registerEvents(new MyAddonListener(plugin), plugin);
    }

    /**
     * Called when your addon is disabled. Perform cleanup tasks here.
     */
    @Override
    public void onDisable() {
        NSRaiAPI.getLogger().info(getName() + " v" + getVersion() + " by " + getAuthor() + " disabled!");
    }

    /** Returns the official name of your addon. */
    @Override
    public String getName() {
        return "MySimpleAddon";
    }

    /** Returns the current version of your addon. */
    @Override
    public String getVersion() {
        return "1.0";
    }

    /** Returns the author(s) of your addon. */
    @Override
    public String getAuthor() {
        return "Gemini";
    }

    /** Returns a brief description of your addon. */
    @Override
    public String getDescription() {
        return "A simple addon demonstrating API usage.";
    }

    /**
     * Handles commands starting with `/ai` that are not handled by the core plugin.
     * Return a message to the player if handled, or `null` otherwise.
     */
    @Override
    public String onCommand(Player player, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("myaddon")) {
            if (args.length > 1 && args[1].equalsIgnoreCase("hello")) {
                player.sendMessage(ChatColor.GREEN + "Hello from MySimpleAddon!");
                return "Handled by MySimpleAddon";
            }
        }
        return null;
    }

    /** Returns a map of commands provided by your addon. */
    @Override
    public Map<String, String> getCommands() {
        return new HashMap<>();
    }

    /** Returns a map of features provided by your addon. */
    @Override
    public Map<String, String> getFeatures() {
        return new HashMap<>();
    }
}
```

### Step 3: Create `addon.yml` (For Legacy method only)

Every NSR-AI addon **must** include an `addon.yml` file in its `src/main/resources` directory. This file serves as the manifest for your addon, providing essential metadata that the NSR-AI core plugin uses to load, identify, and manage it.

Here's an example `addon.yml` and a detailed explanation of each field:

```yaml
# addon.yml
# This file must be located in your addon's src/main/resources directory.

# The official name of your addon. This should be unique and descriptive.
name: MySimpleAddon

# The current version of your addon. Follow semantic versioning (e.g., 1.0.0, 1.2-BETA).
version: 1.0

author: BlackForge

# A brief description of your addon's functionality.
# This is displayed in the /ai addon <name> command.
description: A simple addon demonstrating API usage.

# The fully qualified name of your addon's main class.
# This class must implement the com.nsr.ai.api.AIAddon interface.
# Example: com.yourcompany.youraddon.MainClass
main: com.example.myaddon.MySimpleAddon
```

**Explanation of `addon.yml` fields:**

*   **`name`**: (Required) A unique string identifying your addon. This name will be used in logs and in the `/ai addon list` command.
*   **`version`**: (Required) The current version of your addon. It's recommended to follow [Semantic Versioning](https://semver.org/).
*   **`author`**: (Required) The name(s) of the addon developer(s).
*   **`description`**: (Required) A brief, concise description of what your addon does. This is displayed when users request details about your addon.
*   **`main`**: (Required) The full path to your addon's main class, including its package. This class must implement the `com.nsr.ai.api.AIAddon` interface. The NSR-AI core plugin will instantiate this class when loading your addon.

### Step 4: Package and Install Your Addon

After implementing your `AIAddon` class and configuring `addon.yml` (if using legacy), you need to compile your addon into a JAR file and place it in the correct directory for NSR-AI to load it.

1.  **Build Your Addon**: Use your build tool (e.g., Maven, Gradle) to compile your project into a JAR file. Ensure that the `nsr-ai-api` dependency is set to `provided` in your build configuration, as the core plugin will provide it at runtime.
2.  **Install the JAR**: 
    - If you used **Programmatic Registration** (Step 1), place your JAR in the standard `/plugins/` folder.
    - If you used **Legacy Registration** (Step 2 & 3), your compiled addon JAR file **must** be placed in the `/plugins/NSR-AI/addons/` directory.

    Addons placed in the main `/plugins/` directory (without programmatic registration) will not be loaded as addons by the addon manager. This ensures a clean separation between standard plugins and NSR-AI addons.

    Standard Bukkit/Spigot plugins that do not interact with the NSR-AI API can be placed in the main `/plugins/` folder as usual.

## 6. Addon Command Guidelines

To prevent conflicts with the core plugin's commands and to ensure a consistent user experience, all addons must follow these command registration rules:

### Standard Addon Commands

All general addon commands must be prefixed with either `/aiaddon` or its shorter alias, `/aia`.

-   **Correct:** `/aiaddon myfeature`
-   **Correct:** `/aia stats`
-   **Incorrect:** `/myfeature`

### Advanced Commands (Conditional)

In specific cases, you may register a sub-command under the main `/ai` command (e.g., `/ai playerstats`). This is permitted **only if** your command logic meets the following criteria:

1.  **No Conflict:** It must not override or interfere with any existing or future core `/ai` sub-commands.
2.  **No Conversation Interference:** It must not disrupt a player's ongoing conversation with the AI. Your command must be distinct and not something a player would say in a normal chat. For example, an addon like `Advance-Player-Stats` could use `/ai stats` because it's a specific, non-conversational keyword.

Failure to follow these guidelines may result in your addon being blocked by the core plugin's security manager.

## 7. Further Assistance

For any further questions or issues, please refer to the main `README.md` or open an issue in this repository.

## 8. Addon Recommendations & Freedom of Development

We want to encourage you to build whatever you can imagine! The NSR-AI ecosystem thrives on developer creativity, and we want to see your unique ideas come to life.

*   **Feel Free to Build:** We encourage you to create any addon or plugin integration you want. Feel free to explore the API and push the boundaries of what AI can do in Minecraft.
*   **Advice vs. Requirements:** Most of the guidelines in this document are intended as **helpful advice** to ensure your addons are stable and compatible. We recommend following them for the best user experience, but they are not strict mandates.
*   **License Notice:** While most guidelines are advice, compliance with the **NSR-AI License (including the Commons Clause)** is a strict requirement for all distributed addons and integrations.
*   **Get Recommended:** If you want your addon or plugin to be officially recommended or featured by the NSR-AI team, simply **open a ticket on our official GitHub repository**. We will review your work for stability and quality; if it's good, we'll happily feature it to our users!

Happy coding, and we can't wait to see what you build!

