package dev.thewarrior.Essentials.Commands.Discord;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Managers.PluginConfigManager;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class SetDiscordCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> link;

    private final PluginConfigManager pluginConfigManager;

    public SetDiscordCommand(PluginConfigManager pluginConfigManager) {
        super("setdiscord", "Define o link do Discord do servidor");

        this.pluginConfigManager = pluginConfigManager;
        this.link = this.withRequiredArg("link", "Link para o Discord", ArgTypes.STRING);

        requirePermission(PermissionUtil.getPermission("discord.setlink"));
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        String discordLink = this.link.get(commandContext);

        if(!discordLink.isEmpty() && !discordLink.startsWith("https://")) {
            Message invalidLinkMessage = Messages.COMMAND_DISCORD_INVALID_LINK
                    .italic(true)
                    .color(Color.RED);

            playerRef.sendMessage(invalidLinkMessage);
            return;
        }

        if(!discordLink.equals(this.pluginConfigManager.getDiscordLink())) {
            this.pluginConfigManager.setDiscordLink(discordLink).thenRun(() ->
                playerRef.sendMessage(Messages.COMMAND_DISCORD_LINK_UPDATED.color(Color.GREEN))
            );
        } else {
            playerRef.sendMessage(Messages.COMMAND_DISCORD_LINK_UPDATED.color(Color.GREEN));
        }
    }
}