package dev.thewarrior.MiniGames.World;

/**
 * Maintains the state for spiral coordinate generation.
 * Generates coordinates in a clockwise spiral pattern starting from origin:
 * <p>
 *  16 15 14 13 12 <p>
 *  17 04 03 02 11 <p>
 *  18 05 00 01 10 <p>
 *  19 06 07 08 09 <p>
 *  20 21 22 23 24 <p>
 * <p>
 * Pattern: Right -> Down -> Left -> Up -> (expand and repeat)
 */
public class SpiralState {
    private int x = 0;
    private int z = 0;
    private int direction = 0; // 0=right, 1=down, 2=left, 3=up
    private int stepsInCurrentDirection = 0;
    private int stepsBeforeTurn = 1;
    private int turnCount = 0;
    private boolean isFirstCall = true;

    /**
     * Returns the next grid coordinates in the spiral pattern.
     *
     * @return Array of [x, z] grid coordinates
     */
    public int[] next() {
        // First call returns origin (0, 0)
        if (isFirstCall) {
            isFirstCall = false;
            return new int[]{x, z};
        }

        // Move in current direction
        move();
        stepsInCurrentDirection++;

        // Check if we need to turn
        if (stepsInCurrentDirection >= stepsBeforeTurn) {
            stepsInCurrentDirection = 0;
            direction = (direction + 1) % 4; // Turn clockwise
            turnCount++;

            // After every 2 turns, increase the steps before next turn
            if (turnCount % 2 == 0) {
                stepsBeforeTurn++;
            }
        }

        return new int[]{x, z};
    }

    /**
     * Moves one step in the current direction.
     */
    private void move() {
        switch (direction) {
            case 0 -> x++;      // Right (+X)
            case 1 -> z++;      // Down (+Z)
            case 2 -> x--;      // Left (-X)
            case 3 -> z--;      // Up (-Z)
        }
    }

    /**
     * Gets the current layer/ring number in the spiral.
     * Layer 0 is the center, layer 1 is the first ring around it, etc.
     *
     * @return The current layer number
     */
    public int getCurrentLayer() {
        return Math.max(Math.abs(x), Math.abs(z));
    }
}
