package dev.thewarrior.SkyBlock.Commands.Island;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ClientEffectWorldSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.FlatWorldGenProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.VoidWorldGenProvider;
import dev.thewarrior.Essentials.Utils.ColorUtil;
import dev.thewarrior.Essentials.Utils.TeleportUtil;
import dev.thewarrior.SkyBlock.Managers.Islands.IslandData;
import dev.thewarrior.SkyBlock.Managers.IslandsManager;
import dev.thewarrior.SkyBlock.Managers.SkyBlockSettingsManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public class IslandCreateCommand extends AbstractPlayerCommand {
    private final IslandsManager islandsManager;
    private final SkyBlockSettingsManager skyBlockSettingsManager;
    private final Instant defaultWorldTime = Instant.parse("0001-01-01T12:00:00Z");

    private final BlockSelection defaultPrefabSelection;

    public IslandCreateCommand(final SkyBlockSettingsManager skyBlockSettingsManager, final IslandsManager islandsManager) {
        super("iniciar", "Cria uma nova ilha no SkyBlock");

        this.skyBlockSettingsManager = skyBlockSettingsManager;
        this.islandsManager = islandsManager;

        this.addAliases("criar", "create", "start");

        this.defaultPrefabSelection = PrefabStore.get().getServerPrefab(this.skyBlockSettingsManager.getDefaultPrefabName());
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext commandContext,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world
    ) {
        if(this.defaultPrefabSelection == null) {
            commandContext.sendMessage(ColorUtil.colorize("&cErro ao processar o comando. Prefab padrão não encontrado."));
            return;
        }

        final Player player = store.getComponent(ref, Player.getComponentType());

        if(player == null || player.wasRemoved()) {
            commandContext.sendMessage(ColorUtil.colorize("&cErro ao processar o comando. Jogador inválido."));
            return;
        }

        final List<IslandData> islands = this.islandsManager.getData().getIslandsForPlayer(playerRef);

        if(this.skyBlockSettingsManager.isPlayerBlockedToCreateIsland(player, islands)) {
            commandContext.sendMessage(ColorUtil.colorize("&cVocê atingiu o limite máximo de ilhas que pode criar. Para criar novas ilhas, assine um de nossos &fVIPs &ce ganhe muitos outros benefícios!"));
            return;
        }

        final String worldName = this.skyBlockSettingsManager.getNewWorldNameForPlayer(
                playerRef, this.islandsManager.getCurrentWorldCountForPlayer(playerRef) + 1
        );

        if(worldName == null || worldName.isEmpty()) {
            commandContext.sendMessage(ColorUtil.colorize("&cErro ao gerar o nome do mundo para a nova ilha."));
            return;
        }

        final Vector3d spawnLocation = this.skyBlockSettingsManager.getDefaultIslandSpawnLocation();
        final Vector3d spawnRotation = this.skyBlockSettingsManager.getDefaultIslandSpawnRotation();

        Universe.get().makeWorld(worldName, Constants.UNIVERSE_PATH.resolve("worlds").resolve(worldName), this.getIslandWorldConfig(spawnLocation))
                .thenAccept(createdWorld -> {
                    boolean hasErrors = true;

                    try {
                        final IslandData island = this.islandsManager.createIslandForPlayer(playerRef, worldName, spawnLocation, spawnRotation);

                        if(island != null) {
                            hasErrors = false;

                            commandContext.sendMessage(ColorUtil.colorize("&aIlha criada com sucesso! Teleportando você para sua nova ilha..."));

                            createdWorld.execute(() -> {
                                this.defaultPrefabSelection.place(null, createdWorld, new Vector3i(0, 100, 0), null);

                                TeleportUtil.teleport(playerRef, store, ref, worldName, spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), (float) spawnRotation.getY(), 0);
                            });
                        }
                    } catch (final Exception e) {
                        commandContext.sendMessage(ColorUtil.colorize("&cErro ao criar os dados da ilha: &f" + e.getMessage()));
                        e.printStackTrace();
                        return;
                    }

                    if(hasErrors) {
                        Universe.get().removeWorld(worldName);
                        commandContext.sendMessage(ColorUtil.colorize("&cErro ao criar a ilha. Tente novamente mais tarde."));
                    }
                })
                .exceptionally((e) -> {
                    commandContext.sendMessage(ColorUtil.colorize("&cErro ao criar o mundo da ilha: &f" + Instant.now().atZone(ZoneId.of("UTC")).toLocalDateTime()));
                    e.printStackTrace();
                    return null;
                });
    }

    private WorldConfig getIslandWorldConfig(Vector3d spawn) {
        WorldConfig config = new WorldConfig();

        config.setUuid(UUID.randomUUID());
        config.setForcedWeather("Zone1_Sunny");
        config.setSpawningNPC(false);
        config.setSpawnProvider(new GlobalSpawnProvider(new Transform(spawn)));
        config.setGameTime(this.defaultWorldTime);

        config.setClientEffects(ClientEffectWorldSettings.CODEC.getDefaultValue());

        config.setWorldGenProvider(new VoidWorldGenProvider(FlatWorldGenProvider.DEFAULT_TINT, "Env_Zone1_Plains"));
        return config;
    }
}
