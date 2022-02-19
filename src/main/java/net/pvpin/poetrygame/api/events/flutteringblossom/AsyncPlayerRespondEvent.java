package net.pvpin.poetrygame.api.events.flutteringblossom;

import net.pvpin.poetrygame.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author William_Shi
 */
public class AsyncPlayerRespondEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected Game game;
    protected Player player;
    protected String content;

    public AsyncPlayerRespondEvent(Game game, Player player, String content) {
        super(true);
        this.game = game;
        this.player = player;
        this.content = content;
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

    public String getContent() {
        return content;
    }

}
