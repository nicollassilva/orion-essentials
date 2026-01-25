package dev.thewarrior.Essentials.Managers;

import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.thewarrior.Essentials.Managers.Data.Permission.PermissionData;
import dev.thewarrior.Essentials.Managers.Data.Permission.PermissionGroupsData;
import dev.thewarrior.Essentials.Managers.Permission.PermissionBackupManager;
import dev.thewarrior.OrionEssentials;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.Essentials.Utils.Logger;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.Utils.ThrottledTask;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class PermissionManager {
    private static final String COLOR_PERMISSION = PermissionUtil.getPermission("chat.colors");
    private static final long CACHE_INVALIDATION_INTERVAL_MS = 30_000L; // 30 seconds
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("&[0-9a-fA-F]|&#[0-9a-fA-F]{6}");

    private final AtomicBoolean isLoaded = new AtomicBoolean(false);

    private PluginConfigManager pluginConfigManager;

    private final Path defaultConfigFile;

    private final Path pluginConfigFile;
    private JsonObject pluginRootObject;

    private PermissionGroupsData groupsData;

    private final ThrottledTask saveTask;
    private final Set<String> groupsToSave = ConcurrentHashMap.newKeySet();

    private final PermissionBackupManager backupManager;

    // Cache for player prefix/suffix (UUID -> [prefix, suffix])
    private final Map<UUID, String[]> playerFormatCache = new ConcurrentHashMap<>();

    public PermissionManager(final Path dataPath, PluginConfigManager pluginConfigManager) {
        this.defaultConfigFile = Paths.get("permissions.json");
        this.pluginConfigFile = dataPath.resolve("permissions.json");
        this.pluginConfigManager = pluginConfigManager;

        this.backupManager = new PermissionBackupManager(
                this.defaultConfigFile,
                this.pluginConfigFile,
                dataPath.resolve("PermissionBackups")
        );

        this.saveTask = new ThrottledTask(HytaleServer.SCHEDULED_EXECUTOR, this::saveAsync, 1000);

        // Schedule cache invalidation every 30 seconds
        HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(
                this::invalidateFormatCache,
                CACHE_INVALIDATION_INTERVAL_MS,
                CACHE_INVALIDATION_INTERVAL_MS,
                java.util.concurrent.TimeUnit.MILLISECONDS
        );

        this.syncLoad();
    }

    private void syncLoad() {
        if(this.isLoaded.getAndSet(true)) return;

        boolean hasError = false;

        try (final Reader reader = Files.newBufferedReader(this.defaultConfigFile)) {
            JsonObject defaultRootObject = OrionEssentials.gson.fromJson(reader, JsonObject.class);

            final PermissionGroupsData data = OrionEssentials.gson.fromJson(defaultRootObject, PermissionGroupsData.class);

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
            this.pluginRootObject = OrionEssentials.gson.fromJson(reader, JsonObject.class);
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

    /**
     * Formats a chat message for a player based on their permission groups.
     * The player's name is formatted with prefix/suffix from their highest priority group.
     *
     * @param playerRef The player reference
     * @param content   The message content
     * @return Formatted message with player name decorated with prefix/suffix
     */
    public Message formatChatMessage(PlayerRef playerRef, String content) {
        if (playerRef == null || !playerRef.isValid()) return Message.raw(content);

        UUID playerUuid = playerRef.getUuid();
        String playerName = playerRef.getUsername();

        // Get cached or compute the player's prefix and suffix
        String[] prefixSuffix = playerFormatCache.computeIfAbsent(playerUuid, this::computePrefixSuffix);

        // Strip color codes from message if player doesn't have permission
        String sanitizedContent = content;

        if (!PermissionsModule.get().hasPermission(playerUuid, COLOR_PERMISSION)) {
            sanitizedContent = stripColorCodes(content).trim();
        }

        String formatted = this.pluginConfigManager.getChatFormat()
                .replace("{prefix}", prefixSuffix[0])
                .replace("{player}", playerName)
                .replace("{suffix}", prefixSuffix[1])
                .replace("{message}", sanitizedContent);

        return ColorUtil.colorize(formatted);
    }

    /**
     * Computes the prefix and suffix for a player from their permission groups.
     * Uses the highest priority group that has a prefix or suffix defined.
     *
     * @param playerUuid The player's UUID
     * @return Array with [prefix, suffix]
     */
    private String[] computePrefixSuffix(UUID playerUuid) {
        Set<String> playerGroups = PermissionsModule.get().getGroupsForUser(playerUuid);

        if (playerGroups.isEmpty()) {
            return new String[]{"", ""};
        }

        // Find the highest priority group with prefix or suffix
        PermissionData highestPriorityGroup = null;
        int highestPriority = Integer.MIN_VALUE;

        for (String groupName : playerGroups) {
            final PermissionData data = this.groupsData.getGroupData(groupName);

            if (data == null) continue;
            if (!hasContent(data.getPrefix()) && !hasContent(data.getSuffix())) continue;

            int priority = data.getPriority();

            if (priority > highestPriority) {
                highestPriority = priority;
                highestPriorityGroup = data;
            }
        }

        if (highestPriorityGroup == null) {
            return new String[]{"", ""};
        }

        String prefix = highestPriorityGroup.getPrefix();
        String suffix = highestPriorityGroup.getSuffix();

        return new String[]{
                hasContent(prefix) ? prefix : "",
                hasContent(suffix) ? suffix : ""
        };
    }

    /**
     * Strips color codes (&0-&f and &#RRGGBB) from a string.
     *
     * @param text The input string
     * @return String without color codes
     */
    private String stripColorCodes(String text) {
        if (text == null || text.isEmpty()) return text;

        return COLOR_CODE_PATTERN.matcher(text).replaceAll("");
    }

    /**
     * Invalidates the player name format cache for a specific player.
     *
     * @param playerUuid The player's UUID
     */
    public void invalidatePlayerCache(UUID playerUuid) {
        this.playerFormatCache.remove(playerUuid);
    }

    /**
     * Clears the entire format cache. Called automatically every 30 seconds.
     */
    public void invalidateFormatCache() {
        Logger.info("Clearing player chat format cache.");

        this.playerFormatCache.clear();
    }

    private static boolean hasContent(String str) {
        return str != null && !str.isEmpty();
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

    public void deletePermission(final String groupName) {
        CompletableFuture.runAsync(() -> {
            final PermissionData data = this.getGroupData(groupName);

            if (data == null) return;

            PermissionsModule.get().removeGroupPermission(groupName, new ObjectArraySet<>(data.getPermissions()));

            this.groupsData.removeGroup(groupName);

            if (this.pluginRootObject != null) {
                final JsonObject groupsObject = this.pluginRootObject.getAsJsonObject("groups");

                if (groupsObject != null) {
                    groupsObject.remove(groupName);
                }
            }

            this.save(groupName);
        });
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

        this.invalidateFormatCache();

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
                    Files.writeString(this.pluginConfigFile, OrionEssentials.gson.toJson(this.pluginRootObject));
                }
            } catch (Exception e) {
                Logger.error("[CRITICAL] Failed to save permissions data: " + e.getMessage());
            } finally {
                groupsToProcess.clear();
            }
        });
    }
}
