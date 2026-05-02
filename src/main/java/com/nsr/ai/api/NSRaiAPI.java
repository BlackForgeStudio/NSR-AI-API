package com.nsr.ai.api;

import org.bukkit.entity.Player;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.nsr.ai.api.AIMessage;
import com.nsr.ai.api.AIResponse;
import com.nsr.ai.api.AddonInfo;
import com.nsr.ai.api.AIInterceptor;
import com.nsr.ai.api.AskAI;
import com.nsr.ai.api.SendChat;
import com.nsr.ai.api.SaveMsg;


/**
 * The main entry point for the NSR-AI Open-Source API.
 * This final class provides static methods for addons to interact with the NSR-AI core plugin.
 * It acts as a facade, forwarding calls to the internal plugin implementation via reflection.
 *
 * ⚠️ This API is read-only, cannot bypass NSR-AI security, cannot store or reveal API keys,
 * and cannot be used to create scripted/canned AI responses.
 */
public final class NSRaiAPI {

    /**
     * The current major version of the NSR-AI Open-Source API.
     * Addons should check this value for compatibility.
     */
    public static final int API_VERSION = 3;

    // Internal core plugin reference (set via reflection by the core plugin)
    private static Object internalApiInstance;

    public NSRaiAPI() {
        // Public constructor to allow instantiation (getApi() pattern)
    }

    /**
     * Internal method used by the NSR-AI core plugin to set the internal API instance.
     * Addon developers should NOT call this method.
     * @param instance The internal API instance.
     */
    public static void setInternalApiInstance(Object instance) {
        NSRaiAPI.internalApiInstance = instance;
    }

    /**
     * Calls an internal method of the NSR-AI core plugin via reflection.
     * This method handles the forwarding of public API calls to the actual internal implementation.
     * @param methodName The name of the internal method to call.
     * @param paramTypes An array of Class objects representing the parameter types of the method.
     * @param args The arguments to pass to the method.
     * @param <T> The return type of the method.
     * @return The result of the internal method call.
     * @throws IllegalStateException if the internal API is not initialized, the method is not found,
     *                                an access error occurs, or the internal method throws an exception.
     */
    public static <T> T callInternalMethod(String methodName, Class<?>[] paramTypes, Object... args) {
        if (internalApiInstance == null) {
            throw new IllegalStateException("NSR-AI core plugin not initialized or API not ready.");
        }
        try {
            java.lang.reflect.Method method = internalApiInstance.getClass().getMethod(methodName, paramTypes);
            return (T) method.invoke(internalApiInstance, args);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("NSR-AI core plugin does not support method: " + methodName + ". API mismatch?", e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            } else {
                throw new IllegalStateException("Error calling internal NSR-AI API method: " + methodName, e.getTargetException());
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access internal NSR-AI API method: " + methodName, e);
        }
    }

    // --- Simplified Chat API ---
    
    /**
     * Simply asks the AI a question in the background.
     * @param prompt The question or prompt for the AI.
     * @return The AskAI builder object.
     */
    public static AskAI askAI(String prompt) {
        return new AskAI(prompt);
    }
    
    /**
     * Simplifies sending a message to the AI on behalf of a player.
     * @param player The player to send the message for.
     * @param prompt The text of the message.
     * @return The SendChat builder object.
     */
    public static SendChat sendMsg(Player player, String prompt) {
        return new SendChat(player, prompt);
    }

    /**
     * Saves AI messages or text data to a file easily.
     */
    public static java.util.concurrent.CompletableFuture<Boolean> saveMsg(org.bukkit.plugin.Plugin plugin, String content, String folderName, String fileName, SaveMsg.Format format, boolean binary) {
        return SaveMsg.save(plugin, content, folderName, fileName, format, binary);
    }

    // --- Legacy V1 Compatibility API ---

    public static CompletableFuture<Void> sendMessageToAI(Player player, AIMessage message) {
        return callInternalMethod("sendMessageToAI", new Class<?>[]{Player.class, AIMessage.class}, player, message);
    }

    public static CompletableFuture<AIResponse> getAIResponse(AIMessage message) {
        return callInternalMethod("getAIResponse", new Class<?>[]{AIMessage.class}, message);
    }

    /**
     * Retrieves a player's conversation history as legacy message objects.
     */
    public static java.util.List<AIMessage> getConversationHistoryObjects(Player player) {
        return callInternalMethod("getConversationHistoryObjects", new Class<?>[]{Player.class}, player);
    }

    // --- Memory API ---
    public static Optional<String> getSharedMemory(String key) {
        return callInternalMethod("getSharedMemory", new Class<?>[]{String.class}, key);
    }

    public static void updateSharedMemory(String key, String value) {
        callInternalMethod("updateSharedMemory", new Class<?>[]{String.class, String.class}, key, value);
    }

    public static void clearSharedMemory() {
        callInternalMethod("clearSharedMemory", new Class<?>[]{});
    }

    // --- Cache (Summary) API ---

    public static Optional<String> getPlayerSummary(Player player) {
        return callInternalMethod("getPlayerSummary", new Class<?>[]{Player.class}, player);
    }

    public static void updatePlayerSummary(Player player, String summary) {
        callInternalMethod("updatePlayerSummary", new Class<?>[]{Player.class, String.class}, player, summary);
    }

    public static void clearPlayerSummary(Player player) {
        callInternalMethod("clearPlayerSummary", new Class<?>[]{Player.class}, player);
    }

    // --- Versioning API ---
    public static String getVersion() {
        return callInternalMethod("getVersion", new Class<?>[]{});
    }

    public static int getApiVersion() {
        return API_VERSION;
    }

    // --- Addon Management ---

    public static void registerPlugin(org.bukkit.plugin.Plugin plugin, AIAddon integration) {
        callInternalMethod("registerPlugin", new Class<?>[]{org.bukkit.plugin.Plugin.class, AIAddon.class}, plugin, integration);
    }

    public static void registerAddon(AIAddon addon) {
        callInternalMethod("registerAddon", new Class<?>[]{AIAddon.class}, addon);
    }

    public static java.util.List<AIAddon> getRegisteredAddons() {
        return callInternalMethod("getRegisteredAddons", new Class<?>[]{});
    }

    public static java.util.List<com.nsr.ai.api.AddonInfo> getLoadedAddons() {
        return callInternalMethod("getLoadedAddonInfo", new Class<?>[]{});
    }

    public static java.util.List<com.nsr.ai.api.AddonInfo> getFailedAddons() {
        return callInternalMethod("getFailedAddonInfo", new Class<?>[]{});
    }

    public static java.util.logging.Logger getLogger() {
        return callInternalMethod("getLogger", new Class<?>[]{});
    }

    public static org.bukkit.plugin.Plugin getPlugin() {
        return callInternalMethod("getPlugin", new Class<?>[]{});
    }

    public static NSRaiAPI getApi() {
        return new NSRaiAPI();
    }

    // --- Admin Mode Management ---
    public static boolean toggleAdminMode(Player player, String activationCode) {
        return callInternalMethod("toggleAdminMode", new Class<?>[]{Player.class, String.class}, player, activationCode);
    }

    public static boolean isAdminModeEnabled(Player player) {
        return callInternalMethod("isAdminModeEnabled", new Class<?>[]{Player.class}, player);
    }

    // --- Internal Player States ---
    public static boolean isPlayerOnCooldown(Player player) {
        return callInternalMethod("isPlayerOnCooldown", new Class<?>[]{Player.class}, player);
    }

    public static long getPlayerCooldownRemaining(Player player) {
        return callInternalMethod("getPlayerCooldownRemaining", new Class<?>[]{Player.class}, player);
    }

    public static boolean isAiEnabled(Player player) {
        return callInternalMethod("isAiEnabled", new Class<?>[]{Player.class}, player);
    }

    public static void setAiEnabled(Player player, boolean enabled) {
        callInternalMethod("setAiEnabled", new Class<?>[]{Player.class, boolean.class}, player, enabled);
    }

    // --- Knowledge Base ---
    public static void addKnowledgeEntry(String keyword, String heading, String content) {
        callInternalMethod("addKnowledgeEntry", new Class<?>[]{String.class, String.class, String.class}, keyword, heading, content);
    }

    public static String removeKnowledgeEntry(String keyword) {
        return callInternalMethod("removeKnowledgeEntry", new Class<?>[]{String.class}, keyword);
    }

    public static java.util.Map<String, String> getAllKnowledge() {
        return callInternalMethod("getAllKnowledge", new Class<?>[]{});
    }

    public static java.util.List<String> getKnowledgeKeywords() {
        return callInternalMethod("getKnowledgeKeywords", new Class<?>[]{});
    }

    public static void addKnowledge(String keyword, String content) {
        callInternalMethod("addKnowledge", new Class<?>[]{String.class, String.class}, keyword, content);
    }

    public static void removeKnowledge(String keyword) {
        callInternalMethod("removeKnowledge", new Class<?>[]{String.class}, keyword);
    }

    public enum ReloadType {
        FULL, CONFIG, KNOWLEDGE, FEATURES
    }

    public static void reload(ReloadType type) {
        callInternalMethod("reloadComponent", new Class<?>[]{String.class}, type.name());
    }

    // --- Model & Provider Management ---

    public static String getCurrentModel() {
        return callInternalMethod("getCurrentModel", new Class<?>[]{});
    }

    public static void setModel(String model) {
        callInternalMethod("setModel", new Class<?>[]{String.class}, model);
    }

    public static String getApiProvider() {
        return callInternalMethod("getApiProvider", new Class<?>[]{});
    }

    public static void setApiProvider(String provider) {
        callInternalMethod("setApiProvider", new Class<?>[]{String.class}, provider);
    }

    public static void setPlayerModel(Player player, String model) {
        callInternalMethod("setPlayerModel", new Class<?>[]{Player.class, String.class}, player, model);
    }

    public static void setPlayerApiProvider(Player player, String provider) {
        callInternalMethod("setPlayerApiProvider", new Class<?>[]{Player.class, String.class}, player, provider);
    }

    // --- Session & Memory Management ---

    /**
     * Retrieves a player's conversation history as strings.
     */
    public static java.util.List<String> getConversationHistory(Player player) {
        return callInternalMethod("getConversationHistory", new Class<?>[]{Player.class}, player);
    }

    /**
     * Clears the current conversation history immediately.
     */
    public static void clearConversation(Player player) {
        callInternalMethod("clearConversationHistory", new Class<?>[]{Player.class}, player);
    }

    /**
     * Reloads the conversation session from disk.
     */
    public static void refreshConversation(Player player) {
        callInternalMethod("refreshConversation", new Class<?>[]{Player.class}, player);
    }

    /**
     * Calculates the current token count of the player's conversation.
     */
    public static int getConversationTokens(Player player) {
        Integer result = callInternalMethod("getConversationTokens", new Class<?>[]{Player.class}, player);
        return result != null ? result : 0;
    }

    /**
     * Summarizes the current conversation and injects it into the session.
     */
    public static void summarizeConversation(Player player) {
        callInternalMethod("summarizeAndRefreshSession", new Class<?>[]{Player.class}, player);
    }

    /**
     * Summarizes the current conversation and clears it.
     */
    public static void summarizeAndClear(Player player) {
        callInternalMethod("summarizeAndClearSession", new Class<?>[]{Player.class}, player);
    }

    // --- Pet System Management ---

    public static AIPet getPet(Player player, String name) {
        return callInternalMethod("getPetForAPI", new Class<?>[]{Player.class, String.class}, player, name);
    }

    public static java.util.List<AIPet> getPlayerPets(Player player) {
        return callInternalMethod("getPlayerPetsForAPI", new Class<?>[]{Player.class}, player);
    }

    public static void setPetBond(Player player, String name, int bond) {
        callInternalMethod("setPetBondForAPI", new Class<?>[]{Player.class, String.class, int.class}, player, name, bond);
    }

    public static void setPetMood(Player player, String name, String mood) {
        callInternalMethod("setPetMoodForAPI", new Class<?>[]{Player.class, String.class, String.class}, player, name, mood);
    }

    public static void setPetHunger(Player player, String name, int hunger) {
        callInternalMethod("setPetHungerForAPI", new Class<?>[]{Player.class, String.class, int.class}, player, name, hunger);
    }

    public static void setPetLevel(Player player, String name, int level) {
        callInternalMethod("setPetLevelForAPI", new Class<?>[]{Player.class, String.class, int.class}, player, name, level);
    }

    public static void registerInterceptor(AIInterceptor interceptor) {
        callInternalMethod("registerInterceptor", new Class<?>[]{AIInterceptor.class}, interceptor);
    }
}
