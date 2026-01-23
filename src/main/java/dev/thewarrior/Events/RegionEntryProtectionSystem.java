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

/**
 * Sistema de proteção de entrada em regiões.
 * Verifica se o jogador pode entrar em uma região e teleporta de volta se não puder.
 * Também exibe mensagens de greeting/farewell ao entrar/sair de regiões.
 */
public class RegionEntryProtectionSystem extends EntityTickingSystem<EntityStore> {
    private final RegionManager regionManager;

    // Cache de última posição válida (para teleporte de volta)
    private final Map<UUID, Vector3d> lastValidPosition = new Object2ObjectOpenHashMap<>();

    // Cache de última posição em blocos (para detectar movimento)
    private final Map<UUID, Long> lastBlockPosition = new Object2ObjectOpenHashMap<>();

    // Cache de regiões anteriores (para detectar entrada/saída)
    private final Map<UUID, List<RegionData>> previousRegions = new Object2ObjectOpenHashMap<>();

    // Cache de timestamp de mensagens (throttle para evitar spam)
    private final Object2LongOpenHashMap<UUID> lastMessageTime = new Object2LongOpenHashMap<>();

    // Constantes
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

        // Encode posição em long para comparação rápida (evita criar objetos)
        final long currentBlockPosEncoded = encodePosition(blockX, blockY, blockZ);
        final Long lastBlockPosEncoded = this.lastBlockPosition.get(playerId);

        // Se posição não mudou, não precisa processar
        if (lastBlockPosEncoded != null && currentBlockPosEncoded == lastBlockPosEncoded) return;

        // Inicializa posição válida se não existir (primeira vez que o jogador entra)
        if (!this.lastValidPosition.containsKey(playerId)) {
            this.lastValidPosition.put(playerId, currentPos);
            this.lastBlockPosition.put(playerId, currentBlockPosEncoded);
        }

        final World world = store.getExternalData().getWorld();
        final String worldName = world.getName();
        final List<RegionData> currentRegions = this.regionManager.getApplicableRegions(worldName, blockX, blockY, blockZ);

        // Se tem bypass, atualiza tudo e processa transições normalmente
//        if (player.hasPermission(BYPASS_PERMISSION)) {
//            this.lastBlockPosition.put(playerId, currentBlockPosEncoded);
//            this.lastValidPosition.put(playerId, currentPos);
//            processRegionTransitions(player, playerId, currentRegions);
//            return;
//        }

        // IMPORTANTE: Verifica se entrada é bloqueada ANTES de atualizar qualquer cache
        if (isEntryBlocked(currentRegions)) {
            // Teleporta de volta para a última posição válida
            // NÃO atualiza lastBlockPosition nem lastValidPosition - mantém a posição anterior
            teleportBack(buffer, chunk.getReferenceTo(index), playerId, player, world);
            return;
        }

        // Entrada permitida - agora podemos atualizar os caches
        this.lastBlockPosition.put(playerId, currentBlockPosEncoded);
        this.lastValidPosition.put(playerId, currentPos);

        // Processa entrada/saída de regiões (mensagens greeting/farewell)
        processRegionTransitions(player, playerId, currentRegions);
    }

    /**
     * Processa transições entre regiões (entrada/saída) para exibir mensagens.
     */
    private void processRegionTransitions(Player player, UUID playerId, List<RegionData> currentRegions) {
        final List<RegionData> previousRegionList = this.previousRegions.get(playerId);

        // Atualiza cache de regiões anteriores
        this.previousRegions.put(playerId, currentRegions);

        if (previousRegionList == null) return;

        // Detecta regiões que o jogador entrou
        for (RegionData region : currentRegions) {
            if (!previousRegionList.contains(region)) {
                onRegionEnter(player, playerId, region);
            }
        }

        // Detecta regiões que o jogador saiu
        for (RegionData region : previousRegionList) {
            if (!currentRegions.contains(region)) {
                onRegionExit(player, playerId, region);
            }
        }
    }

    /**
     * Chamado quando o jogador entra em uma região.
     */
    private void onRegionEnter(Player player, UUID playerId, RegionData region) {
        final String greeting = region.getFlags().getMessage(RegionFlag.GREETING.getName());

        if (greeting == null || greeting.isEmpty()) return;

        sendThrottledMessage(player, playerId, greeting, GREEDING_SECONDARY_TITLE);
    }

    /**
     * Chamado quando o jogador sai de uma região.
     */
    private void onRegionExit(Player player, UUID playerId, RegionData region) {
        final String farewell = region.getFlags().getMessage(RegionFlag.FAREWELL.getName());

        if (farewell == null || farewell.isEmpty()) return;

        sendThrottledMessage(player, playerId, farewell, FAREWELL_SECONDARY_TITLE);
    }

    /**
     * Envia mensagem com throttle para evitar spam.
     */
    private void sendThrottledMessage(Player player, UUID playerId, String title, String secondaryTitle) {
        final long now = System.currentTimeMillis();
        final long lastTime = this.lastMessageTime.getOrDefault(playerId, 0L);

        if (now - lastTime < MESSAGE_COOLDOWN_MS) return;

        Ref<EntityStore> ref = player.getReference();

        if(ref == null) return;

        Store<EntityStore> store = ref.getStore();
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

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

    /**
     * Verifica se a entrada em alguma região está bloqueada para o jogador.
     */
    private boolean isEntryBlocked(List<RegionData> regions) {
        for (RegionData region : regions) {
            if (!region.getFlags().hasFlag(RegionFlag.PERMISSIONS.getName())) continue;

            // Se a flag entry existe e está configurada como false, bloqueia
            final boolean canEntry = region.getFlags().checkMappedPermission(
                    RegionFlag.PERMISSIONS.getName(), "entry"
            );

            if (!canEntry) return true;
        }

        return false;
    }

    /**
     * Teleporta o jogador de volta para a última posição válida.
     */
    private void teleportBack(CommandBuffer<EntityStore> buffer, Ref<EntityStore> ref, UUID playerId, Player player, World world) {
        final Vector3d lastValid = this.lastValidPosition.get(playerId);

        if (lastValid == null) return;

        final PlayerRef playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());

        if (playerRef == null || !playerRef.isValid()) return;

        Vector3f orientation = playerRef.getTransform().getRotation();

        // Pequeno ajuste para evitar ficar preso em blocos
        double x = 0.5 - (Math.cos(Math.toRadians(orientation.getY())) * 0.25);
        double z = 0.5 - (Math.sin(Math.toRadians(orientation.getY())) * 0.25);

        final Teleport teleport = new Teleport(world, lastValid.add(x, 0, z), playerRef.getTransform().getRotation());

        buffer.addComponent(ref, Teleport.getComponentType(), teleport);

        // Envia mensagem de negação com throttle
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

    /**
     * Remove todos os dados do jogador do cache (chamar quando desconectar).
     */
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
