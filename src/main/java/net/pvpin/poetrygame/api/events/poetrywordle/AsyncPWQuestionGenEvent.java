package net.pvpin.poetrygame.api.events.poetrywordle;

import net.pvpin.poetrygame.game.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * @author William_Shi
 */
public class AsyncPWQuestionGenEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected Game game;
    protected String answer;

    public AsyncPWQuestionGenEvent(Game game, String answer) {
        super(true);
        this.game = game;
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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
