package com.nsr.ai.api;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * A builder class to simplify addon creation.
 * This class implements AIAddon and handles all mappings automatically.
 */
public class SimpleAddon implements AIAddon {

    private final String name;
    private final String version;
    private final String author;
    private final String description;
    private String alias;
    
    private final Map<String, String> features = new HashMap<>();
    private final Map<String, String> commands = new HashMap<>();
    private BiFunction<Player, String[], String> commandHandler = (player, args) -> null;
    private BiFunction<Player, String[], java.util.List<String>> tabHandler = (player, args) -> java.util.Collections.emptyList();

    public SimpleAddon(String name, String version, String author, String description) {
        this.name = name;
        this.version = version;
        this.author = author;
        this.description = description;
        this.alias = name.toLowerCase().replace(" ", "-");
    }

    /**
     * Sets the command alias for this addon.
     */
    public SimpleAddon setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * Adds a feature keyword to this addon.
     */
    public SimpleAddon addFeature(String keyword, String info) {
        features.put(keyword, info);
        return this;
    }

    /**
     * Adds a command to this addon.
     */
    public SimpleAddon addCommand(String command, String description) {
        commands.put(command, description);
        return this;
    }

    /**
     * Sets the command execution handler.
     */
    public SimpleAddon onCommandExecution(BiFunction<Player, String[], String> handler) {
        this.commandHandler = handler;
        return this;
    }

    /**
     * Sets the tab completion handler.
     */
    public SimpleAddon onTabCompleteHandler(BiFunction<Player, String[], java.util.List<String>> handler) {
        this.tabHandler = handler;
        return this;
    }

    @Override
    public String onCommand(Player player, String[] args) {
        return commandHandler.apply(player, args);
    }

    @Override
    public java.util.List<String> onTabComplete(Player player, String[] args) {
        return tabHandler.apply(player, args);
    }

    @Override
    public Map<String, String> getCommands() {
        return commands;
    }

    @Override
    public Map<String, String> getFeatures() {
        return features;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getAlias() {
        return alias;
    }
}
