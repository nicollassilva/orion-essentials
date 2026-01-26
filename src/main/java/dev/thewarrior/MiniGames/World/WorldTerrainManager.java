package dev.thewarrior.MiniGames.World;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages terrain position generation for minigame arenas using a spiral allocation pattern.
 * Each world has its own spiral state, allowing multiple arenas to be placed without overlap.
 * <p>
 * This class is thread-safe and can be used in concurrent environments.
 */
public class WorldTerrainManager {
    /** Default Y coordinate for all arena centers */
    private static final int BASE_Y = 150;

    /** Default spacing between arenas in blocks */
    private static final int DEFAULT_SPACING = 10;

    /** Map of world name to list of generated positions (thread-safe) */
    private final Map<String, List<Vector3i>> generatedPositionsByWorld;

    /** Map of world name to current spiral state (thread-safe) */
    private final Map<String, SpiralState> spiralStateByWorld;

    /** Map of world name to its dedicated lock for fine-grained synchronization */
    private final Map<String, ReentrantLock> worldLocks;

    /** Spacing between arenas */
    private final int spacing;

    public WorldTerrainManager() {
        this(DEFAULT_SPACING);
    }

    public WorldTerrainManager(int spacing) {
        this.generatedPositionsByWorld = new ConcurrentHashMap<>();
        this.spiralStateByWorld = new ConcurrentHashMap<>();
        this.worldLocks = new ConcurrentHashMap<>();
        this.spacing = spacing;
    }

    /**
     * Gets or creates a lock for a specific world.
     * This allows fine-grained locking per world instead of a global lock.
     *
     * @param worldName The name of the world
     * @return The lock for the specified world
     */
    private ReentrantLock getWorldLock(String worldName) {
        return worldLocks.computeIfAbsent(worldName, k -> new ReentrantLock());
    }

    public void start() {
        // Initialize if needed
    }

    /**
     * Generates a center position for a new arena using spiral allocation.
     * The spiral pattern ensures arenas are placed efficiently around the origin.
     * <p>
     * This method is thread-safe and uses per-world locking to allow concurrent
     * position generation in different worlds.
     *
     * @param world The world where the arena will be placed
     * @param gameSettings The game settings containing maxArenaSize
     * @return The center position for the new arena
     */
    public Vector3i generateCenterPosition(final World world, GameSettings gameSettings) {
        String worldName = world.getName();
        ReentrantLock lock = getWorldLock(worldName);

        lock.lock();
        try {
            List<Vector3i> positions = generatedPositionsByWorld.computeIfAbsent(
                    worldName, _ -> Collections.synchronizedList(new ObjectArrayList<>())
            );

            final SpiralState state = spiralStateByWorld.computeIfAbsent(worldName, _ -> new SpiralState());

            // Calculate the cell size: arena size + spacing on each side
            int arenaSize = gameSettings.getMaxArenaSize();
            int cellSize = arenaSize + spacing;

            // Get next spiral coordinates and calculate world position
            int[] gridCoords = state.next();
            int x = gridCoords[0] * cellSize;
            int z = gridCoords[1] * cellSize;

            Vector3i center = new Vector3i(x, BASE_Y, z);

            positions.add(center);

            return center;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets all generated positions for a specific world.
     * <p>
     * Returns an unmodifiable copy to prevent external modification.
     *
     * @param worldName The name of the world
     * @return Unmodifiable list of generated positions, or empty list if none exist
     */
    public List<Vector3i> getGeneratedPositions(String worldName) {
        ReentrantLock lock = getWorldLock(worldName);

        lock.lock();
        try {
            List<Vector3i> positions = generatedPositionsByWorld.get(worldName);

            if (positions == null) {
                return Collections.emptyList();
            }

            return List.copyOf(positions);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the total count of generated positions for a world.
     *
     * @param worldName The name of the world
     * @return The number of generated positions
     */
    public int getPositionCount(String worldName) {
        ReentrantLock lock = getWorldLock(worldName);

        lock.lock();
        try {
            List<Vector3i> positions = generatedPositionsByWorld.get(worldName);
            return positions != null ? positions.size() : 0;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Resets all generated positions for a specific world.
     * <p>
     * This method is thread-safe.
     *
     * @param worldName The name of the world to reset
     */
    public void resetWorld(String worldName) {
        ReentrantLock lock = getWorldLock(worldName);

        lock.lock();

        try {
            generatedPositionsByWorld.remove(worldName);
            spiralStateByWorld.remove(worldName);
        } finally {
            lock.unlock();
        }

        // Clean up the lock itself after reset (optional, prevents memory leak for many worlds)
        worldLocks.remove(worldName);
    }

    /**
     * Resets all generated positions for all worlds.
     * <p>
     * Warning: This method acquires all world locks. Use with caution in production.
     */
    public synchronized void resetAll() {
        for (ReentrantLock lock : worldLocks.values()) {
            lock.lock();
        }

        try {
            generatedPositionsByWorld.clear();
            spiralStateByWorld.clear();
        } finally {
            for (ReentrantLock lock : worldLocks.values()) {
                lock.unlock();
            }

            worldLocks.clear();
        }
    }
}
