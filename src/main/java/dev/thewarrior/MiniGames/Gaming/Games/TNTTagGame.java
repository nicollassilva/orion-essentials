package dev.thewarrior.MiniGames.Gaming.Games;

import dev.thewarrior.MiniGames.Gaming.Container.GameArena;
import dev.thewarrior.MiniGames.Gaming.Enums.GameType;
import dev.thewarrior.MiniGames.Gaming.Model.Game;
import dev.thewarrior.MiniGames.Storage.Settings.GameSettings;

public class TNTTagGame extends Game {
    public TNTTagGame(GameType type, GameArena arena, GameSettings settings) {
        super(type, arena, settings);
    }

    public void onGameTick() {
        super.onGameTick();

        System.out.println("TNTTagGame Tick: " + this.gameTick.get());
    }
}
