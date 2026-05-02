package com.nsr.ai.api;

/**
 * Metadata for a registered NSR-AI addon.
 * Part of the V1 Legacy system, maintained for backward compatibility.
 */
public class AddonInfo {
    private final String name;
    private final String version;
    private final String author;
    private final String status;
    private final String description;
    private final String error;

    public AddonInfo(String name, String version, String author, String status, String description, String error) {
        this.name = name;
        this.version = version;
        this.author = author;
        this.status = status;
        this.description = description;
        this.error = error;
    }

    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getAuthor() { return author; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
    public String getError() { return error; }
}
