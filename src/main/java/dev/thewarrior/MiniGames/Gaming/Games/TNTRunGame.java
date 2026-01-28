package dev.thewarrior.MiniGames.Gaming.Games;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.MiniGames.Gaming.Container.GameArena;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.GameManager;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
import dev.thewarrior.MiniGames.Gaming.Player.GamePlayer;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;

import java.util.HashSet;
import java.util.Set;

public class TNTRunGame extends Game {
    public TNTRunGame(GameType type, GameArena arena, GameSettings settings) {
        super(type, arena, settings);
    }

    public void onGameTick() {
        super.onGameTick();

        if(this.gameTick.get() % (1_000 / GameManager.TICK_RATE_MS) != 0) return;

        this.world.execute(() -> {
            Set<Vector3i> positionsToClear = new HashSet<>();

            for (final GamePlayer gamePlayer : this.players.values()) {
                final PlayerRef playerRef = gamePlayer.getPlayer();

                if(playerRef == null || !playerRef.isValid()) {
                    // It should not happen
                    continue;
                }

                final Ref<EntityStore> ref = playerRef.getReference();

                if(ref == null || !ref.isValid()) {
                    // It should not happen
                    continue;
                }

                final Store<EntityStore> store = ref.getStore();
                final TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());

                if(transform == null) {
                    // It should not happen
                    continue;
                }

                MovementStatesComponent movements = store.getComponent(ref, MovementStatesComponent.getComponentType());

                if(movements == null) {
                    // It should not happen
                    continue;
                }

                if(movements.getMovementStates().falling) {
                    System.out.println("Flying");
                    continue;
                }

                final Vector3d playerPosition = transform.getPosition();
                final Vector3i playerPositionI = playerPosition.toVector3i().subtract(0, (movements.getMovementStates().jumping ? 2 : 1), 0);

                final boolean isOffsetToRight = playerPosition.getX() - playerPositionI.getX() > 0.3;
                final boolean isOffsetToFront = playerPosition.getZ() - playerPositionI.getZ() > 0.3;
                final boolean isOffsetToLeft = playerPosition.getX() - playerPositionI.getX() < 0.3;
                final boolean isOffsetToBack = playerPosition.getZ() - playerPositionI.getZ() < 0.3;

                final boolean isOffsetToDiagonal = (isOffsetToRight || isOffsetToLeft) && (isOffsetToFront || isOffsetToBack);

                if(this.world.getBlock(playerPositionI) != BlockType.EMPTY_ID) {
                    positionsToClear.add(playerPositionI);
                }

                if(isOffsetToRight && this.world.getBlock(playerPositionI.getX() + 1, playerPositionI.getY(), playerPositionI.getZ()) != BlockType.EMPTY_ID) {
                    positionsToClear.add(playerPositionI.add(1, 0, 0));
                } else if(isOffsetToLeft && this.world.getBlock(playerPositionI.getX() - 1, playerPositionI.getY(), playerPositionI.getZ()) != BlockType.EMPTY_ID) {
                    positionsToClear.add(playerPositionI.add(-1, 0, 0));
                } else if(isOffsetToFront && this.world.getBlock(playerPositionI.getX(), playerPositionI.getY(), playerPositionI.getZ() + 1) != BlockType.EMPTY_ID) {
                    positionsToClear.add(playerPositionI.add(0, 0, 1));
                } else if(isOffsetToBack && this.world.getBlock(playerPositionI.getX(), playerPositionI.getY(), playerPositionI.getZ() - 1) != BlockType.EMPTY_ID) {
                    positionsToClear.add(playerPositionI.add(0, 0, -1));
                } else if (isOffsetToDiagonal) {
                    int offsetX = isOffsetToRight ? 1 : -1;
                    int offsetZ = isOffsetToFront ? 1 : -1;

                    if(this.world.getBlock(playerPositionI.getX() + offsetX, playerPositionI.getY(), playerPositionI.getZ() + offsetZ) != BlockType.EMPTY_ID) {
                        positionsToClear.add(playerPositionI.add(offsetX, 0, offsetZ));
                    }
                }
            }

            for (final Vector3i position : positionsToClear) {
                this.world.setBlock(position.getX(), position.getY(), position.getZ(), BlockType.EMPTY.getId());
            }
        });
    }
}
