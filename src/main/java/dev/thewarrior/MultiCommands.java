package dev.thewarrior;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Commands.Tell.BaseTellCommand;
import dev.thewarrior.Data.PlayerCommandData;
import dev.thewarrior.Handlers.PlayerEventHandler;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MultiCommands extends JavaPlugin {
    public static ComponentType<EntityStore, PlayerCommandData> PlayerDataComponent;

    public MultiCommands(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        PlayerDataComponent = getEntityStoreRegistry().registerComponent(PlayerCommandData.class, "PlayerCommandData", PlayerCommandData.CODEC);

        this.registerTellCommands();
        this.registerFriendCommands();

        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerEventHandler::onPlayerReady);
    }

    private void registerTellCommands() {
        this.getCommandRegistry().registerCommand(new BaseTellCommand());
    }

    private void registerFriendCommands() {

    }
}
