package dev.thewarrior.SkyBlock.Events;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.thewarrior.Essentials.Utils.Logger;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CobblestoneGeneratorHandler extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    private static final String STONE_MATCHING = "_Stone";
    private static final String STONE_FALLBACK = "Rock_Stone_Cobble";
    private static final Random RANDOM = new Random();

    protected static final Vector2i[] ORTO_OFFSETS = new Vector2i[] {
            new Vector2i(-1, 0),
            new Vector2i(1, 0),
            new Vector2i(0, -1),
            new Vector2i(0, 1)
    };

    public CobblestoneGeneratorHandler() {
        super(BreakBlockEvent.class);
    }

    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull BreakBlockEvent event
    ) {
        try {
            final Vector3i targetPosition = event.getTargetBlock();
            final String blockKey = event.getBlockType().getId();

            if (!blockKey.contains(STONE_MATCHING)) {
                System.out.println("Not cobblestone, skipping (block was: " + blockKey + ")");
                return;
            }

            final World world = store.getExternalData().getWorld();

            if(!world.getName().startsWith("island_")) return;

            final boolean needsToBeRegenerate = isValidGenerator(world, targetPosition);

            if (!needsToBeRegenerate) return;

            scheduleRegeneration(world, world.getName(), targetPosition);
        } catch (Exception e) {
            Logger.error("Error in CobblestoneGeneratorHandler: " + e.getMessage());
        }

    }

    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    private static boolean isValidGenerator(World world, Vector3i pos) {
        int x = pos.getX();
        int z = pos.getZ();

        boolean foundLava = false, foundWater = false;

        for (final Vector2i offset : ORTO_OFFSETS) {
            try {
                int checkX = x + offset.getX();
                int checkZ = z + offset.getY();
                int fluidId = world.getFluidId(checkX, pos.getY(), checkZ);

                foundLava |= fluidId == 6; // Lava fluid ID
                foundWater |= fluidId == 7; // Water fluid ID

                if(foundLava && foundWater) {
                    return true;
                }
            } catch (Exception e) {
                // Ignore exceptions for out of bounds or other issues
            }
        }

        return false;
    }

    private static void scheduleRegeneration(World world, String worldName, Vector3i position) {
        long delayMs = 500L;

        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS).execute(() -> {
            world.execute(() -> {
                try {
                    final BlockType currentBlock = world.getBlockType(position.getX(), position.getY(), position.getZ());

                    if(currentBlock == null) return;

                    boolean willGenerateOre = RANDOM.nextDouble() < 0.9;

                    String blockToPlace;

                    if (willGenerateOre) {
                        blockToPlace = selectRandomOre();
                    } else {
                        blockToPlace = STONE_FALLBACK;
                    }

                    try {
                        world.setBlock(position.getX(), position.getY(), position.getZ(), blockToPlace, 0);
                    } catch (IllegalArgumentException var18) {
                        blockToPlace = STONE_FALLBACK;

                        world.setBlock(position.getX(), position.getY(), position.getZ(), blockToPlace, 0);
                    }
                } catch (Exception var19) {
                    Logger.error("Error during cobblestone generator regeneration: " + var19.getMessage());
                }
            });
        });
    }

    private static String selectRandomOre() {
        final List<String> ores = List.of(
                "Ore_Copper_Stone",
                "Ore_Gold_Stone",
                "Ore_Iron_Stone",
                "Ore_Coal_Stone",
                "Ore_Mithril_Stone",
                "Ore_Silver_Stone"
        );

        return ores.stream()
                .skip(RANDOM.nextInt(ores.size()))
                .findFirst()
                .orElse("Rock_Stone_Cobble");
    }
}
