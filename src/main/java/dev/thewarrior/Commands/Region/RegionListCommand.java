package dev.thewarrior.Commands.Region;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Managers.RegionManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RegionListCommand extends AbstractPlayerCommand {
    private final RegionManager regionManager;

    public RegionListCommand(RegionManager regionManager) {
        super("list", "Lista todas as regiões existentes no servidor.");

        this.regionManager = regionManager;

        requirePermission("multicommands.region.list");
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        final List<RegionData> regions = this.regionManager.getRegions();

        if (regions.isEmpty()) {
            commandContext.sendMessage(Message.join(
                    Message.raw("Nenhuma região foi encontrada no servidor.\n").color(Color.YELLOW),
                    Message.raw("Use ").color(Color.LIGHT_GRAY),
                    Message.raw("/region create").color(Color.GREEN).bold(true),
                    Message.raw(" para criar uma nova região.\n").color(Color.LIGHT_GRAY)
            ));
            return;
        }

        commandContext.sendMessage(Message.raw("\n=== Regiões do Servidor ===\n").color(Color.CYAN).bold(true));

        int count = 1;
        List<Message> messages = new ArrayList<>();

        for (final RegionData region : regions) {
            messages.add(Message.raw("#" + count + " - ").color(Color.GREEN).bold(true));
            messages.add(Message.raw("Nome: ").color(Color.WHITE).bold(true));
            messages.add(Message.raw(region.getName()).color(Color.PINK).bold(true));
            messages.add(Message.raw(" no Mundo: ").color(Color.WHITE).bold(true));
            messages.add(Message.raw(region.getWorldName() + " \n").color(Color.PINK).bold(true));
            count++;
        }

        commandContext.sendMessage(Message.join(messages.toArray(new Message[0])));
    }
}
