package dev.thewarrior.Essentials.Commands.Home;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Components.PlayerCommandComponent;
import dev.thewarrior.Essentials.Managers.TeleportManager;
import dev.thewarrior.OrionBootstrap;
import dev.thewarrior.Essentials.Utils.NamedLocation;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import dev.thewarrior.Essentials.Utils.StringUtils;
import dev.thewarrior.Essentials.i18n.Messages;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;

public class HomeCommand extends AbstractPlayerCommand {
    private final TeleportManager teleportManager;

    public HomeCommand(final TeleportManager teleportManager) {
        super("home", "Ir para uma home previamente definida");

        this.teleportManager = teleportManager;

        requirePermission(PermissionUtil.getPermission("homes.use"));
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
        final PlayerCommandComponent component = store.getComponent(ref, OrionBootstrap.PlayerDataComponent);

        if(component == null) {
            playerRef.sendMessage(Messages.CANNOT_GET_OWN_PLAYER_DATA.color(Color.RED));
            return;
        }

        if(component.getHomesCount() < 1) {
            sendHomeCommandList(playerRef);
            return;
        }

        final String rawInput = commandContext.getInputString();
        final String[] parts = rawInput.split("\\s+", 2); // [command, name]
        final String name = parts.length > 1 ? parts[1].trim() : "home";

        if(!component.hasHome(name)) {
            playerRef.sendMessage(Message.raw(String.format(Messages.COMMAND_HOME_NOT_EXISTS, name)).color(Color.YELLOW));
            return;
        }

        final NamedLocation home = component.getHome(name);

        Vector3d startPosition = playerRef.getTransform().getPosition();

        this.teleportManager.queueTeleport(
                playerRef, ref, store, startPosition,
                home.getWorld(), home.getX(), home.getY(), home.getZ(), home.getYaw(), home.getPitch(),
                Messages.COMMAND_GENERIC_TELEPORT_SUCCESS.color(Color.GREEN)
        );
    }

    public static void sendHomeCommandList(PlayerRef playerRef) {
        playerRef.sendMessage(Message.join(
                Message.raw("\n- Como usar o comando ").color(Color.GREEN), Message.raw("/home").color(Color.WHITE).bold(true), Message.raw(":\n").color(Color.GREEN),
                Message.raw("Permite salvar localizações e voltar à elas posteriormente.\n\n").color(Color.LIGHT_GRAY).italic(true),
                Message.raw("/home ").color(Color.WHITE).bold(true), Message.raw(" <nome?>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Teleporta para a home\n", 20, " ")),
                Message.raw("/sethome").color(Color.WHITE).bold(true), Message.raw(" <nome?>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Salva a localização atual\n", 15, " ")),
                Message.raw("/delhome").color(Color.WHITE).bold(true), Message.raw(" <nome?>").color(Color.YELLOW).bold(true), Message.raw(StringUtils.padLeft("Remove a localização indicada\n", 15, " ")),
                Message.raw("/homes").color(Color.WHITE).bold(true), Message.raw(StringUtils.padLeft("Lista todas as suas localizações salvas\n", 36, " "))
        ));
    }
}
