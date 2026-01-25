package dev.thewarrior.Essentials.Events;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Essentials.Managers.Data.Region.Flag.RegionFlag;
import dev.thewarrior.Essentials.Managers.RegionManager;
import dev.thewarrior.Essentials.Utils.PermissionUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BlockBreakProtectionSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    private final RegionManager regionManager;
    private static final String BYPASS_PERMISSION = PermissionUtil.getPermission("bypass.block_break");
    private final String regionFlag;

    public BlockBreakProtectionSystem(RegionManager regionManager) {
        super(BreakBlockEvent.class);
        this.regionManager = regionManager;

        this.regionFlag = RegionFlag.BREAK.getName();
    }

    @Override
    public void handle(
            int index,
            @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
            @NonNullDecl BreakBlockEvent event
    ) {
        if (event.isCancelled()) return;

        final Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        final Player player = store.getComponent(ref, Player.getComponentType());
        final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (player == null || playerRef == null || !playerRef.isValid()) return;
        if (player.hasPermission(BYPASS_PERMISSION)) return;

        final int blockX = event.getTargetBlock().getX();
        final int blockY = event.getTargetBlock().getY();
        final int blockZ = event.getTargetBlock().getZ();
        final World world = store.getExternalData().getWorld();
        final BlockType blockType = world.getBlockType(blockX, blockY, blockZ);

        final List<RegionData> regions = this.regionManager.getApplicableRegions(world.getName(), blockX, blockY, blockZ);

        if (regions.isEmpty()) return;
        if (this.isBreakAllowed(regions, blockType)) return;

        event.setCancelled(true);
    }

    private boolean isBreakAllowed(List<RegionData> regions, BlockType blockType) {
        for (RegionData region : regions) {
            if (!region.getFlags().hasFlag(this.regionFlag)) continue;

            return region.getFlags().checkMappedPermission(this.regionFlag, blockType.getId());
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


