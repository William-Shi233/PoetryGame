package net.pvpin.poetrygame.game.mix;

import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.game.Game;
import net.pvpin.poetrygame.game.GameType;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author William_Shi
 */
public class MixGame extends Game {

    protected String type;
    protected MixTask task;

    public MixGame(String type) {
        super(GameType.MIX);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public int getMaxPlayers() {
        return (int) ConfigManager.Mix.TYPES.get(type).get("maxPlayers");
    }

    @Override
    public int getMinPlayers() {
        return (int) ConfigManager.Mix.TYPES.get(type).get("minPlayers");
    }

    @Override
    public long getTimeBeforeInit() {
        return ((Integer) ConfigManager.Mix.TYPES.get(type).get("timeBeforeInit")).longValue();
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
            this.task = new MixTask(this);
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
        // Do nothing here.
    }

    public void endMix() {
        super.end();
        if (task.WINNERS.isEmpty()) {
            BroadcastUtils.broadcast(
                    Constants.PREFIX + "無人應答。此豈名家詩詞之不知。實乃業荒於嬉也。",
                    players);
            return;
        }
        List<Map.Entry<UUID, AtomicInteger>> rankListAll = new ArrayList<>(task.WINNERS.entrySet());
        rankListAll.sort((o1, o2) -> Integer.compare(o1.getValue().intValue(), o2.getValue().intValue()) * (-1));
        int topScore = rankListAll.get(0).getValue().get();
        StringJoiner joiner = new StringJoiner(" ");
        rankListAll.forEach(rank -> {
            if (rank.getValue().intValue() == topScore) {
                joiner.add(Bukkit.getOfflinePlayer(rank.getKey()).getName());
            }
        });
        BroadcastUtils.broadcast(
                Constants.PREFIX + joiner +
                        " 勝出。",
                players
        );
    }
}
