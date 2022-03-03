package net.pvpin.poetrygame.api.events.flutteringblossom;

import net.pvpin.poetrygame.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author William_Shi
 */
public class AsyncFBTimeoutEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected Game game;
    protected Player player;

    public AsyncFBTimeoutEvent(Game game, Player player) {
        super(true);
        this.game = game;
        this.player = player;
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

    public Player getPlayer() {
        return player;
    }

}
