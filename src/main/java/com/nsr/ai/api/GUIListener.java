package com.nsr.ai.api;

import org.bukkit.entity.Player;

/**
 * Listener interface for GUI-related events.
 * Part of the experimental V1 system.
 */
public interface GUIListener {
    void onGUIEvent(Player player, String eventType);
}
