package dev.thewarrior.Managers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.ShutdownReason;
import dev.thewarrior.Managers.Data.Permission.PermissionData;
import dev.thewarrior.Managers.Data.Permission.PermissionGroupsData;
import dev.thewarrior.MultiCommands;
import dev.thewarrior.Utils.Logger;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class PermissionManager {
    private final Path defaultConfigFile;
    private final Path pluginConfigFile;
    private PermissionGroupsData groupsData;
    private JsonObject rootObject;

    public PermissionManager(final Path dataPath) {
        this.defaultConfigFile = Paths.get("permissions.json");
        this.pluginConfigFile = dataPath.resolve("permissions.json");

        this.syncLoad();
    }

    private void syncLoad() {
        boolean hasError = false;

        try (final Reader reader = Files.newBufferedReader(this.defaultConfigFile)) {
            this.rootObject = MultiCommands.gson.fromJson(reader, JsonObject.class);

            final PermissionGroupsData data = MultiCommands.gson.fromJson(this.rootObject, PermissionGroupsData.class);

            if(data == null) {
                hasError = true;
                return;
            }

            this.groupsData = data;
        } catch (Exception e) {
            Logger.error("Failed to load permissions data: " + e.getMessage());
        }

        if(!Files.exists(this.pluginConfigFile)) {
            try {
                Files.createDirectories(this.pluginConfigFile.getParent());
                Files.writeString(this.pluginConfigFile, "{\n  \"groups\": {}\n}");
            } catch (Exception e) {
                Logger.error("Failed to create plugin-specific permissions file: " + e.getMessage());
                hasError = true;
            }
        }

        try (final Reader reader = Files.newBufferedReader(this.pluginConfigFile)) {
            final JsonObject pluginRoot = MultiCommands.gson.fromJson(reader, JsonObject.class);
            final JsonObject permissionsObject = pluginRoot.getAsJsonObject("groups");

            if (permissionsObject != null) {
                for (String groupName : permissionsObject.keySet()) {
                    this.groupsData.syncGroupData(groupName, permissionsObject.getAsJsonObject(groupName));
                }
            }
        } catch (Exception e) {
            Logger.error("Failed to load plugin-specific permissions data: " + e.getMessage());
            hasError = true;
        }

        if (hasError) {
            Logger.error("Critical error occurred while loading permissions data. Shutting down the server.");
            HytaleServer.get().shutdownServer(ShutdownReason.CRASH);
        }
    }

    public PermissionGroupsData getGroupsData() {
        return this.groupsData;
    }

    public PermissionData getGroupData(final String groupName) {
        return this.groupsData.getGroupData(groupName);
    }

    public CompletableFuture<Void> saveAsync(final String groupName) {
        return CompletableFuture.runAsync(() -> {
            try {
                final JsonElement groupJson = MultiCommands.gson.toJsonTree(this.groupsData.getGroupData(groupName));

                this.rootObject.add(groupName, groupJson);

                Files.writeString(this.defaultConfigFile, MultiCommands.gson.toJson(this.rootObject));
            } catch (Exception e) {
                Logger.error("[CRITICAL] Failed to save permissions data: " + e.getMessage());
            }
        });
    }
}
