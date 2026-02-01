package dev.thewarrior.SkyBlock.Events;


import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class CobblestoneGeneratorHandler extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    private static final String COBBLESTONE = "Rock_Stone_Cobble";
    private static final String LAVA_SOURCE_BLOCK = "Fluid_Lava";
    private static final String WATER_SOURCE_BLOCK = "Fluid_Water";
    private static final int LAVA_MIN_ID = 6;
    private static final int LAVA_MAX_ID = 7;
    private static final int WATER_MIN_ID = 8;
    private static final int WATER_MAX_ID = 11;
    private static final Random RANDOM = new Random();
    private static final boolean DEBUG = false;
    private static final double ORE_CHANCE = 0.5; // 10% chance to generate ore instead of cobblestone

    private static long getRegenDelayMs() {
        return 1000L;
    }

    private static boolean isEnabled() {
        return true;
    }

    public CobblestoneGeneratorHandler() {
        super(BreakBlockEvent.class);
    }

    private static void debug(String message) {
        if (DEBUG) {
            System.out.println("[CobbleGen] " + message);
        }
    }

    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull BreakBlockEvent event) {
        try {
            if (!isEnabled()) {
                debug("Generator is DISABLED in config, skipping");
                return;
            }

            Vector3i pos = event.getTargetBlock();
            BlockType blockType = event.getBlockType();
            if (pos == null || blockType == null) {
                debug("pos or blockType is null, skipping");
                return;
            }

            String blockKey = blockType.getId();
            debug("Block broken: " + blockKey + " at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
            if (!"Rock_Stone_Cobble".equals(blockKey) && !blockKey.contains("Cobble") && !blockKey.contains("cobble")) {
                debug("Not cobblestone, skipping (block was: " + blockKey + ")");
                return;
            }

            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            EntityStore entityStore = (EntityStore)store.getExternalData();
            if (entityStore == null) {
                debug("entityStore is null, skipping");
                return;
            }

            World world = entityStore.getWorld();
            if (world == null) {
                debug("world is null, skipping");
                return;
            }

            Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
            Player player = entityRef != null ? (Player)store.getComponent(entityRef, Player.getComponentType()) : null;
            Predicate<String> hasPermission = (perm) -> {
                if (player == null) {
                    return false;
                } else {
                    try {
                        return player.hasPermission(perm);
                    } catch (Exception var3) {
                        return false;
                    }
                }
            };
            boolean hasLava = hasNearbyFluid(world, x, y, z, true);
            boolean hasWater = hasNearbyFluid(world, x, y, z, false);
            debug("Fluid check - hasLava: " + hasLava + ", hasWater: " + hasWater);
            if (hasLava && hasWater) {
                String worldName = world.getName();
                debug("Both fluids found! Scheduling regeneration in world: " + worldName);
                scheduleRegeneration(world, worldName, x, y, z, hasPermission);
            } else {
                debug("Missing fluid - not a valid generator setup");
            }
        } catch (Exception var20) {
            System.err.println("[CobbleGen] Error: " + var20.getMessage());
            var20.printStackTrace();
        }

    }

    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    private static boolean hasNearbyFluid(World world, int x, int y, int z, boolean lookingForLava) {
        int[][] offsets = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, 1, 0}, {0, -1, 0}};

        for (int[] offset : offsets) {
            try {
                int checkX = x + offset[0];
                int checkY = y + offset[1];
                int checkZ = z + offset[2];
                int fluidId = world.getFluidId(checkX, checkY, checkZ);
                System.out.println("Checking fluid at: " + checkX + ", " + checkY + ", " + checkZ + " - fluidId: " + fluidId);
                if (fluidId > 0) {
                    if (!lookingForLava && fluidId >= 6 && fluidId <= 7) {
                        debug("Found lava at: " + checkX + ", " + checkY + ", " + checkZ);
                        return true;
                    }

                    if (lookingForLava && fluidId >= 8 && fluidId <= 11) {
                        debug("Found water at: " + checkX + ", " + checkY + ", " + checkZ);
                        return true;
                    }
                }

                BlockType blockType = world.getBlockType(checkX, checkY, checkZ);
                if (blockType != null) {
                    String blockId = blockType.getId();
                    if (!lookingForLava || !"Fluid_Lava".equals(blockId) && !blockId.contains("Lava")) {
                        if (lookingForLava || !"Fluid_Water".equals(blockId) && (!blockId.contains("Water") || !blockId.startsWith("Fluid"))) {
                            continue;
                        }

                        return true;
                    }

                    return true;
                }
            } catch (Exception e) {
                // Ignore exceptions for out of bounds or other issues
            }
        }

        return false;
    }

    private static void scheduleRegeneration(World world, String worldName, int x, int y, int z, Predicate<String> hasPermission) {
        long delayMs = getRegenDelayMs();
        debug("Scheduling regen at (" + x + ", " + y + ", " + z + ") with delay: " + delayMs + "ms");
        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS).execute(() -> {
            debug("Delay elapsed, attempting world.execute() for (" + x + ", " + y + ", " + z + ")");
            world.execute(() -> {
                try {
                    BlockType currentBlock = world.getBlockType(x, y, z);
                    String currentBlockId = currentBlock != null ? currentBlock.getId() : "null";
                    debug("Current block at location: " + currentBlockId);
                    boolean willGenerateOre = RANDOM.nextDouble() < ORE_CHANCE;
                    debug("Ore chance: " + ORE_CHANCE + ", will generate ore: " + willGenerateOre);
                    String blockToPlace;
                    if (willGenerateOre) {
                        blockToPlace = selectRandomOre(hasPermission);
                        debug("Selected ore: " + blockToPlace);
                    } else {
                        blockToPlace = COBBLESTONE;
                        debug("Selected cobblestone");
                    }

                    debug("Placing block: " + blockToPlace + " at (" + x + ", " + y + ", " + z + ")");

                    try {
                        world.setBlock(x, y, z, blockToPlace, 0);
                        debug("setBlock called successfully!");
                    } catch (IllegalArgumentException var18) {
                        debug("Unknown block '" + blockToPlace + "', falling back to cobblestone. Remove this block from config.json generator.ores");
                        world.setBlock(x, y, z, COBBLESTONE, 0);
                        blockToPlace = COBBLESTONE;
                    }

                    BlockType verifyBlock = world.getBlockType(x, y, z);
                    String verifyBlockId = verifyBlock != null ? verifyBlock.getId() : "null";
                    debug("Verification - block at location is now: " + verifyBlockId);
//                    if (worldName != null && worldName.startsWith("void_")) {
//                        IslandManager islandManager = HTSkyBlock.getInstance().getIslandManager();
//                        if (islandManager != null) {
//                            islandManager.updateIslandScore(worldName, blockToPlace, true);
//                        }
//                    }
                } catch (Exception var19) {
                    debug("ERROR in world.execute: " + var19.getMessage());
                    var19.printStackTrace();
                }

            });
        });
    }

    private static String selectRandomOre(Predicate<String> hasPermission) {
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
