package net.pvpin.poetrygame.game.flutteringblossoms;

import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.poetry.PoetryUtils;
import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.game.Game;
import net.pvpin.poetrygame.game.GameType;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author William_Shi
 */
public class FBGame extends Game {

    protected FBTask task;

    public FBGame() {
        super(GameType.FLUTTERING_BLOSSOMS);
    }

    @Override
    public void start() {
        try {
            super.start();
            BroadcastUtils.broadcast(
                    Constants.PREFIX +
                            Constants.FLUTTERING_BLOSSOMS_POEMS.get(
                                    ThreadLocalRandom.current().nextInt(Constants.FLUTTERING_BLOSSOMS_POEMS.size())
                            ),
                    players);

            this.task = new FBTask(this);
            Bukkit.getScheduler().runTaskAsynchronously(
                    Main.getPlugin(Main.class),
                    task::run
                    // Do not block GameManager#StartTask.
                    // Block another thread using Future#get.
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void end() {
        super.end();
        if (task.currentGamers.peekFirst() != null) {
            BroadcastUtils.broadcast(
                    Constants.PREFIX +
                            Bukkit.getOfflinePlayer(task.currentGamers.peekFirst()).getName() +
                            " 詞源倒流三峡水。筆陣獨掃千人軍。一時無人能抵！",
                    players
            );
        }
    }
}
