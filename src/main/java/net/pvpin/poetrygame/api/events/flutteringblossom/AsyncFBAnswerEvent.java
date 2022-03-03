package net.pvpin.poetrygame.api.events.flutteringblossom;

import net.pvpin.poetrygame.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author William_Shi
 */
public class AsyncFBAnswerEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected Game game;
    protected Player player;
    protected String content;
    protected Result result;

    public AsyncFBAnswerEvent(Game game, Player player, String content, Result result) {
        super(true);
        this.game = game;
        this.player = player;
        this.content = content;
        this.result = result;
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

    public enum Result {
        REDUNDANT_POEM,
        UNKNOWN_POEM,
        SUCCESS;
    }
}
