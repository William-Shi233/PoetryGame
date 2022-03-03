package net.pvpin.poetrygame.api.events.common;

import net.pvpin.poetrygame.game.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author William_Shi
 */
public class AsyncGameEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected Game game;
    protected EndReason reason;

    public AsyncGameEndEvent(Game game, EndReason reason) {
        super(true);
        this.game = game;
        this.reason = reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Game getGame() {
        return game;
    }

    public EndReason getReason() {
        return reason;
    }

    public enum EndReason {
        GAME_FINISH,
        PLAYER_QUIT,
        PLAYER_NOT_ENOUGH;
    }
}
