package dev.thewarrior.Essentials.Commands.Home;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Components.PlayerCommandComponent;
import dev.thewarrior.Essentials.Managers.WarpManager;
import dev.thewarrior.OrionEssentials;
import dev.thewarrior.Essentials.Utils.Logger;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class SetHomeCommand extends AbstractPlayerCommand {
    public SetHomeCommand() {
        super("sethome", "Define uma home na sua localização atual");

        requirePermission(PermissionUtil.getPermission("homes.set"));
        setAllowsExtraArguments(true);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final PlayerCommandComponent component = store.getComponent(ref, OrionEssentials.PlayerDataComponent);

        if(component == null) {
            playerRef.sendMessage(Messages.CANNOT_GET_OWN_PLAYER_DATA.color(Color.RED));
            return;
        }

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());

        if (transform == null) {
            playerRef.sendMessage(Messages.CANNOT_GET_OWN_PLAYER_POSITION.color(Color.RED));
            return;
        }

        final String rawInput = commandContext.getInputString();
        final String[] parts = rawInput.split("\\s+", 2); // [command, name]
        final String name = parts.length > 1 ? parts[1].trim() : "home";

        if(name.isEmpty() || name.length() > 16 || !WarpManager.WARP_NAME_PATTERN.matcher(name).matches()) {
            playerRef.sendMessage(Messages.HOME_NAME_INVALID.color(Color.RED));
            return;
        }

        if(component.getHomesCount() >= 5) {
            playerRef.sendMessage(Messages.HOME_SET_LIMIT_REACHED.color(Color.RED));
            return;
        }

        Vector3d position = transform.getPosition();
        HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
        Vector3f rotation = (headRotation != null) ? headRotation.getRotation() : new Vector3f(0.0F, 0.0F, 0.0F);

        try {
            component.addHome(name, world, position.clone(), rotation.clone());
        } catch (Exception e) {
            playerRef.sendMessage(Messages.COMMAND_SET_HOME_FAILED.color(Color.RED));
            Logger.error("Erro ao criar uma nova home para o usuário " + playerRef.getUsername(), e);
            return;
        }

        playerRef.sendMessage(Message.raw(String.format(Messages.COMMAND_SET_HOME_SUCCESS, name)).color(Color.GREEN));
    }
}
