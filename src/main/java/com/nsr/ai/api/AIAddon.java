package com.nsr.ai.api;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.Collections;
import java.util.Map;

/**
 * This interface must be implemented by all NSR-AI addons.
 * Standard Bukkit plugins can also implement this interface to act as an addon and plugin simultaneously.
 */
public interface AIAddon {

    default void onEnable(Plugin plugin) {}

    default void onDisable() {}

    default String onCommand(Player player, String[] args) {
        return null;
    }

    /**
     * Called when a player requests tab completion for an addon command.
     * @param player The player.
     * @param args The current arguments.
     * @return A list of suggestions.
     */
    default java.util.List<String> onTabComplete(Player player, String[] args) {
        return java.util.Collections.emptyList();
    }

    default Map<String, String> getCommands() {
        return Collections.emptyMap();
    }

    default Map<String, String> getFeatures() {
        return Collections.emptyMap();
    }

    String getName();

    String getVersion();

    String getAuthor();

    String getDescription();

    /**
     * The command alias for this addon (e.g., "aps" for Advanced Player Stats).
     * Used as: /aia <alias> <command> or /ai <alias> <command>
     * @return The alias string.
     */
    default String getAlias() {
        return getName().toLowerCase().replace(" ", "-");
    }
}
