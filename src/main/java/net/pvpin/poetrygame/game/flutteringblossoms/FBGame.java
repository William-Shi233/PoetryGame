package net.pvpin.poetrygame.game.flutteringblossoms;

import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.PresetManager;
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
    protected final ConcurrentLinkedDeque<UUID> currentGamers;
    protected final CopyOnWriteArrayList<String> record;
    protected long roundStamp;
    public String keyWord;

    public FBGame() {
        super(GameType.FLUTTERING_BLOSSOMS);
        this.currentGamers = new ConcurrentLinkedDeque<>();
        this.record = new CopyOnWriteArrayList<>();
    }

    public UUID getCurrentPlayer() {
        return currentGamers.peekFirst();
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

            BroadcastUtils.broadcast(
                    Constants.PREFIX + "諸君各言行令之字。",
                    players);
            CompletableFuture<String> future = new CompletableFuture<>();
            FBVote vote = new FBVote(this, future);
            this.keyWord = future.get();
            PresetManager.loadPreset(keyWord);
            BroadcastUtils.broadcast(
                    Constants.PREFIX + "行令字" + keyWord + "字。",
                    players);

            players.forEach(currentGamers::offerFirst);
            Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(Main.class),
                    () -> {
                        new FBTask(this).run();
                    });
            new FBCountDown(this)
                    .runTaskTimerAsynchronously(Main.getPlugin(Main.class), 0L, 10L);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void end() {
        super.end();
        if (getCurrentPlayer() != null) {
            BroadcastUtils.broadcast(
                    Constants.PREFIX +
                            Bukkit.getOfflinePlayer(getCurrentPlayer()).getName() +
                            " 詞源倒流三峡水，筆陣獨掃千人軍。一時無人能抵！"
                    , players
            );
        }
    }
}
