package com.nsr.ai.api;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;

/**
 * Handles background AI questioning.
 */
public class AskAI {
    private final String prompt;
    private Player playerContext = null;
    private boolean usePlayerOnly = false;
    private boolean useGlobalOnly = false;
    private Float temperature = null;
    private Float topP = null;
    private Integer topK = null;
    private Integer maxTokens = null;
    private String modelOverride = null;
    private String providerOverride = null;
    private final java.util.Map<String, Object> customSettings = new java.util.HashMap<>();

    public AskAI(String prompt) {
        this.prompt = prompt;
    }

    public AskAI setCustomSetting(String key, Object value) {
        this.customSettings.put(key, value);
        return this;
    }

    public AskAI setTemperature(float temperature) {
        this.temperature = temperature;
        return this;
    }

    public AskAI setTopP(float topP) {
        this.topP = topP;
        return this;
    }

    public AskAI setTopK(int topK) {
        this.topK = topK;
        return this;
    }

    public AskAI setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public AskAI setModel(String model) {
        this.modelOverride = model;
        return this;
    }

    public AskAI setProvider(String provider) {
        this.providerOverride = provider;
        return this;
    }

    public AskAI setPlayerContext(Player player) {
        this.playerContext = player;
        return this;
    }

    public AskAI usePlayerApiKey(Player player) {
        this.playerContext = player;
        this.usePlayerOnly = true;
        this.useGlobalOnly = false;
        return this;
    }

    public AskAI useGlobalApiKey() {
        this.useGlobalOnly = true;
        this.usePlayerOnly = false;
        return this;
    }

    public CompletableFuture<String> execute() {
        return NSRaiAPI.callInternalMethod("askAI", 
            new Class<?>[]{String.class, Player.class, Boolean.class, Boolean.class, Float.class, Float.class, Integer.class, Integer.class, String.class, String.class, java.util.Map.class}, 
            prompt, playerContext, usePlayerOnly, useGlobalOnly, temperature, topP, topK, maxTokens, modelOverride, providerOverride, customSettings);
    }
}
