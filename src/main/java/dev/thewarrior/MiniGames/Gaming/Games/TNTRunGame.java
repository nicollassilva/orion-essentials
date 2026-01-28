package dev.thewarrior.MiniGames.Gaming.Games;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.MiniGames.Gaming.Container.GameArena;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Enums.GameWinnerCondition;
import dev.thewarrior.MiniGames.Gaming.GameManager;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
import dev.thewarrior.MiniGames.Gaming.Player.GamePlayer;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;

public final class TNTRunGame extends Game {
    private static final double THRESHOLD = 0.3;

    private int bestX, bestY, bestZ;
    private double bestDist;

    public TNTRunGame(GameType type, GameArena arena, GameSettings settings) {
        super(type, arena, settings);

        this.winnerCondition = GameWinnerCondition.LAST_PLAYER_STANDING;
    }

    @Override
    public void onGameTick() {
        super.onGameTick();

        // mantém seu tick-rate atual
        if (this.gameTick.get() % ((1_000 / GameManager.TICK_RATE_MS) - 17) != 0) {
            return;
        }

        this.world.execute(() -> {
            for (final GamePlayer gamePlayer : this.players.values()) {

                final PlayerRef playerRef = gamePlayer.getPlayer();
                if (playerRef == null || !playerRef.isValid()) continue;

                final Ref<EntityStore> ref = playerRef.getReference();
                if (ref == null || !ref.isValid()) continue;

                final Store<EntityStore> store = ref.getStore();

                final TransformComponent transform =
                        store.getComponent(ref, TransformComponent.getComponentType());
                if (transform == null) continue;

                final MovementStatesComponent movement =
                        store.getComponent(ref, MovementStatesComponent.getComponentType());
                if (movement == null) continue;

                // Hypixel não quebra bloco enquanto o player está caindo
                if (movement.getMovementStates().falling) continue;

                handlePlayer(transform.getPosition());
            }
        });
    }

    /**
     * Decide exatamente 1 bloco para quebrar (ou nenhum).
     * Nunca quebra mais de 1 por tick.
     */
    private void handlePlayer(Vector3d pos) {

        final double px = pos.getX();
        final double pz = pos.getZ();

        final int baseX = (int) Math.floor(px);
        final int baseZ = (int) Math.floor(pz);

        final int footY  = (int) Math.floor(pos.getY()) - 1;
        final int belowY = footY - 1;

        final double fracX = px - baseX;
        final double fracZ = pz - baseZ;

        final boolean right = fracX > 1.0 - THRESHOLD;
        final boolean left  = fracX < THRESHOLD;
        final boolean front = fracZ > 1.0 - THRESHOLD;
        final boolean back  = fracZ < THRESHOLD;

        boolean found = false;
        bestDist = Double.MAX_VALUE;

        // ===== candidatos no nível do pé =====
        found |= tryCandidate(px, pz, baseX, footY, baseZ);

        if (right) found |= tryCandidate(px, pz, baseX + 1, footY, baseZ);
        if (left)  found |= tryCandidate(px, pz, baseX - 1, footY, baseZ);
        if (front) found |= tryCandidate(px, pz, baseX, footY, baseZ + 1);
        if (back)  found |= tryCandidate(px, pz, baseX, footY, baseZ - 1);

        if (right && front) found |= tryCandidate(px, pz, baseX + 1, footY, baseZ + 1);
        if (right && back)  found |= tryCandidate(px, pz, baseX + 1, footY, baseZ - 1);
        if (left && front)  found |= tryCandidate(px, pz, baseX - 1, footY, baseZ + 1);
        if (left && back)   found |= tryCandidate(px, pz, baseX - 1, footY, baseZ - 1);

        // achou → quebra APENAS UM bloco
        if (found) {
            this.world.setBlock(bestX, bestY, bestZ, BlockType.EMPTY.getId());
            return;
        }

        // ===== fallback abaixo (raro, mas necessário) =====
        if (this.world.getBlock(baseX, belowY, baseZ) != BlockType.EMPTY_ID) {
            this.world.setBlock(baseX, belowY, baseZ, BlockType.EMPTY.getId());
        }
    }

    /**
     * Testa um bloco candidato e decide se ele é melhor.
     * Regra de desempate determinística:
     *  - menor distância ao centro do player
     *  - se empatar, prioriza o bloco central (baseX/baseZ)
     */
    private boolean tryCandidate(double px, double pz, int x, int y, int z) {

        if (this.world.getBlock(x, y, z) == BlockType.EMPTY_ID) {
            return false;
        }

        final double dx = px - (x + 0.5);
        final double dz = pz - (z + 0.5);
        final double dist = dx * dx + dz * dz;

        if (dist < bestDist
                || (dist == bestDist && x == (int) Math.floor(px) && z == (int) Math.floor(pz))) {

            bestDist = dist;
            bestX = x;
            bestY = y;
            bestZ = z;
            return true;
        }

        return false;
    }
}
