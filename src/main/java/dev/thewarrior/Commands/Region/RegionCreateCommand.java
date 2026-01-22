package dev.thewarrior.Commands.Region;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import dev.thewarrior.Managers.Data.Region.Composition.RegionType;
import dev.thewarrior.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Managers.RegionManager;
import dev.thewarrior.Utils.Logger;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class RegionCreateCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> type;
    private final RequiredArg<String> name;
    private final DefaultArg<Integer> priority;

    private final RegionManager regionManager;

    public RegionCreateCommand(RegionManager regionManager) {
        super("create", "Cria uma região baseado na seleção.");

        this.regionManager = regionManager;

        this.type = this.withRequiredArg("type", "Tipo da região (cuboid, polygonal, etc).", ArgTypes.STRING);
        this.name = this.withRequiredArg("name", "Nome da nova região", ArgTypes.STRING);
        this.priority = this.withDefaultArg("priority", "Prioridade da região (0..~)", ArgTypes.INTEGER, 0, "Prioridade padrão");

        requirePermission("multicommands.region.create");
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

        if(!PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, player, store)) {
            commandContext.sendMessage(Messages.ERROR_CREATING_REGION_NO_SELECTION.color(Color.RED));
            return;
        }

        final RegionType regionType = RegionType.fromString(this.type.get(commandContext).toLowerCase());
        final String regionName = this.name.get(commandContext);
        final int regionPriority = this.priority.get(commandContext);

        if(regionType.equals(RegionType.NO_FOUND)) {
            commandContext.sendMessage(Messages.ERROR_CREATING_REGION_INVALID_TYPE.color(Color.RED));
            return;
        }

        if (regionPriority < 0) {
            commandContext.sendMessage(Messages.ERROR_CREATING_REGION_INVALID_PRIORITY.color(Color.RED));
        }

        if(this.regionManager.hasRegionWithName(regionName)) {
            commandContext.sendMessage(Messages.ERROR_CREATING_REGION_NAME_ALREADY_EXISTS.color(Color.RED));
            return;
        }

        if(regionType.equals(RegionType.GLOBAL) && this.regionManager.hasGlobalRegionOnWorld(world.getName())) {
            commandContext.sendMessage(Messages.ERROR_CREATING_REGION_GLOBAL_ALREADY_EXISTS.color(Color.RED));
            return;
        }

        BuilderToolsPlugin.addToQueue(player, playerRef, (entityRef, builderState, componentAccessor) -> {
            final BlockSelection selection = builderState.getSelection();

            if((selection == null || !selection.hasSelectionBounds()) && !regionType.equals(RegionType.GLOBAL)) {
                commandContext.sendMessage(Messages.ERROR_CREATING_REGION_NO_SELECTION.color(Color.RED));
                return;
            }

            final Vector3i min = selection == null ? Vector3i.ZERO : selection.getSelectionMin();
            final Vector3i max = selection == null ? Vector3i.ZERO : selection.getSelectionMax();

            RegionData regionData = null;

            try {
                regionData = this.regionManager.create(regionType, regionName, world.getName(), min, max, regionPriority);
            } catch (Exception e) {
                Logger.error("Error while creating region", e);
            }

            if(regionData == null || UUIDUtil.isEmptyOrNull(regionData.getId())) {
                commandContext.sendMessage(Messages.ERROR_CREATING_REGION_GENERIC.color(Color.RED));
                return;
            }

            RegionBaseCommand.sendRegionData(commandContext, regionData, "Região criada com sucesso!");
        });
    }
}
