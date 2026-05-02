package com.nsr.ai.api;

import org.bukkit.entity.Player;

/**
 * Interface for intercepting and modifying AI interactions.
 */
public interface AIInterceptor {
    
    /**
     * Called when a player sends a message to the AI.
     * @param player The player who sent the message.
     * @param input The raw input text.
     * @return The modified input, or null to cancel the request.
     */
    default String onPlayerInput(Player player, String input) {
        return input;
    }

    /**
     * Called when the AI generates a response, before it is sent to the player.
     * @param player The player receiving the response.
     * @param response The raw AI response.
     * @return The modified response, or null to cancel the response.
     */
    default String onAIResponse(Player player, String response) {
        return response;
    }

    /**
     * Called when the system prompt is being prepared for a core NSR-AI chat (/ai).
     * Addons can use this to inject extra instructions or context.
     * @param player The player in the conversation.
     * @param systemPrompt The current system prompt.
     * @return The modified system prompt.
     */
    default String onSystemPrompt(Player player, String systemPrompt) {
        return systemPrompt;
    }
}
