package dev.thewarrior.Essentials.Events;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage.EntitySource;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage.Source;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Essentials.Managers.Data.Region.Flag.RegionFlag;
import dev.thewarrior.Essentials.Managers.RegionManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public class DamageProtectionSystem extends EntityEventSystem<EntityStore, Damage> {
    private final RegionManager regionManager;
    private final String fallAssetName = "Fall";

    public DamageProtectionSystem(RegionManager regionManager) {
        super(Damage.class);
        this.regionManager = regionManager;
    }

    @Override
    public void handle(
            int index,
            @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
            @NonNullDecl Damage event
    ) {
        if (event.isCancelled()) return;

        // === Obtém informações da vítima ===
        final Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(index);
        final Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
        final PlayerRef victimPlayerRef = store.getComponent(victimRef, PlayerRef.getComponentType());
        final boolean victimIsPlayer = victimPlayer != null && victimPlayerRef != null && victimPlayerRef.isValid();

        if (!victimIsPlayer) return;

        final RegionData victimRegion = getHighestPriorityRegion(victimPlayerRef, victimRef, store);

        if (victimRegion != null && isInvincible(victimRegion)) {
            this.cancelDamageWithKnockback(event, victimRef, store, commandBuffer);
            return;
        }

        final Source source = event.getSource();

        if (source instanceof EntitySource entitySource) {
            this.handleEntityDamage(event, entitySource, store, commandBuffer, victimRef, victimPlayerRef, victimRegion);
            return;
        }

        this.handleEnvironmentalDamage(event, victimRef, store, commandBuffer, victimRegion);
    }

    /**
     * Processa dano causado por entidades (PvP ou PvM).
     */
    private void handleEntityDamage(
            Damage event,
            EntitySource entitySource,
            Store<EntityStore> victimStore,
            CommandBuffer<EntityStore> commandBuffer,
            Ref<EntityStore> victimRef,
            PlayerRef victimPlayerRef,
            RegionData victimRegion
    ) {
        final Ref<EntityStore> attackerRef = entitySource.getRef();

        if (!attackerRef.isValid()) return;

        final Store<EntityStore> attackerStore = attackerRef.getStore();

        final Player attackerPlayer;
        final PlayerRef attackerPlayerRef;

        try {
            attackerPlayer = attackerStore.getComponent(attackerRef, Player.getComponentType());
            attackerPlayerRef = attackerStore.getComponent(attackerRef, PlayerRef.getComponentType());
        } catch (Exception e) {
            return;
        }

        final boolean attackerIsPlayer = attackerPlayer != null && attackerPlayerRef != null && attackerPlayerRef.isValid();

        if (attackerIsPlayer) {
            this.handlePvP(event, victimRef, victimStore, commandBuffer, attackerRef, attackerPlayerRef, attackerStore, victimRegion);
        } else {
            this.handlePvM(event, victimRef, victimStore, commandBuffer, victimRegion);
        }
    }

    private void handlePvP(
            Damage event,
            Ref<EntityStore> victimRef,
            Store<EntityStore> victimStore,
            CommandBuffer<EntityStore> commandBuffer,
            Ref<EntityStore> attackerRef,
            PlayerRef attackerPlayerRef,
            Store<EntityStore> attackerStore,
            RegionData victimRegion
    ) {
        final RegionData attackerRegion = this.getHighestPriorityRegion(attackerPlayerRef, attackerRef, attackerStore);

        if (attackerRegion != null && this.isPvPBlocked(attackerRegion)) {
            this.cancelDamageWithKnockback(event, victimRef, victimStore, commandBuffer);
            return;
        }

        if (victimRegion != null && this.isPvPBlocked(victimRegion)) {
            this.cancelDamageWithKnockback(event, victimRef, victimStore, commandBuffer);
        }
    }

    private void handlePvM(
            Damage event,
            Ref<EntityStore> victimRef,
            Store<EntityStore> victimStore,
            CommandBuffer<EntityStore> commandBuffer,
            RegionData victimRegion
    ) {
        if (victimRegion == null) return;

        if (isPvMBlocked(victimRegion)) {
            this.cancelDamageWithKnockback(event, victimRef, victimStore, commandBuffer);
        }
    }

    private void handleEnvironmentalDamage(
            Damage event,
            Ref<EntityStore> victimRef,
            Store<EntityStore> store,
            CommandBuffer<EntityStore> commandBuffer,
            RegionData victimRegion
    ) {
        if (victimRegion == null) return;

        final DamageCause cause = DamageCause.getAssetMap().getAsset(event.getDamageCauseIndex());

        if (cause != null && isFallDamage(cause)) {
            if (this.isFallDamageBlocked(victimRegion)) this.cancelDamageWithKnockback(event, victimRef, store, commandBuffer);

            return;
        }

        if (this.isPvEBlocked(victimRegion)) {
            this.cancelDamageWithKnockback(event, victimRef, store, commandBuffer);
        }
    }

    @Nullable
    private RegionData getHighestPriorityRegion(PlayerRef playerRef, Ref<EntityStore> ref, Store<EntityStore> store) {
        final TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());

        if (transform == null) return null;

        return this.regionManager.getHighestPriorityRegion(
                playerRef.getUuid(), transform, store.getExternalData().getWorld()
        );
    }

    private boolean isFallDamage(DamageCause cause) {
        return cause == DamageCause.getAssetMap().getAsset(this.fallAssetName);
    }

    private boolean isInvincible(RegionData region) {
        Boolean invincible = region.getFlags().getBoolean(RegionFlag.INVINCIBLE.getName());

        return invincible != null && invincible;
    }

    private boolean isPvPBlocked(RegionData region) {
        Boolean pvp = region.getFlags().getBoolean(RegionFlag.PVP.getName());

        return pvp != null && !pvp;
    }

    private boolean isPvMBlocked(RegionData region) {
        Boolean pvm = region.getFlags().getBoolean(RegionFlag.PVM.getName());

        return pvm != null && !pvm;
    }

    private boolean isPvEBlocked(RegionData region) {
        Boolean pve = region.getFlags().getBoolean(RegionFlag.PVE.getName());

        return pve != null && !pve;
    }

    private boolean isFallDamageBlocked(RegionData region) {
        Boolean fallDamage = region.getFlags().getBoolean(RegionFlag.FALL_DAMAGE.getName());
        return fallDamage != null && !fallDamage;
    }

    private void cancelDamageWithKnockback(
            Damage event, Ref<EntityStore> ref, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer
    ) {
        event.setCancelled(true);

        clearKnockback(event, ref, store, commandBuffer);
    }

    private void clearKnockback(
            Damage event, Ref<EntityStore> ref, Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer
    ) {
        event.removeMetaObject(Damage.KNOCKBACK_COMPONENT);

        KnockbackComponent knockback = store.getComponent(ref, KnockbackComponent.getComponentType());
        if (knockback != null) {
            knockback.setVelocity(Vector3d.ZERO);
            knockback.setDuration(0.0F);
        }

        commandBuffer.tryRemoveComponent(ref, KnockbackComponent.getComponentType());
    }

    @Nullable
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @NonNullDecl
    public Set<Dependency<EntityStore>> getDependencies() {
        return Collections.singleton(RootDependency.first());
    }
}
