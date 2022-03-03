package net.pvpin.poetrygame.game;

import net.pvpin.poetrygame.api.utils.ConfigManager;

/**
 * @author William_Shi
 */
public enum GameType {
    FLUTTERING_BLOSSOMS(
            ConfigManager.FlutteringBlossoms.MAX_PLAYERS,
            ConfigManager.FlutteringBlossoms.MIN_PLAYERS,
            ConfigManager.FlutteringBlossoms.TIME_BEFORE_INIT
    ),
    POETRY_WORDLE(
            ConfigManager.PoetryWordle.MAX_PLAYERS,
            ConfigManager.PoetryWordle.MIN_PLAYERS,
            ConfigManager.PoetryWordle.TIME_BEFORE_INIT
    ),
    POETRY_IDENTIFICATION(
            ConfigManager.PoetryIdentification.MAX_PLAYERS,
            ConfigManager.PoetryIdentification.MIN_PLAYERS,
            ConfigManager.PoetryIdentification.TIME_BEFORE_INIT
    );
    private int maxPlayers;
    private int minPlayers;
    private long timeBeforeInit;

    private GameType(int maxPlayers, int minPlayers, long timeBeforeInit) {
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;
        this.timeBeforeInit = timeBeforeInit;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public long getTimeBeforeInit() {
        return timeBeforeInit;
    }
}
