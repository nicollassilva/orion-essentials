package dev.thewarrior.Utils;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nullable;
import java.util.List;

public class PlayerUtils {
    /**
     * Find a player by name (case-insensitive).
     */
    @Nullable
    public static PlayerRef findPlayer(String name) {
        List<PlayerRef> players = Universe.get().getPlayers();

        for (PlayerRef player : players) {
            if (player.getUsername().equalsIgnoreCase(name)) {
                return player;
            }
        }

        return null;
    }
}
