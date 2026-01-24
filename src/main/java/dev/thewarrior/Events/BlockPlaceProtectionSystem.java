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
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Managers.Data.Region.Flag.RegionFlag;
import dev.thewarrior.Managers.RegionManager;
import dev.thewarrior.Utils.PermissionUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BlockPlaceProtectionSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
    private final RegionManager regionManager;
    private static final String BYPASS_PERMISSION = PermissionUtil.getPermission("bypass.block_place");
    private final String regionFlag;

    public BlockPlaceProtectionSystem(RegionManager regionManager) {
        super(PlaceBlockEvent.class);

        this.regionManager = regionManager;

        this.regionFlag = RegionFlag.BUILD.getName();
    }

    @Override
    public void handle(
            int index,
            @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
            @NonNullDecl PlaceBlockEvent event
    ) {
        if (event.isCancelled()) return;

        final Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        final Player player = store.getComponent(ref, Player.getComponentType());
        final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (player == null || playerRef == null || !playerRef.isValid()) return;
        //if (player.hasPermission(BYPASS_PERMISSION)) return;

        final int blockX = event.getTargetBlock().getX();
        final int blockY = event.getTargetBlock().getY();
        final int blockZ = event.getTargetBlock().getZ();
        final World world = store.getExternalData().getWorld();
        final String itemId = event.getItemInHand() != null && event.getItemInHand().isValid()
                ? event.getItemInHand().getItemId()
                : "*";

        final List<RegionData> regions = this.regionManager.getApplicableRegions(world.getName(), blockX, blockY, blockZ);

        if (regions.isEmpty()) return;
        if (this.isPlaceAllowed(regions, itemId)) return;

        event.setCancelled(true);
    }

    private boolean isPlaceAllowed(List<RegionData> regions, String itemId) {
        for (RegionData region : regions) {
            if (!region.getFlags().hasFlag(this.regionFlag)) continue;

            return region.getFlags().checkMappedPermission(this.regionFlag, itemId);
        }

        return true;
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

