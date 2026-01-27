package dev.thewarrior.MiniGames.Utils;

public class GameUtil {
    public static int[] calculateBalancedDistribution(final int toBeBalanced, final int baseCount) {
        final int[] distribution = new int[baseCount];

        if (toBeBalanced <= baseCount) {
            // Each player gets their own spawn (1 or 0 per spawn)
            for (int i = 0; i < toBeBalanced; i++) {
                distribution[i] = 1;
            }
        } else {
            // Distribute players evenly across spawns
            final int basePlayersPerSpawn = toBeBalanced / baseCount;
            final int remainder = toBeBalanced % baseCount;

            for (int i = 0; i < baseCount; i++) {
                // First 'remainder' spawns get one extra player
                distribution[i] = basePlayersPerSpawn + (i < remainder ? 1 : 0);
            }
        }

        return distribution;
    }
}
