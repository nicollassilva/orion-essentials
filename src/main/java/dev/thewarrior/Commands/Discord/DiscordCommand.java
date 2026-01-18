package dev.thewarrior.Commands.Discord;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.MaybeBool;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.PluginConfigManager;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class DiscordCommand extends AbstractPlayerCommand {
    private final PluginConfigManager pluginConfigManager;
    private final Color discordColor = new Color(94, 99, 247);

    public DiscordCommand(PluginConfigManager pluginConfigManager) {
        super("discord", "Mostra o link do Discord do servidor");

        this.pluginConfigManager = pluginConfigManager;
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        Message discordMessage = Messages.COMMAND_DISCORD_LINK
                .italic(true)
                .link(this.pluginConfigManager.getDiscordLink())
                .color(this.discordColor);

        discordMessage.getFormattedMessage().underlined = MaybeBool.True;

        playerRef.sendMessage(discordMessage);
    }
}