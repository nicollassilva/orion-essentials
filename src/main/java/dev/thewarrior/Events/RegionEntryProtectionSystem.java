package dev.thewarrior.Events;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import dev.thewarrior.Managers.Data.Region.Data.RegionData;
import dev.thewarrior.Managers.Data.Region.Flag.RegionFlag;
import dev.thewarrior.Managers.RegionManager;
import dev.thewarrior.Utils.ColorUtil;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RegionEntryProtectionSystem extends EntityTickingSystem<EntityStore> {
    private final RegionManager regionManager;

    private final Map<UUID, Vector3d> lastValidPosition = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, Long> lastBlockPosition = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, List<RegionData>> previousRegions = new Object2ObjectOpenHashMap<>();
    private final Object2LongOpenHashMap<UUID> lastMessageTime = new Object2LongOpenHashMap<>();

    private static final long MESSAGE_COOLDOWN_MS = 1750L;
    private static final String BYPASS_PERMISSION = "multicommands.bypass.entry";
    private static final Message ENTRY_DENIED_MESSAGE = ColorUtil.colorize("&cVocê não tem permissão para entrar nessa área.");

    private static final String GREEDING_SECONDARY_TITLE = "Entrou em uma área protegida";
    private static final String FAREWELL_SECONDARY_TITLE = "Saiu de uma área protegida";

    public RegionEntryProtectionSystem(final RegionManager regionManager) {
        this.regionManager = regionManager;
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

        final Player player = chunk.getComponent(index, Player.getComponentType());

        if (player == null) return;

        final UUID playerId = playerRef.getUuid();
        final Vector3d currentPos = playerRef.getTransform().getPosition();
        final int blockX = (int) Math.floor(currentPos.getX());
        final int blockY = (int) Math.floor(currentPos.getY());
        final int blockZ = (int) Math.floor(currentPos.getZ());

        final long currentBlockPosEncoded = encodePosition(blockX, blockY, blockZ);
        final Long lastBlockPosEncoded = this.lastBlockPosition.get(playerId);

        if (lastBlockPosEncoded != null && currentBlockPosEncoded == lastBlockPosEncoded) return;

        if (!this.lastValidPosition.containsKey(playerId)) {
            this.lastValidPosition.put(playerId, currentPos);
            this.lastBlockPosition.put(playerId, currentBlockPosEncoded);
        }

        final World world = store.getExternalData().getWorld();
        final String worldName = world.getName();
        final List<RegionData> currentRegions = this.regionManager.getApplicableRegions(worldName, blockX, blockY, blockZ);

        if (player.hasPermission(BYPASS_PERMISSION)) {
            this.lastBlockPosition.put(playerId, currentBlockPosEncoded);
            this.lastValidPosition.put(playerId, currentPos);
            processRegionTransitions(player, playerId, currentRegions);
            return;
        }

        if (this.isEntryBlocked(player, currentRegions)) {
            this.teleportBack(buffer, chunk.getReferenceTo(index), playerId, player, world);
            return;
        }

        this.lastBlockPosition.put(playerId, currentBlockPosEncoded);
        this.lastValidPosition.put(playerId, currentPos);

        this.processRegionTransitions(player, playerId, currentRegions);
    }

    private void processRegionTransitions(Player player, UUID playerId, List<RegionData> currentRegions) {
        final List<RegionData> previousRegionList = this.previousRegions.get(playerId);

        this.previousRegions.put(playerId, currentRegions);

        if (previousRegionList == null) return;

        for (RegionData region : currentRegions) {
            if (!previousRegionList.contains(region)) {
                this.onRegionEnter(player, playerId, region);
            }
        }

        for (RegionData region : previousRegionList) {
            if (!currentRegions.contains(region)) {
                this.onRegionExit(player, playerId, region);
            }
        }
    }

    private void onRegionEnter(Player player, UUID playerId, RegionData region) {
        final String greeting = region.getFlags().getMessage(RegionFlag.GREETING.getName());

        if (greeting == null || greeting.isEmpty()) return;

        sendThrottledMessage(player, playerId, greeting, GREEDING_SECONDARY_TITLE);
    }

    private void onRegionExit(Player player, UUID playerId, RegionData region) {
        final String farewell = region.getFlags().getMessage(RegionFlag.FAREWELL.getName());

        if (farewell == null || farewell.isEmpty()) return;

        sendThrottledMessage(player, playerId, farewell, FAREWELL_SECONDARY_TITLE);
    }

    private void sendThrottledMessage(Player player, UUID playerId, String title, String secondaryTitle) {
        final long now = System.currentTimeMillis();
        final long lastTime = this.lastMessageTime.getOrDefault(playerId, 0L);

        if (now - lastTime < MESSAGE_COOLDOWN_MS) return;

        final Ref<EntityStore> ref = player.getReference();

        if(ref == null) return;

        final Store<EntityStore> store = ref.getStore();
        final PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if(playerRef == null || !playerRef.isValid()) return;

        this.lastMessageTime.put(playerId, now);

        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw(title),
                Message.raw(secondaryTitle),
                true,
                null,
                2,
                0.5F,
                0.5F
        );
    }

    private boolean isEntryBlocked(Player player, List<RegionData> regions) {
        for (RegionData region : regions) {
            if (!region.getFlags().hasFlag(RegionFlag.PERMISSIONS.getName())) continue;

            if (!region.getFlags().checkPlayerPermissions(RegionFlag.PERMISSIONS.getName(), player::hasPermission)) {
                return true;
            }
        }

        return false;
    }

    private void teleportBack(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref, UUID playerId, Player player, World world) {
        final Vector3d lastValid = this.lastValidPosition.get(playerId);

        if (lastValid == null) return;

        final PlayerRef playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());

        if (playerRef == null || !playerRef.isValid()) return;

        Vector3f orientation = playerRef.getTransform().getRotation();

        double x = 0.5 - (Math.cos(Math.toRadians(orientation.getY())) * 0.25);
        double z = 0.5 - (Math.sin(Math.toRadians(orientation.getY())) * 0.25);

        final Teleport teleport = new Teleport(world, lastValid.add(x, 0, z), playerRef.getTransform().getRotation());

        buffer.addComponent(ref, Teleport.getComponentType(), teleport);

        final long now = System.currentTimeMillis();
        final long lastTime = this.lastMessageTime.getOrDefault(playerId, 0L);

        if (now - lastTime >= MESSAGE_COOLDOWN_MS) {
            this.lastMessageTime.put(playerId, now);
            player.sendMessage(ENTRY_DENIED_MESSAGE);
        }
    }

    /**
     * Codifica posição XYZ em um único long para comparação rápida.
     * Formato: X (21 bits) | Y (21 bits) | Z (21 bits) = 63 bits
     */
    private static long encodePosition(int x, int y, int z) {
        return ((long) (x & 0x1FFFFF) << 42) | ((long) (y & 0x1FFFFF) << 21) | (z & 0x1FFFFF);
    }

    public void clearPlayer(UUID playerId) {
        this.lastValidPosition.remove(playerId);
        this.lastBlockPosition.remove(playerId);
        this.previousRegions.remove(playerId);
        this.lastMessageTime.removeLong(playerId);
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
