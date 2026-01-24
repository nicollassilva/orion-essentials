package dev.thewarrior.Commands.Region;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Utils.PermissionUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class RegionDeselectCommand extends AbstractPlayerCommand {
    public RegionDeselectCommand() {
        super("deselect", "Remove a seleção atual do jogador.");

        requirePermission(PermissionUtil.getPermission("regions.deselect"));
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final Player player = store.getComponent(ref, Player.getComponentType());

        if (player == null) return;

        BuilderToolsPlugin.addToQueue(player, playerRef, (entityRef, buildState, componentAccessor) -> {
            if (buildState.getSelection() == null || !buildState.getSelection().hasSelectionBounds()) return;

            buildState.deselect(componentAccessor);

            commandContext.sendMessage(Message.raw("A região selecionada foi escondida com sucesso!").color(Color.GREEN));
        });
    }
}

