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
import dev.thewarrior.Essentials.Utils.Data.Spawn;
import dev.thewarrior.Essentials.Utils.TeleportUtil;
import dev.thewarrior.MiniGames.Gaming.Container.GameArena;
import dev.thewarrior.MiniGames.Gaming.Enums.GameState;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.GameManager;
import dev.thewarrior.MiniGames.Gaming.Player.GamePlayer;
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

    protected Spawn lobbySpawn;
    protected List<Spawn> gameSpawns;

    protected World world;

    protected final GameType type;
    protected final GameArena arena;
    protected final GameSettings settings;

    protected final Map<UUID, GamePlayer> players;
    protected final AtomicReference<GameState> state;

    protected final AtomicLong lastVisitTimestamp;

    protected final AtomicInteger lobbyCountdown = new AtomicInteger(-1);
    protected final AtomicInteger countdownBeforeStart = new AtomicInteger(-1);

    protected final AtomicInteger gameTick = new AtomicInteger(0);

    public Game(GameType type, GameArena arena, GameSettings settings) {
        this.id = UUID.randomUUID();

        this.type = type;
        this.arena = arena;
        this.settings = settings;

        this.state = new AtomicReference<>(GameState.CREATING);
        this.players = new ConcurrentHashMap<>();
        this.lastVisitTimestamp = new AtomicLong(System.currentTimeMillis());

        if(settings.getCountdownBeforeStart() > 0) {
            this.countdownBeforeStart.set(settings.getCountdownBeforeStart());
        }

        this.world = Universe.get().getWorld(settings.getWorldName());
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

    public void onGameTick() {
        this.gameTick.incrementAndGet();

        // Override in subclasses
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
            this.broadcastMessage(ColorUtil.colorize(
                    "&eVocê será teleportado para a &6&larena&x&e em &6&l" + this.lobbyCountdown.get() + " segundos&x&e!"
            ));
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

    public void onGameReady() {
        // Override in subclasses
    }

    public void onGameStart() {
        // Override in subclasses
    }

    public void onGameStartCountdownTick() {
        if(this.state.get().canJoin()) return;

        if(this.countdownBeforeStart.get() > 0) {
            this.broadcastMessage(ColorUtil.colorize("&eA partida começará em &6&l" + this.countdownBeforeStart.get() + " segundos&x&e!"));
        } else {
            this.setState(GameState.COUNTDOWN, GameState.RUNNING);
            this.countdownBeforeStart.set(-1);
            this.broadcastMessage(ColorUtil.colorize("&aA partida começou! Boa sorte a todos!"));
            this.onGameStart();
        }

        if(this.countdownBeforeStart.get() > 0) {
            this.countdownBeforeStart.decrementAndGet();
        }
    }

    public void onGameEnd() {
        this.setState(GameState.RUNNING, GameState.ENDING);

        // Override in subclasses
    }

    public void onPlayerJoin(final PlayerGameSession session) {
        if(this.players.containsKey(session.getPlayerId())) return;

        final PlayerRef playerRef = session.getPlayer();

        if(playerRef == null || !playerRef.isValid()) return;

        this.teleportToLobby(playerRef);

        this.players.put(session.getPlayerId(), new GamePlayer(session.getPlayerId()));

        this.lastVisitTimestamp.set(System.currentTimeMillis());

        this.broadcastMessage(ColorUtil.colorize(
                "&6> " + playerRef.getUsername() + "&f entrou na partida! " + "&e(" + this.players.size() + "/" + this.settings.getMaxPlayersPerGame() + ")"
        ));

        if(this.state.get().canJoin()) {
            this.updateLobbyCountdown();
        }

        System.out.println(this.state.get().name());
        System.out.println(this.lobbyCountdown.get() + " segundos");
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

    public void onPlayerLeave(final UUID playerId) {
        final GamePlayer gamePlayer = this.players.remove(playerId);

        if(gamePlayer == null) return;

        if(this.players.isEmpty() && this.state.get().isInGame()) {
            this.onGameEnd();
            return;
        }

        final PlayerRef playerRef = gamePlayer.getPlayer();

        String message = "&6< " + playerRef.getUsername() + "&f deixou a partida!";

        if(!this.state.get().canJoin()) {
            message += " &c Ele possui 1 minuto para retornar ao jogo.";
        } else {
            message += " &e(" + this.players.size() + "/" + this.settings.getMaxPlayersPerGame() + ")";
        }

        this.broadcastMessage(ColorUtil.colorize(message));
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

    public void onStateChanged(final GameState previous, final GameState current) {
        // Override in subclasses
    }

    public void onStateNotChanged(final GameState previous, final GameState current) {
        // Override in subclasses
    }

    public Map<UUID, GamePlayer> getPlayers() {
        return this.players;
    }

    protected void teleportToLobby(final PlayerRef playerRef) {
        final Ref<EntityStore> ref = playerRef.getReference();

        if(ref == null || !ref.isValid()) return;

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
                    teleportPlayerToSpawn(playerRef, spawnData);
                }

                playerIndex++;
            }
        }
    }

    private void teleportPlayerToSpawn(final PlayerRef playerRef, final GamePrefabSpawnData spawnData) {
        final Ref<EntityStore> ref = playerRef.getReference();

        if (ref == null || !ref.isValid()) return;

        final Store<EntityStore> store = ref.getStore();

        final Vector3f spawnRotation = spawnData.getRotation();
        final Vector3d spawnPosition = this.arena.getRespawnPosition(spawnData.getPosition().clone());

        try {
            TeleportUtil.teleport(
                    playerRef, store, ref, this.settings.getWorldName(),
                    spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ(),
                    spawnRotation.getX(), spawnRotation.getZ()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
