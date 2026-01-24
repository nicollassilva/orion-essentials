package dev.thewarrior.Commands.Region;

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
import dev.thewarrior.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Managers.RegionManager;
import dev.thewarrior.Utils.PermissionUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class RegionDeleteCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> name;
    private final RegionManager regionManager;

    public RegionDeleteCommand(RegionManager regionManager) {
        super("delete", "Remove uma região existente do servidor.", true);

        this.regionManager = regionManager;

        this.name = this.withRequiredArg("name", "Nome da região a ser removida", ArgTypes.STRING);

        requirePermission(PermissionUtil.getPermission("regions.delete"));
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final String regionName = this.name.get(commandContext);
        final RegionData regionData = this.regionManager.getRegionByName(regionName);

        if (regionData == null) {
            commandContext.sendMessage(Message.raw("[ERRO] A região '" + regionName + "' não foi encontrada.").color(Color.RED));
            return;
        }

        this.regionManager.deleteRegion(regionData);

        commandContext.sendMessage(Message.raw("A região '" + regionName + "' foi removida com sucesso!").color(Color.GREEN));
    }
}
