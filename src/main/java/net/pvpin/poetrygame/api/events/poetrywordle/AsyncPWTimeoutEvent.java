package net.pvpin.poetrygame.api.events.poetrywordle;

import net.pvpin.poetrygame.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author William_Shi
 */
public class AsyncPWTimeoutEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected Game game;

    public AsyncPWTimeoutEvent(Game game) {
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
}
