package dev.thewarrior.Events;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent.PlayerRequest;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.MultiCommands;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public class ItemDropProtectionSystem extends EntityEventSystem<EntityStore, PlayerRequest> {
    private final MultiCommands plugin;
    private final String bypassPermission = "multicommands.bypass.item_drop_protection";

    protected ItemDropProtectionSystem(MultiCommands plugin) {
        super(PlayerRequest.class);

        this.plugin = plugin;
    }

    @Override
    public void handle(
            int index,
            @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
            @NonNullDecl PlayerRequest event
    ) {
        if(event.isCancelled()) return;

        final Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        final Player player = store.getComponent(ref, Player.getComponentType());
        final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if(player == null || playerRef == null || !playerRef.isValid()) return;
        if(player.hasPermission(this.bypassPermission)) return;

        final TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());

        if(transform == null) return;

        int x = (int) Math.floor(transform.getPosition().getX());
        int y = (int) Math.floor(transform.getPosition().getY());
        int z = (int) Math.floor(transform.getPosition().getZ());
    }

    @Nullable
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }

    @NonNullDecl
    public Set<Dependency<EntityStore>> getDependencies() {
        return Collections.singleton(RootDependency.first());
    }
}
