package dev.thewarrior.Essentials.Events;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Managers.TeleportManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class TeleportMovementCheckerSystem extends EntityTickingSystem<EntityStore> {
    private final TeleportManager teleportManager;

    public TeleportMovementCheckerSystem(final TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
    }
    @Override
    public void tick(
            float deltaTime,
            int index,
            @NonNullDecl ArchetypeChunk<EntityStore> chunk,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> buffer
    ) {
        final PlayerRef playerRef = chunk.getComponent(index, PlayerRef.getComponentType());

        if (playerRef == null || !playerRef.isValid()) return;
        if (!this.teleportManager.hasPendingTeleport(playerRef.getUuid())) return;

        final Ref<EntityStore> currentRef = chunk.getReferenceTo(index);
        final Vector3d currentPosition = playerRef.getTransform().getPosition();

        this.teleportManager.tick(playerRef.getUuid(), currentRef, currentPosition, deltaTime, buffer);
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}
