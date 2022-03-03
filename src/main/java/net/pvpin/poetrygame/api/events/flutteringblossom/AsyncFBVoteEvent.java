package net.pvpin.poetrygame.api.events.flutteringblossom;

import net.pvpin.poetrygame.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author William_Shi
 */
public class AsyncFBVoteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    protected boolean cancelled = false;
    protected Game game;
    protected Player player;
    protected String key;

    public AsyncFBVoteEvent(Game game, Player player, String key) {
        super(true);
        this.game = game;
        this.player = player;
        this.key = key;
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
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public Game getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
