package dev.thewarrior.SkyBlock.Commands.Island;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.SkyBlock.Managers.IslandLevelManager;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Pages.IslandMenuPage;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class IslandMenuCommand extends AbstractPlayerCommand {
    private final IslandsManager islandsManager;
    private final IslandLevelManager islandLevelManager;

    public IslandMenuCommand(final IslandsManager islandsManager, final IslandLevelManager islandLevelManager) {
        super("menu", "Abre o menu principal da ilha");

        this.islandsManager = islandsManager;
        this.islandLevelManager = islandLevelManager;

        this.addAliases("m", "painel");

        this.requirePermission(PermissionUtil.getPermission("skyblock.menu"));
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

        if (player == null || player.wasRemoved()) {
            commandContext.sendMessage(ColorUtil.colorize("&cErro ao processar o comando. Jogador inválido."));
            return;
        }

        final IslandMenuPage menuPage = new IslandMenuPage(playerRef, this.islandsManager, this.islandLevelManager);

        player.getPageManager().openCustomPage(ref, store, menuPage);
    }
}

