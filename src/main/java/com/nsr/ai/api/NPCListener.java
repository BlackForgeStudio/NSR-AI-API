package com.nsr.ai.api;

import org.bukkit.entity.Player;

/**
 * Listener interface for NPC-related events.
 * Part of the experimental V1 system.
 */
public interface NPCListener {
    void onNPCInteract(Player player, String npcName);
}
