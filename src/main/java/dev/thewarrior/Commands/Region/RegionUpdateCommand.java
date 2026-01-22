package dev.thewarrior.Commands.Region;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.Data.Region.Composition.RegionType;
import dev.thewarrior.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Managers.RegionManager;
import dev.thewarrior.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class RegionUpdateCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> name;
    private final OptionalArg<String> newName;
    private final OptionalArg<Integer> priority;
    private final OptionalArg<Boolean> updateArea;

    private final RegionManager regionManager;

    public RegionUpdateCommand(RegionManager regionManager) {
        super("update", "Atualiza uma região existente no servidor.");

        this.regionManager = regionManager;

        this.name = this.withRequiredArg("name", "Nome da região a ser atualizada", ArgTypes.STRING);
        this.newName = this.withOptionalArg("newName", "Novo nome da região", ArgTypes.STRING);
        this.priority = this.withOptionalArg("priority", "Nova prioridade da região", ArgTypes.INTEGER);
        this.updateArea = this.withOptionalArg("updateArea", "Atualizar a área da região com a seleção atual", ArgTypes.BOOLEAN);

        requirePermission("multicommands.region.update");
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

        final String regionName = this.name.get(commandContext);
        final RegionData regionData = this.regionManager.getRegionByName(regionName);

        if (regionData == null) {
            commandContext.sendMessage(Message.raw("[ERRO] A região '" + regionName + "' não foi encontrada.").color(Color.RED));
            return;
        }

        final String newNameValue = this.newName.get(commandContext);
        final Integer priorityValue = this.priority.get(commandContext);
        final Boolean updateAreaValue = this.updateArea.get(commandContext);

        if (newNameValue == null && priorityValue == null && (updateAreaValue == null || !updateAreaValue)) {
            commandContext.sendMessage(Message.raw("[AVISO] Nenhuma alteração foi especificada. Use os parâmetros newName, priority ou updateArea.").color(Color.YELLOW));
            return;
        }

        // Verificar se o novo nome já existe
        if (newNameValue != null && !newNameValue.equalsIgnoreCase(regionName) && this.regionManager.hasRegionWithName(newNameValue)) {
            commandContext.sendMessage(Messages.ERROR_CREATING_REGION_NAME_ALREADY_EXISTS.color(Color.RED));
            return;
        }

        // Verificar prioridade válida
        if (priorityValue != null && priorityValue < 0 && !regionData.getType().equals(RegionType.GLOBAL)) {
            commandContext.sendMessage(Messages.ERROR_CREATING_REGION_INVALID_PRIORITY.color(Color.RED));
            return;
        }

        // Se precisa atualizar a área
        if (updateAreaValue != null && updateAreaValue && !regionData.getType().equals(RegionType.GLOBAL)) {
            if (!PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, player, store)) {
                commandContext.sendMessage(Messages.ERROR_CREATING_REGION_NO_SELECTION.color(Color.RED));
                return;
            }

            BuilderToolsPlugin.addToQueue(player, playerRef, (entityRef, builderState, componentAccessor) -> {
                final BlockSelection selection = builderState.getSelection();

                if (selection == null || !selection.hasSelectionBounds()) {
                    commandContext.sendMessage(Messages.ERROR_CREATING_REGION_NO_SELECTION.color(Color.RED));
                    return;
                }

                final Vector3i min = selection.getSelectionMin();
                final Vector3i max = selection.getSelectionMax();

                this.regionManager.updateRegion(regionData, newNameValue, priorityValue, min, max);

                RegionBaseCommand.sendRegionData(commandContext, regionData, "Região atualizada com sucesso!");
            });
        } else {
            // Atualização sem mudança de área
            this.regionManager.updateRegion(regionData, newNameValue, priorityValue, null, null);

            RegionBaseCommand.sendRegionData(commandContext, regionData, "Região atualizada com sucesso!");
        }
    }
}

