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
    ),
    POETRY_FILLING(
            ConfigManager.PoetryFilling.MAX_PLAYERS,
            ConfigManager.PoetryFilling.MIN_PLAYERS,
            ConfigManager.PoetryFilling.TIME_BEFORE_INIT
    ),
    MIX(
            Integer.MAX_VALUE,
            Integer.MIN_VALUE,
            Long.MAX_VALUE
    );
    private int maxPlayers;
    private int minPlayers;
    private long timeBeforeInit;

    GameType(int maxPlayers, int minPlayers, long timeBeforeInit) {
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;
        this.timeBeforeInit = timeBeforeInit;
    }

    protected int getMaxPlayers() {
        return maxPlayers;
    }

    protected int getMinPlayers() {
        return minPlayers;
    }

    protected long getTimeBeforeInit() {
        return timeBeforeInit;
    }
}
