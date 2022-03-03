package net.pvpin.poetrygame.api.events.common;

import net.pvpin.poetrygame.game.Game;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author William_Shi
 */
public class AsyncGameStartEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    protected Game game;
    protected boolean cancelled = false;

    public AsyncGameStartEvent(Game game) {
        super(true);
        this.game = game;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Game getGame() {
        return game;
    }
}
