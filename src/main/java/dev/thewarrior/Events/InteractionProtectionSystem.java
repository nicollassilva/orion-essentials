package dev.thewarrior.Events;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent.Pre;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Managers.Data.Region.Flag.RegionFlag;
import dev.thewarrior.Managers.RegionManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InteractionProtectionSystem extends EntityEventSystem<EntityStore, Pre> {
    private final RegionManager regionManager;
    private static final String BYPASS_PERMISSION = "multicommands.bypass.item_interact";
    private final String regionFlag;

    public InteractionProtectionSystem(RegionManager regionManager) {
        super(Pre.class);

        this.regionManager = regionManager;

        this.regionFlag = RegionFlag.INTERACT.getName();
    }

    @Override
    public void handle(
            int index,
            @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
            @NonNullDecl Pre event
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

        final List<RegionData> regions = this.regionManager.getApplicableRegions(world.getName(), blockX, blockY, blockZ);

        if (regions.isEmpty()) return;
        //if (this.isSeatingInteraction(event.getBlockType())) return;
        if (this.isInteractionAllowed(regions, event.getBlockType())) return;

        event.setCancelled(true);
    }

    private boolean isSeatingInteraction(BlockType blockType) {
        if(blockType == null) return false;

        final Map<InteractionType, String> interactions = blockType.getInteractions();

        if(interactions == null || interactions.isEmpty()) return false;

        final String useInteraction = interactions.get(InteractionType.Use);

        if(useInteraction != null && useInteraction.toLowerCase().contains("seat")) {
            return true;
        }

        final String secondaryUseInteraction = interactions.get(InteractionType.Secondary);

        return secondaryUseInteraction != null && secondaryUseInteraction.toLowerCase().contains("seat");
    }

    private boolean isInteractionAllowed(List<RegionData> regions, BlockType blockType) {
        for (final RegionData region : regions) {
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


