package dev.thewarrior.MiniGames.Gaming.Model;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.Essentials.Utils.Logger;
import dev.thewarrior.Essentials.Utils.TeleportUtil;
import dev.thewarrior.MiniGames.Gaming.Container.GameArena;
import dev.thewarrior.MiniGames.Gaming.Enums.GameState;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Enums.GameWinnerCondition;
import dev.thewarrior.MiniGames.Gaming.Enums.PlayerGameLeaveCause;
import dev.thewarrior.MiniGames.Gaming.GameManager;
import dev.thewarrior.MiniGames.Gaming.Player.GamePlayer;
import dev.thewarrior.MiniGames.Gaming.Player.GamePlayerState;
import dev.thewarrior.MiniGames.Gaming.Player.Session.PlayerGameSession;
import dev.thewarrior.MiniGames.Storage.Prefabs.GamePrefabSpawnData;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;
import dev.thewarrior.MiniGames.Utils.GameUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Game {
    protected final UUID id;

    protected World world;

    protected final GameType type;
    protected final GameArena arena;
    protected final GameSettings settings;

    protected final Map<UUID, GamePlayer> players;
    protected final AtomicReference<GameState> state;

    protected final AtomicLong lastVisitTimestamp;

    protected final AtomicInteger lobbyCountdown = new AtomicInteger(-1);
    protected final AtomicInteger countdownBeforeStart = new AtomicInteger(-1);
    protected final AtomicInteger countdownAfterEnd = new AtomicInteger(-1);

    protected final AtomicInteger gameTick = new AtomicInteger(0);
    protected final AtomicInteger gameElapsedSeconds = new AtomicInteger(0);
    protected final AtomicInteger gameEndTick = new AtomicInteger(-1);

    protected GameWinnerCondition winnerCondition;

    public Game(GameType type, GameArena arena, GameSettings settings) {
        this.id = UUID.randomUUID();
        this.winnerCondition = GameWinnerCondition.NONE;

        this.type = type;
        this.arena = arena;
        this.settings = settings;

        this.state = new AtomicReference<>(GameState.CREATING);
        this.players = new ConcurrentHashMap<>();
        this.lastVisitTimestamp = new AtomicLong(System.currentTimeMillis());

        if(settings.getCountdownBeforeStart() > 0) {
            this.countdownBeforeStart.set(settings.getCountdownBeforeStart());
        }

        if(settings.getCountdownAfterEnd() > 0) {
            this.countdownAfterEnd.set(settings.getCountdownAfterEnd());
        }

        this.world = Universe.get().getWorld(settings.getWorldName());
    }

    /**
     * Reset the game state to initial state for reuse in the pool
     * Called when game is returned to available games queue
     */
    public void reset() {
        this.players.clear();
        this.state.set(GameState.FREE);
        this.lobbyCountdown.set(-1);
        this.countdownBeforeStart.set(-1);
        this.gameTick.set(0);
        this.gameElapsedSeconds.set(0);
        this.lastVisitTimestamp.set(System.currentTimeMillis());
        this.countdownAfterEnd.set(-1);
        this.countdownBeforeStart.set(-1);

        if(settings.getCountdownBeforeStart() > 0) {
            this.countdownBeforeStart.set(settings.getCountdownBeforeStart());
        }

        if(settings.getCountdownAfterEnd() > 0) {
            this.countdownAfterEnd.set(settings.getCountdownAfterEnd());
        }

        this.onGameReset();
    }

    public UUID getId() {
        return id;
    }

    public void onGameCreated() {
        final World world = Universe.get().getWorld(this.settings.getWorldName());

        if(world == null) return;

        world.execute(() -> {
            final BlockSelection prefab = PrefabStore.get().getServerPrefab("tntrun_arena1.prefab.json");

            prefab.place(null, world, this.arena.position(), null);

            this.setState(GameState.CREATING, GameState.WAITING);

            this.onGameReady();
        });
    }

    public void onGameReset() {
        final World world = Universe.get().getWorld(this.settings.getWorldName());

        if(world == null) return;

        world.execute(() -> {
            final BlockSelection prefab = PrefabStore.get().getServerPrefab("tntrun_arena1.prefab.json");

            prefab.place(null, world, this.arena.position(), null);
        });
    }

    public void onGameReady() {
        // Override in subclasses
    }

    public void onGameTick() {
        this.gameTick.incrementAndGet();

        // Calculate elapsed seconds based on tick rate
        // TICK_RATE_MS = 50ms = 0.05s, so we need 20 ticks to make 1 second
        final int ticksPerSecond = (int) (1000 / GameManager.TICK_RATE_MS);

        if (this.gameTick.get() % ticksPerSecond == 0) {
            // Handle state-specific tick logic
            handleStateTick();
        }

        // Override in subclasses
    }

    /**
     * Handles tick logic based on current game state
     * Each state has its own responsibilities
     */
    private void handleStateTick() {
        final GameState currentState = this.state.get();

        // ENDING state: Countdown to return players to server
        if (currentState == GameState.ENDING) {
            handleEndingStateTick();
        }
        // RUNNING state: Update game time and check warnings
        else if (currentState == GameState.RUNNING) {
            handleRunningStateTick();
        }
        // Other states don't need per-second tick handling
    }

    /**
     * Handles ENDING state: Shows countdown and teleports players when done
     * Description: "Time to send rewards and send all to lobby"
     */
    private void handleEndingStateTick() {
        this.broadcastMessageWithPrefix("&eRetornando ao spawn do servidor em &6&l" + this.gameEndTick.get() + " segundos&x&e...");

        if (this.gameEndTick.get() <= 0) {
            this.broadcastMessageWithPrefix("&eA partida terminou! Retornando ao spawn do servidor...");

            // Teleport all remaining players to server spawn
            for (final GamePlayer gamePlayer : this.players.values()) {
                final PlayerRef playerRef = gamePlayer.getPlayer();

                if (playerRef == null || !playerRef.isValid()) continue;

                GameUtil.teleportPlayerToServerSpawn(gamePlayer);
            }

            // Transition to ENDED state
            this.setState(GameState.ENDING, GameState.ENDED);
        }

        this.gameEndTick.decrementAndGet();
    }

    /**
     * Handles RUNNING state: Updates elapsed time and checks for max duration
     * Description: "Game is running"
     */
    private void handleRunningStateTick() {
        this.gameElapsedSeconds.incrementAndGet();

        final int remainingSeconds = this.getGameRemainingSeconds();

        // Check if game duration has been exceeded
        if (remainingSeconds <= 0) {
            this.broadcastMessageWithPrefix("&cTempo da partida expirou! Encerrando jogo...");
            this.onGameEnd();
            return;
        }

        // Send warning messages at specific time intervals
        this.checkTimeWarnings(remainingSeconds);
    }

    public void onCountdownTick() {
        if(this.state.get().canJoin()) {
            this.onLobbyCountdownTick();
        } else {
            this.onGameStartCountdownTick();
        }
    }

    public void onLobbyCountdownTick() {
        if(!this.state.get().canJoin()) return;

        final boolean needsSendMessage = this.lobbyCountdown.get() == 30
                || this.lobbyCountdown.get() == 15
                || this.lobbyCountdown.get() <= 10;

        if(this.lobbyCountdown.get() > 0 && needsSendMessage) {
            this.broadcastMessageWithPrefix(
                    "&eVocê será teleportado para a &6&larena&x&e em &6&l" + this.lobbyCountdown.get() + " segundos&x&e!"
            );
        }

        if(this.lobbyCountdown.get() <= 5) {
            this.setState(GameState.WAITING, GameState.STARTING);
        }

        if(this.lobbyCountdown.get() > 0) {
            this.lobbyCountdown.decrementAndGet();
        } else {
            this.lobbyCountdown.set(-1);
            this.teleportToGame();
            this.setState(GameState.STARTING, GameState.COUNTDOWN);
        }
    }

    public void onPlayerJoin(final PlayerGameSession session) {
        if(this.players.containsKey(session.getPlayerId())) return;

        final PlayerRef playerRef = session.getPlayer();

        if(playerRef == null || !playerRef.isValid()) {
            Logger.warning("Player " + session.getPlayerId() + " has invalid reference, cannot join game");
            return;
        }

        // Validate entity reference before teleporting
        final Ref<EntityStore> ref = playerRef.getReference();
        if(ref == null || !ref.isValid()) {
            Logger.warning("Player " + playerRef.getUuid() + " entity reference is invalid, cannot join game");
            return;
        }

        this.teleportToLobby(playerRef);

        this.players.put(session.getPlayerId(), new GamePlayer(session.getPlayerId()));

        this.lastVisitTimestamp.set(System.currentTimeMillis());

        this.broadcastMessageWithPrefix(
                "&6> " + playerRef.getUsername() + "&f entrou na partida! " + "&e(" + this.players.size() + "/" + this.settings.getMaxPlayersPerGame() + ")"
        );

        if(this.state.get().canJoin()) {
            this.updateLobbyCountdown();
        }
    }

    public void onGameStartCountdownTick() {
        if(this.state.get().canJoin()) return;

        if(this.countdownBeforeStart.get() > 0) {
            this.broadcastMessageWithPrefix("&eA partida começará em &6&l" + this.countdownBeforeStart.get() + " segundos&x&e!");
        } else {
            this.setState(GameState.COUNTDOWN, GameState.RUNNING);
            this.countdownBeforeStart.set(-1);
            this.broadcastMessageWithPrefix("&aA partida começou! Boa sorte a todos!");
            this.onGameStart();
        }

        if(this.countdownBeforeStart.get() > 0) {
            this.countdownBeforeStart.decrementAndGet();
        }
    }

    public void onGameStart() {
        for (final GamePlayer gamePlayer : this.players.values()) {
            if(!gamePlayer.setState(GamePlayerState.WAITING, GamePlayerState.PLAYING)) {
                Logger.warning("Failed to set player " + gamePlayer.getId() + " state to PLAYING on game start");
            }
        }
    }

    public void onGameEnd() {
        if (!this.state.compareAndSet(GameState.RUNNING, GameState.ENDING)) return;

        if(this.winnerCondition != GameWinnerCondition.NONE) {
            if(this.getGameElapsedSeconds() >= this.settings.getMinDuration()) {
                this.onGameRewardWinners();
            } else {
                this.broadcastMessageWithPrefix("&eA partida terminou, mas o tempo mínimo de jogo não foi atingido. Nenhum vencedor será recompensado.");
            }
        } else {
            this.broadcastMessageWithPrefix("&eNenhuma condição de vitória foi atribuída à esse jogo.");
        }

        // Dispatch event
        this.onStateChanged(GameState.RUNNING, GameState.ENDING);

        if(this.settings.getCountdownAfterEnd() > 0) {
            this.gameEndTick.set(this.settings.getCountdownAfterEnd());
        }
    }

    public void onGameRewardWinners() {
        // Override in subclasses
    }

    public void onStateChanged(final GameState previous, final GameState current) {
        // Override in subclasses
    }

    public void onStateNotChanged(final GameState previous, final GameState current) {
        // Override in subclasses
    }

    private void checkTimeWarnings(final int remainingSeconds) {
        if (!this.state.get().isInGame()) return;

        switch (remainingSeconds) {
            case 300: // 5 minutes
                this.broadcastMessageWithPrefix("&fA partida terminará em &6&l5 minutos&f!");
                break;
            case 180: // 3 minutes
                this.broadcastMessageWithPrefix("&fA partida terminará em &6&l3 minutos&f!");
                break;
            case 90: // 1 minute 30 seconds
                this.broadcastMessageWithPrefix("&fA partida terminará em &6&l1 minuto e 30 segundos&f!");
                break;
            case 60: // 1 minute
                this.broadcastMessageWithPrefix("&fA partida terminará em &c&l1 minuto&f!");
                break;
            case 30: // 30 seconds
                this.broadcastMessageWithPrefix("&fA partida terminará em &c&l30 segundos&f!");
                break;
            case 10: // 10 seconds
                this.broadcastMessageWithPrefix("&cA partida terminará em &4&l10 segundos&c!");
                break;
        }
    }

    public int getGameElapsedSeconds() {
        return this.gameElapsedSeconds.get();
    }

    public int getGameRemainingSeconds() {
        return Math.max(0, this.settings.getMaxDuration() - this.gameElapsedSeconds.get());
    }

    private void updateLobbyCountdown() {
        final int currentPlayers = this.players.size();
        final int minPlayers = this.settings.getMinPlayersToStart();
        final int maxPlayers = this.settings.getMaxPlayersPerGame();
        final int twoThirdsPlayers = (maxPlayers * 2) / 3;

        final int currentCountdown = this.lobbyCountdown.get();

        // Full lobby - reduce to minimum countdown
        if (currentPlayers >= maxPlayers) {
            final int targetCountdown = GameManager.COUNTDOWN_SECONDS;

            if (currentCountdown > targetCountdown || currentCountdown == -1) {
                this.lobbyCountdown.set(targetCountdown);
            }

            return;
        }

        // At least 2/3 of players - reduce to medium countdown
        if (currentPlayers >= twoThirdsPlayers) {
            final int targetCountdown = (int) (GameManager.COUNTDOWN_SECONDS * 1.5);

            if (currentCountdown > targetCountdown || currentCountdown == -1) {
                this.lobbyCountdown.set(targetCountdown);
            }

            return;
        }

        // Minimum players reached - start countdown
        if (currentPlayers >= minPlayers && currentCountdown == -1) {
            this.lobbyCountdown.set(GameManager.COUNTDOWN_SECONDS * 3);
        }
    }

    /**
     * Called when a player leaves the game for any reason
     * Handles both lobby and in-game scenarios
     *
     * @param playerId The ID of the player leaving
     * @param cause The reason why the player is leaving
     */
    public void onPlayerLeave(final UUID playerId, final PlayerGameLeaveCause cause) {
        // Remove player from game
        final GamePlayer gamePlayer = this.players.remove(playerId);

        if(gamePlayer == null) return;

        final PlayerRef playerRef = gamePlayer.getPlayer();

        // Build appropriate message based on cause and game state
        String messageToBroadcast = this.buildLeaveMessage(
            playerRef != null && playerRef.isValid() ? playerRef.getUsername() : "Unknown",
            cause
        );

        if(!messageToBroadcast.isEmpty()) {
            this.broadcastMessageWithPrefix(messageToBroadcast);
        }

        // Teleport player back to server spawn (if possible)
        if(playerRef != null && playerRef.isValid()) {
            GameUtil.teleportPlayerToServerSpawn(gamePlayer);
        }

        // Handle game state updates
        this.handleGameStateAfterPlayerLeave(cause);
    }

    /**
     * Builds the message to broadcast when a player leaves
     */
    private String buildLeaveMessage(final String playerName, final PlayerGameLeaveCause cause) {
        return switch (cause) {
            case DISCONNECTED -> {
                String msg = "&6< " + playerName + "&f deixou a partida!";
                if(!this.state.get().canJoin()) {
                    msg += " &c Ele possui 1 minuto para retornar ao jogo.";
                } else {
                    msg += " &e(" + this.players.size() + "/" + this.settings.getMaxPlayersPerGame() + ")";
                }
                yield msg;
            }
            case KICKED_BY_STAFF -> "&c< " + playerName + "&f foi expulso da partida por um membro da equipe!";
            case KICKED_BY_SYSTEM -> "&c< " + playerName + "&f foi removido do jogo pelo sistema.";
            case LEFT_GAME -> "&6< " + playerName + "&f saiu da partida de forma voluntária! Se essa partida já estiver em andamento, ele receberá uma penalidade.";
            case LOST_GAME -> "&c< " + playerName + "&f foi eliminado da partida!";
            default -> "";
        };
    }

    /**
     * Handles game state updates when a player leaves
     */
    private void handleGameStateAfterPlayerLeave(final PlayerGameLeaveCause cause) {
        // If no players left and game is active, end the game
        if(this.players.isEmpty() && this.state.get().isInGame()) {
            this.onGameEnd();
            return;
        }

        // Reset lobby countdown if players leave during lobby phase
        if(this.state.get().canJoin() && this.lobbyCountdown.get() > 0) {
            this.updateLobbyCountdown();
        }
    }

    /**
     * Called when a player dies during the game
     * Determines if the player should be eliminated from the game
     *
     * @param playerId The ID of the player who died
     * @return true if the player should be removed from the game, false otherwise
     */
    public boolean onPlayerDeath(final UUID playerId) {
        // Validate game state
        if(!this.state.get().isInGame()) {
            return false;
        }

        // Validate winner condition is set
        if(this.winnerCondition == GameWinnerCondition.NONE) {
            Logger.error("[Critical] onPlayerDeath called but no winner condition is set!");
            return false;
        }

        // Determine if player should be eliminated based on winner condition
        final boolean shouldRemovePlayer = this.shouldPlayerBeEliminated();

        if(shouldRemovePlayer) {
            // Only broadcast if player is still in game (not already removed)
            if(this.players.containsKey(playerId)) {
                final GamePlayer gamePlayer = this.players.get(playerId);

                if (gamePlayer != null) {
                    final PlayerRef playerRef = gamePlayer.getPlayer();

                    if(playerRef != null && playerRef.isValid()) {
                        this.broadcastMessageWithPrefix("&c< " + playerRef.getUsername() + "&f foi eliminado da partida!");
                    }
                }
            }
        }

        return shouldRemovePlayer;
    }

    /**
     * Determines if a player should be eliminated based on the winner condition
     */
    private boolean shouldPlayerBeEliminated() {
        return switch (this.winnerCondition) {
            case LAST_PLAYER_STANDING -> true;  // Death eliminates player
            case HIGHEST_SCORE -> false;         // Death doesn't eliminate
            case TIME_LIMIT_REACHED -> false;    // Death doesn't eliminate
            case NONE -> false;                  // Should not happen
        };
    }

    public void broadcastMessageWithPrefix(final String message) {
        final String prefix = "&4&l[" + this.type.name() + "] &x";

        this.broadcastMessage(ColorUtil.colorize(prefix + message));
    }

    public void broadcastMessage(final Message message) {
        for(final GamePlayer gamePlayer : this.players.values()) {
            final PlayerRef playerRef = gamePlayer.getPlayer();

            if(playerRef == null || !playerRef.isValid()) continue; // It should not happen

            playerRef.sendMessage(message);
        }
    }

    public GameState getState() {
        return state.get();
    }

    public void setState(final GameState expected, final GameState state) {
        boolean updated = this.state.compareAndSet(expected, state);

        if(updated) this.onStateChanged(expected, state);
        else this.onStateNotChanged(expected, state);
    }

    public Map<UUID, GamePlayer> getPlayers() {
        return this.players;
    }

    protected void teleportToLobby(final PlayerRef playerRef) {
        final Ref<EntityStore> ref = playerRef.getReference();

        if(ref == null || !ref.isValid()) {
            Logger.error("Cannot teleport player " + playerRef.getUuid() + " to lobby: invalid reference");
            return;
        }

        final Store<EntityStore> store = ref.getStore();

        final Vector3f spawnRotation = this.arena.prefab().getLobbySpawnData().getRotation();
        final Vector3d spawnPosition = this.arena.getRespawnPosition(
                this.arena.prefab().getLobbySpawnData().getPosition().clone()
        );

        try {
            TeleportUtil.teleport(
                    playerRef, store, ref, this.settings.getWorldName(),
                    spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ(),
                    spawnRotation.getX(), spawnRotation.getZ()
            );
        } catch (Exception e) {
            Logger.error("Failed to teleport player " + playerRef.getUuid() + " to lobby: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void teleportToGame() {
        final List<GamePrefabSpawnData> spawns = this.arena.prefab().getGameSpawnsData();

        if (spawns == null || spawns.isEmpty()) return;

        final List<GamePlayer> playerList = new ArrayList<>(this.players.values());
        final int playerCount = playerList.size();
        final int spawnCount = spawns.size();

        // Calculate balanced distribution of players per spawn
        final int[] playersPerSpawn = GameUtil.calculateBalancedDistribution(playerCount, spawnCount);

        int playerIndex = 0;
        for (int spawnIndex = 0; spawnIndex < spawnCount && playerIndex < playerCount; spawnIndex++) {
            final GamePrefabSpawnData spawnData = spawns.get(spawnIndex);
            final int playersForThisSpawn = playersPerSpawn[spawnIndex];

            for (int i = 0; i < playersForThisSpawn && playerIndex < playerCount; i++) {
                final GamePlayer gamePlayer = playerList.get(playerIndex);
                final PlayerRef playerRef = gamePlayer.getPlayer();

                if (playerRef != null && playerRef.isValid()) {
                    teleportPlayerToGameSpawn(playerRef, spawnData);
                }

                playerIndex++;
            }
        }
    }

    private void teleportPlayerToGameSpawn(final PlayerRef playerRef, final GamePrefabSpawnData spawnData) {
        final Ref<EntityStore> ref = playerRef.getReference();

        if (ref == null || !ref.isValid()) {
            Logger.error("Cannot teleport player " + playerRef.getUuid() + " to game spawn: invalid reference");
            return;
        }

        final Store<EntityStore> store = ref.getStore();

        final Vector3f spawnRotation = spawnData.getRotation();
        final Vector3d spawnPosition = this.arena.getRespawnPosition(spawnData.getPosition().clone());

        System.out.println("Teleporting player " + playerRef.getUuid());
        try {
            TeleportUtil.teleport(
                    playerRef, store, ref, this.settings.getWorldName(),
                    spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ(),
                    spawnRotation.getX(), spawnRotation.getZ()
            );
        } catch (Exception e) {
            Logger.error("Failed to teleport player " + playerRef.getUuid() + " to game spawn: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
