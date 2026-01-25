package dev.thewarrior.Essentials.Managers.Composition;

import dev.thewarrior.OrionBootstrap;
import dev.thewarrior.Essentials.Utils.Logger;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public abstract class StorableManager<T> {
    protected final Path configFile;
    protected T data;
    protected Class<T> dataClass;

    public StorableManager(@Nonnull Path dataFolder, String fileName, Class<T> dataClass) {
        this.configFile = dataFolder.resolve(fileName);
        this.data = this.createDefaultData();
        this.dataClass = dataClass;

        this.loadConfig(dataClass);
    }

    /**
     * Creates a new instance of the data object with default values.
     * Subclasses must implement this to provide their specific data type.
     * @return A new instance of T with default values
     */
    protected abstract T createDefaultData();

    /**
     * Loads the configuration from the file, or creates a default one if it doesn't exist.
     * @param dataClass The class type of the data to deserialize
     */
    private void loadConfig(Class<T> dataClass) {
        if(!Files.exists(this.configFile)) {
            this.saveConfigSync();
            this.onDataLoaded();
            return;
        }

        try {
            String json = Files.readString(this.configFile);
            T loadedData = OrionBootstrap.gson.fromJson(json, dataClass);

            if (loadedData != null) {
                this.data = loadedData;
            }
            this.onDataLoaded();
        } catch (Exception e) {
            Logger.error("Falha ao carregar o arquivo de configuração: " + this.configFile);
        }
    }

    /**
     * Called after data is loaded from file.
     * Subclasses can override this to perform post-load operations.
     */
    protected void onDataLoaded() {
        // Default implementation does nothing
    }

    /**
     * Saves the current data to the file asynchronously.
     * This method does not block the calling thread - the save operation runs in the background.
     * Can be called by subclasses when they need to persist changes.
     * @return CompletableFuture that completes when the save is done
     */
    protected CompletableFuture<Void> saveConfig() {
        return CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(configFile.getParent());
                Files.writeString(this.configFile, OrionBootstrap.gson.toJson(this.data));
            } catch (Exception e) {
                Logger.error("[ASYNC] Falha ao salvar o arquivo de configuração: " + this.configFile, e);
            }
        });
    }

    /**
     * Saves the current data to the file synchronously.
     * This method blocks until the save operation completes.
     * Use this only when you need to ensure the data is written before continuing.
     */
    protected void saveConfigSync() {
        try {
            Files.createDirectories(configFile.getParent());
            Files.writeString(this.configFile, OrionBootstrap.gson.toJson(this.data));
        } catch (Exception e) {
            Logger.error("[SYNC] Falha ao salvar o arquivo de configuração: " + this.configFile, e);
        }
    }

    /**
     * Gets the current data object.
     * @return The current data
     */
    public T getData() {
        return this.data;
    }

    /**
     * Reloads the configuration from the file.
     */
    public void reload() {
        this.loadConfig(this.dataClass);
    }
}
