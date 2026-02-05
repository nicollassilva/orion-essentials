package dev.thewarrior.SkyBlock.Commands.Island;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class IslandCreateCommand extends AbstractPlayerCommand {
    private final IslandsManager islandsManager;

    public IslandCreateCommand(final IslandsManager islandsManager) {
        super("iniciar", "Cria uma nova ilha no SkyBlock");

        this.islandsManager = islandsManager;

        this.addAliases("criar", "create", "start");

        this.requirePermission(PermissionUtil.getPermission("skyblock.create"));
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        this.islandsManager.createIslandForPlayerAsync(
                playerRef, store, ref,
                () -> commandContext.sendMessage(ColorUtil.colorize("&aIlha criada com sucesso! Teleportando você para sua nova ilha...")),
                () -> commandContext.sendMessage(ColorUtil.colorize("&cErro ao criar a ilha. Tente novamente mais tarde."))
        );
    }
}
