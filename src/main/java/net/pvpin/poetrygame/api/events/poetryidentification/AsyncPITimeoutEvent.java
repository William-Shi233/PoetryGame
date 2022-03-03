package net.pvpin.poetrygame.api.events.poetryidentification;

import net.pvpin.poetrygame.game.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author William_Shi
 */
public class AsyncPITimeoutEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected Game game;
    protected String question;
    protected String answer;

    public AsyncPITimeoutEvent(Game game, String question, String answer) {
        super(true);
        this.game = game;
        this.question = question;
        this.answer = answer;
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

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

}
