package net.pvpin.poetrygame.api.events.poetrywordle;


import net.pvpin.poetrygame.api.poetry.PoetryUtils;
import net.pvpin.poetrygame.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author William_Shi
 */
public class AsyncPWAnswerEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    protected Game game;
    protected String answer;
    protected String playerAnswer;
    protected Player player;

    public AsyncPWAnswerEvent(Game game, String answer, String playerAnswer, Player player) {
        super(true);
        this.game = game;
        this.answer = answer;
        this.playerAnswer = playerAnswer;
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

    public String getAnswer() {
        return answer;
    }

    public String getPlayerAnswer() {
        return playerAnswer;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isAnswerCorrect() {
        return PoetryUtils.strip(playerAnswer).equals(PoetryUtils.strip(answer));
    }
}
