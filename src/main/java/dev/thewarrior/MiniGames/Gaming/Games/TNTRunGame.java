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
import dev.thewarrior.MiniGames.Gaming.Enums.GameState;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Enums.GameWinnerCondition;
import dev.thewarrior.MiniGames.Gaming.GameManager;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
import dev.thewarrior.MiniGames.Gaming.Player.GamePlayer;
import dev.thewarrior.MiniGames.Gaming.Player.GamePlayerStats;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;

public final class TNTRunGame extends Game {
    private static final double THRESHOLD = 0.3;

    public TNTRunGame(GameType type, GameArena arena, GameSettings settings) {
        super(type, arena, settings);

        this.winnerCondition = GameWinnerCondition.LAST_PLAYER_STANDING;
    }

    @Override
    public void onGameTick() {
        super.onGameTick();

        // mantém seu tick-rate atual
        if (this.state.get() != GameState.RUNNING || this.gameTick.get() % ((1_000 / GameManager.TICK_RATE_MS) - 18) != 0) return;

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

                handlePlayer(gamePlayer, transform.getPosition());
            }
        });
    }

    /**
     * Decide exatamente 1 bloco para quebrar (ou nenhum).
     * Nunca quebra mais de 1 por tick.
     */
    private void handlePlayer(GamePlayer gamePlayer, Vector3d pos) {

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
        boolean broke = false;

        final CandidateResult result = new CandidateResult(-1, -1, -1, Double.MAX_VALUE);

        found = tryCandidate(px, pz, baseX, footY, baseZ, result);

        if (right) found |= tryCandidate(px, pz, baseX + 1, footY, baseZ, result);
        if (left)  found |= tryCandidate(px, pz, baseX - 1, footY, baseZ, result);
        if (front) found |= tryCandidate(px, pz, baseX, footY, baseZ + 1, result);
        if (back)  found |= tryCandidate(px, pz, baseX, footY, baseZ - 1, result);

        if (right && front) found |= tryCandidate(px, pz, baseX + 1, footY, baseZ + 1, result);
        if (right && back)  found |= tryCandidate(px, pz, baseX + 1, footY, baseZ - 1, result);
        if (left && front)  found |= tryCandidate(px, pz, baseX - 1, footY, baseZ + 1, result);
        if (left && back)   found |= tryCandidate(px, pz, baseX - 1, footY, baseZ - 1, result);

        // achou → quebra APENAS UM bloco
        if (found) {
            this.world.setBlock(result.x, result.y, result.z, BlockType.EMPTY.getId());
            broke = true;
        } else if (this.world.getBlock(baseX, belowY, baseZ) != BlockType.EMPTY_ID) {
            this.world.setBlock(baseX, belowY, baseZ, BlockType.EMPTY.getId());
            broke = true;
        }

        if(broke) {
            gamePlayer.addOrUpdateStat(GamePlayerStats.TNT_RUN_DESTROYED_BLOCKS, 1);
        }
    }

    /**
     * Testa um bloco candidato e decide se ele é melhor.
     * Regra de desempate determinística:
     *  - menor distância ao centro do player
     *  - se empatar, prioriza o bloco central (baseX/baseZ)
     */
    private boolean tryCandidate(double px, double pz, int x, int y, int z, CandidateResult result) {

        if (this.world.getBlock(x, y, z) == BlockType.EMPTY_ID) {
            return false;
        }

        final double dx = px - (x + 0.5);
        final double dz = pz - (z + 0.5);
        final double dist = dx * dx + dz * dz;

        if (dist < result.dist || (
                dist == result.dist && x == (int) Math.floor(px) && z == (int) Math.floor(pz)
        )) {

            result.x = x;
            result.y = y;
            result.z = z;
            result.dist = dist;

            return true;
        }

        return false;
    }

    public static class CandidateResult {
        public int x, y, z;
        public double dist;

        public CandidateResult(int x, int y, int z, double dist) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dist = dist;
        }
    }
}
