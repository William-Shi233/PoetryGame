package net.pvpin.poetrygame.api.events.flutteringblossom;

import net.pvpin.poetrygame.game.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author William_Shi
 */
public class AsyncFBKeyWordGenEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected Game game;
    protected String keyWord;

    public AsyncFBKeyWordGenEvent(Game game, String keyWord) {
        super(true);
        this.game = game;
        this.keyWord = keyWord;
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

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

}
