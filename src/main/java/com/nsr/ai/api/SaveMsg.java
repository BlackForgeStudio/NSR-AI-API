package com.nsr.ai.api;

import org.bukkit.plugin.Plugin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Inbuilt easy system to save AI responses and data to files.
 */
public class SaveMsg {

    public enum Format {
        TXT, JSON, YML, DAT
    }

    /**
     * Saves text content to a file easily.
     * @param plugin Your plugin instance.
     * @param content The text to save.
     * @param folderName The folder inside your plugin's data folder to save it in (e.g., "saved_chats").
     * @param fileName The name of the file (e.g., "player_data").
     * @param format The format of the file (TXT, JSON, YML, DAT).
     * @param binary Whether to save as raw binary bytes or readable text.
     * @return true if successful, false otherwise.
     */
    public static CompletableFuture<Boolean> save(Plugin plugin, String content, String folderName, String fileName, Format format, boolean binary) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                File folder = new File(plugin.getDataFolder(), folderName);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                File file = new File(folder, fileName + "." + format.name().toLowerCase());

                if (binary) {
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(content.getBytes(StandardCharsets.UTF_8));
                    }
                } else {
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(content);
                    }
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}
