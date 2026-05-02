package com.nsr.ai.api;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * Interface for addons to provide custom GUI definitions.
 * Part of the experimental V1 system.
 */
public interface CustomGUIProvider {
    Inventory createInventory(Player player);
    void handleClick(InventoryClickEvent event);
    String getTitle();
    int getSize();
}
