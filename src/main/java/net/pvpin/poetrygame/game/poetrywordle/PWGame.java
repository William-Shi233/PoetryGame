package net.pvpin.poetrygame.game.poetrywordle;

import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.game.Game;
import net.pvpin.poetrygame.game.GameType;
import org.bukkit.Bukkit;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author William_Shi
 */
public class PWGame extends Game {
    protected final CopyOnWriteArrayList<Session> record;
    protected String currentAnswer;

    public PWGame() {
        super(GameType.POETRY_WORDLE);
        this.record = new CopyOnWriteArrayList<>();
    }

    @Override
    public void start() {
        super.start();
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(Main.class),
                () -> {
                    new PWTask(this).run();
                });
    }

    @Override
    public void end() {
        super.end();
        if (record.isEmpty()) {
            BroadcastUtils.broadcast(
                    Constants.PREFIX + "無人應答。",
                    players);
            return;
        }
        Session answer = record.get(record.size() - 1);
        if (!answer.isCorrect()) {
            BroadcastUtils.broadcast(
                    Constants.PREFIX + "無人猜中。",
                    players);
        } else {
            BroadcastUtils.broadcast(
                    Constants.PREFIX + Bukkit.getOfflinePlayer(answer.getPlayer()).getName() +
                            " 猜中。",
                    players);
        }
    }
}
