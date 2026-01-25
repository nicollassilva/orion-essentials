package dev.thewarrior;

import dev.thewarrior.Essentials.Utils.Logger;

import java.nio.file.Path;

public class BasePluginModule {
    protected String moduleName;
    protected Path dataPath;

    protected OrionBootstrap plugin;

    public BasePluginModule(OrionBootstrap plugin, String moduleName) {
        this.plugin = plugin;

        this.moduleName = moduleName;
        this.dataPath = Path.of(moduleName);
    }

    public void setup() {
        Logger.info(moduleName + " - Configurações iniciais carregadas");
    }

    public void start() {
        Logger.info(moduleName + " - Iniciando sistemas");
    }

    protected Path getDataDirectory() {
        return this.plugin.getDataDirectory().resolve(this.dataPath);
    }
}
