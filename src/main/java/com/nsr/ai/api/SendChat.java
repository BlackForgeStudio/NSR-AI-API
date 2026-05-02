package com.nsr.ai.api;

import org.bukkit.entity.Player;

/**
 * Handles sending simulated chat messages to the AI.
 */
public class SendChat {
    private final Player player;
    private final String prompt;
    private boolean usePlayerOnly = false;
    private boolean useGlobalOnly = false;
    private Float temperature = null;
    private Float topP = null;
    private Integer topK = null;
    private Integer maxTokens = null;
    private String modelOverride = null;
    private String providerOverride = null;
    private final java.util.Map<String, Object> customSettings = new java.util.HashMap<>();

    public SendChat(Player player, String prompt) {
        this.player = player;
        this.prompt = prompt;
    }

    public SendChat setCustomSetting(String key, Object value) {
        this.customSettings.put(key, value);
        return this;
    }

    public SendChat setTemperature(float temperature) {
        this.temperature = temperature;
        return this;
    }

    public SendChat setTopP(float topP) {
        this.topP = topP;
        return this;
    }

    public SendChat setTopK(int topK) {
        this.topK = topK;
        return this;
    }

    public SendChat setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public SendChat setModel(String model) {
        this.modelOverride = model;
        return this;
    }

    public SendChat setProvider(String provider) {
        this.providerOverride = provider;
        return this;
    }

    public SendChat usePlayerApiKey() {
        this.usePlayerOnly = true;
        this.useGlobalOnly = false;
        return this;
    }

    public SendChat useGlobalApiKey() {
        this.useGlobalOnly = true;
        this.usePlayerOnly = false;
        return this;
    }

    public java.util.concurrent.CompletableFuture<Void> execute() {
        return NSRaiAPI.callInternalMethod("sendMsg", 
            new Class<?>[]{Player.class, String.class, Boolean.class, Boolean.class, Float.class, Float.class, Integer.class, Integer.class, String.class, String.class, java.util.Map.class}, 
            player, prompt, usePlayerOnly, useGlobalOnly, temperature, topP, topK, maxTokens, modelOverride, providerOverride, customSettings);
    }
}
