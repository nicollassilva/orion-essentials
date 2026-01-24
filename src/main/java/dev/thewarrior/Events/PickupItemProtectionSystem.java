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
import com.hypixel.hytale.server.core.event.events.ecs.InteractivelyPickupItemEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
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

public class PickupItemProtectionSystem extends EntityEventSystem<EntityStore, InteractivelyPickupItemEvent> {
    private final RegionManager regionManager;
    private static final String BYPASS_PERMISSION = PermissionUtil.getPermission("bypass.pickup_item");
    private final String regionFlag;

    public PickupItemProtectionSystem(RegionManager regionManager) {
        super(InteractivelyPickupItemEvent.class);
        this.regionManager = regionManager;

        this.regionFlag = RegionFlag.PICKUP_ITEM.getName();
    }

    @Override
    public void handle(
            int index,
            @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
            @NonNullDecl InteractivelyPickupItemEvent event
    ) {
        if (event.isCancelled()) return;

        final Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        final Player player = store.getComponent(ref, Player.getComponentType());
        final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (player == null || playerRef == null || !playerRef.isValid()) return;
        //if (player.hasPermission(BYPASS_PERMISSION)) return;

        final TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

        if (transformComponent == null) return;

        final World world = store.getExternalData().getWorld();

        final List<RegionData> regions = this.regionManager.getApplicableRegions(transformComponent, world);

        if (regions.isEmpty()) return;
        if (this.isPickupItemAllowed(regions, event.getItemStack())) return;

        event.setCancelled(true);
        event.setItemStack(ItemStack.EMPTY);
    }

    private boolean isPickupItemAllowed(List<RegionData> regions, ItemStack itemStack) {
        final String blockId = itemStack.getItemId();

        for (RegionData region : regions) {
            if (!region.getFlags().hasFlag(this.regionFlag)) continue;

            return region.getFlags().checkMappedPermission(this.regionFlag, blockId.isEmpty() ? "*" : blockId);
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


