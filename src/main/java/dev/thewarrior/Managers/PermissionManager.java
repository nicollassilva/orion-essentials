package dev.thewarrior.Managers;

import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import dev.thewarrior.Managers.Data.Permission.PermissionData;
import dev.thewarrior.Managers.Data.Permission.PermissionGroupsData;
import dev.thewarrior.Managers.Permission.PermissionBackupManager;
import dev.thewarrior.MultiCommands;
import dev.thewarrior.Utils.Logger;
import dev.thewarrior.Utils.ThrottledTask;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PermissionManager {
    private final AtomicBoolean isLoaded = new AtomicBoolean(false);

    private final Path defaultConfigFile;

    private final Path pluginConfigFile;
    private JsonObject pluginRootObject;

    private PermissionGroupsData groupsData;

    private final ThrottledTask saveTask;
    private final Set<String> groupsToSave = ConcurrentHashMap.newKeySet();

    private final PermissionBackupManager backupManager;

    public PermissionManager(final Path dataPath) {
        this.defaultConfigFile = Paths.get("permissions.json");
        this.pluginConfigFile = dataPath.resolve("permissions.json");

        this.backupManager = new PermissionBackupManager(
                this.defaultConfigFile,
                this.pluginConfigFile,
                dataPath.resolve("PermissionBackups")
        );

        this.saveTask = new ThrottledTask(HytaleServer.SCHEDULED_EXECUTOR, this::saveAsync, 1000);

        this.syncLoad();
    }

    private void syncLoad() {
        if(this.isLoaded.getAndSet(true)) return;

        boolean hasError = false;

        try (final Reader reader = Files.newBufferedReader(this.defaultConfigFile)) {
            JsonObject defaultRootObject = MultiCommands.gson.fromJson(reader, JsonObject.class);

            final PermissionGroupsData data = MultiCommands.gson.fromJson(defaultRootObject, PermissionGroupsData.class);

            if(data == null) {
                hasError = true;
            }

            this.groupsData = data;
        } catch (Exception e) {
            Logger.error("Failed to load permissions data: " + e.getMessage());
            HytaleServer.get().shutdownServer();
            return;
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
            this.pluginRootObject = MultiCommands.gson.fromJson(reader, JsonObject.class);
            final JsonObject permissionsObject = this.pluginRootObject.getAsJsonObject("groups");

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

    public boolean isMandatoryPermission(String groupName) {
        final String lower = groupName.toLowerCase();

        return lower.equals("default") || lower.equals("op") || lower.equals("adventure") || lower.equals("creative");
    }

    public boolean hasGroupData(final String groupName) {
        return this.groupsData.hasGroup(groupName);
    }

    public void swapPermission(final String currentPermissionName) {
        final PermissionData currentData = this.getGroupData(currentPermissionName);

        if(currentData == null) {
            throw new NoSuchElementException();
        }

        if(!currentData.needsNameUpdate()) return;

        final Set<String> permissions = new ObjectArraySet<>(currentData.getPermissions());

        this.groupsData.removeGroup(currentPermissionName);

        PermissionsModule.get().removeGroupPermission(currentPermissionName, permissions);

        permissions.removeAll(currentData.getPermissionsToDelete());
        permissions.addAll(currentData.getPermissionsToAdd());

        this.groupsData.addGroup(currentData.getUpdatedName(), currentData.getPermissions());

        PermissionsModule.get().addGroupPermission(currentData.getUpdatedName(), permissions);
    }

    public void save(final String groupName) {
        final PermissionData data = this.getGroupData(groupName);

        if (data == null || !data.needsUpdate()) return;

        this.groupsToSave.add(groupName);

        this.saveTask.execute();
    }

    private void saveAsync() {
        if (this.groupsToSave.isEmpty()) return;

        final Set<String> groupsToProcess = new ObjectArraySet<>();

        this.groupsToSave.removeIf(group -> {
            groupsToProcess.add(group);

            return true;
        });

        CompletableFuture.runAsync(() -> {
            try {
                boolean needsFileUpdate = false;
                boolean needsBackup = false;

                for (final String groupName : groupsToProcess) {
                    final PermissionData data = this.getGroupData(groupName);

                    if (data == null) {
                        Logger.error("[CRITICAL] No permission data found for group: " + groupName);
                        continue;
                    }

                    final boolean hasNameUpdate = data.needsNameUpdate();
                    final String finalGroupName = hasNameUpdate ? data.getUpdatedName() : groupName;

                    if(hasNameUpdate) {
                        this.swapPermission(groupName);
                    } else if (data.needsPermissionsUpdate()) {
                        needsBackup |= !data.getPermissionsToDelete().isEmpty() || !data.getPermissionsToAdd().isEmpty();

                        PermissionsModule.get().removeGroupPermission(finalGroupName, data.getPermissionsToDelete());
                        PermissionsModule.get().addGroupPermission(finalGroupName, data.getPermissionsToAdd());

                        data.clearPendingPermissionsChanges();
                    }

                    if (data.needsDataUpdate()) {
                        final JsonObject updatedGroupData = data.getUpdatedCustomData();
                        JsonObject groupsObject = this.pluginRootObject.getAsJsonObject("groups");

                        if (groupsObject == null) {
                            groupsObject = new JsonObject();
                            this.pluginRootObject.add("groups", groupsObject);
                        }

                        if(hasNameUpdate) {
                            groupsObject.remove(groupName);
                        }

                        groupsObject.add(finalGroupName, updatedGroupData);
                        needsFileUpdate = true;
                    }

                    if(hasNameUpdate) {
                        data.setUpdatedName("");
                    }
                }

                if(needsBackup) {
                    this.backupManager.createBackup();
                }

                if (needsFileUpdate) {
                    Files.writeString(this.pluginConfigFile, MultiCommands.gson.toJson(this.pluginRootObject));
                }
            } catch (Exception e) {
                Logger.error("[CRITICAL] Failed to save permissions data: " + e.getMessage());
            } finally {
                groupsToProcess.clear();
            }
        });
    }
}
