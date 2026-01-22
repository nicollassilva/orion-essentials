package dev.thewarrior.Commands.Region;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.Data.Region.Composition.RegionType;
import dev.thewarrior.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Managers.RegionManager;
import dev.thewarrior.Utils.ColorUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class RegionDetailCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> name;
    private final DefaultArg<Boolean> shouldShow;
    private final RegionManager regionManager;

    public RegionDetailCommand(RegionManager regionManager) {
        super("detail", "Mostra os detalhes de uma região específica.");

        this.regionManager = regionManager;
        this.name = this.withRequiredArg("name", "Nome da nova região", ArgTypes.STRING);
        this.shouldShow = this.withDefaultArg("show", "Se deve mostrar a seleção da região", ArgTypes.BOOLEAN, false, "Não mostra por padrão");

        requirePermission("multicommands.region.detail");
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

        if(player == null) return;

        final String regionName = this.name.get(commandContext);
        final RegionData regionData = this.regionManager.getRegionByName(regionName);
        final boolean shouldOpenSelection = this.shouldShow.get(commandContext);

        if(regionData == null) {
            commandContext.sendMessage(Message.raw("[ERRO] Nenhuma região com o nome '" + regionName + "' foi encontrada no servidor.").color(Color.RED));
            return;
        }

        RegionBaseCommand.sendRegionData(commandContext, regionData, "Região encontrada!");

        if(regionData.getType().equals(RegionType.GLOBAL) || !shouldOpenSelection) return;

        commandContext.sendMessage(ColorUtil.colorize("&3Para limpar a seleção, use &b/region deselect&3."));

        BuilderToolsPlugin.addToQueue(player, playerRef, (entityRef, buildState, componentAccessor) -> {
            if(buildState.getSelection() != null && buildState.getSelection().hasSelectionBounds()) {
                buildState.deselect(componentAccessor);
            }

            buildState.select(
                    regionData.getArea().getMin().toVector3i(),
                    regionData.getArea().getMax().toVector3i(),
                    regionData.getName(), componentAccessor
            );
        });
    }
}
