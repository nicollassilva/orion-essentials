package dev.thewarrior.Managers.Permission;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.thewarrior.Utils.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

public class PermissionBackupManager {
    private static final int MAX_BACKUPS = 50;
    private static final DateTimeFormatter BACKUP_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");

    private final Path defaultConfigFile;
    private final Path pluginConfigFile;
    private final Path backupDirectory;

    public PermissionBackupManager(final Path defaultConfigFile, final Path pluginConfigFile, final Path backupDirectory) {
        this.defaultConfigFile = defaultConfigFile;
        this.pluginConfigFile = pluginConfigFile;
        this.backupDirectory = backupDirectory;

        this.initializeBackupDirectory();
    }

    private void initializeBackupDirectory() {
        try {
            if (!Files.exists(this.backupDirectory)) {
                Files.createDirectories(this.backupDirectory);
            }
        } catch (IOException e) {
            Logger.error("Failed to create backup directory: " + e.getMessage());
        }
    }

    public void createBackup() {
        try {
            final String timestamp = LocalDateTime.now().format(BACKUP_DATE_FORMAT);
            final Path backupFolder = this.backupDirectory.resolve(timestamp);

            Files.createDirectories(backupFolder);

            // Backup only "groups" from default permissions file
            if (Files.exists(this.defaultConfigFile)) {
                try (Reader reader = Files.newBufferedReader(this.defaultConfigFile)) {
                    final JsonObject rootObject = JsonParser.parseReader(reader).getAsJsonObject();
                    final JsonObject groupsObject = rootObject.getAsJsonObject("groups");

                    if (groupsObject != null) {
                        final Path defaultBackup = backupFolder.resolve("permissions.json");
                        Files.writeString(defaultBackup, groupsObject.toString());
                    }
                }
            }

            // Backup plugin permissions file
            if (Files.exists(this.pluginConfigFile)) {
                final Path pluginBackup = backupFolder.resolve("plugin_permissions.json");
                Files.copy(this.pluginConfigFile, pluginBackup);
            }

            this.cleanupOldBackups();
        } catch (IOException e) {
            Logger.error("Failed to create permissions backup: " + e.getMessage());
        }
    }

    private void cleanupOldBackups() {
        try (Stream<Path> folders = Files.list(this.backupDirectory)) {
            final var backupFolders = folders
                    .filter(Files::isDirectory)
                    .sorted(Comparator.comparing(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis();
                        } catch (IOException e) {
                            return 0L;
                        }
                    }))
                    .toList();

            if (backupFolders.size() > MAX_BACKUPS) {
                final int toRemove = backupFolders.size() - MAX_BACKUPS;

                for (int i = 0; i < toRemove; i++) {
                    final Path oldBackupFolder = backupFolders.get(i);
                    this.deleteDirectory(oldBackupFolder);
                }
            }
        } catch (IOException e) {
            Logger.error("Failed to cleanup old backups: " + e.getMessage());
        }
    }

    private void deleteDirectory(final Path directory) throws IOException {
        try (Stream<Path> walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            Logger.error("Failed to delete: " + path + " - " + e.getMessage());
                        }
                    });
        }
    }
}

