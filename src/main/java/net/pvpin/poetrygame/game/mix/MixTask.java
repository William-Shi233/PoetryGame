package net.pvpin.poetrygame.game.mix;

import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.poetry.PoetryUtils;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.game.Game;
import net.pvpin.poetrygame.game.flutteringblossoms.FBTask;
import net.pvpin.poetrygame.game.poetryfilling.PFTask;
import net.pvpin.poetrygame.game.poetryidentification.PITask;
import net.pvpin.poetrygame.game.poetrywordle.PWTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author William_Shi
 */
class MixTask {
    protected MixGame game;
    protected final MemorySection CONFIG;
    protected final AtomicInteger currentRound = new AtomicInteger(0);
    protected final Map<UUID, AtomicInteger> WINNERS = new ConcurrentHashMap<>(16);

    protected MixTask(MixGame game) {
        this.game = game;
        this.CONFIG = ConfigManager.Mix.TYPES.get(game.type);
    }

    protected void run() {
        do {
            try {
                Thread.sleep(10000);
            } catch (Exception ex) {
                ex.printStackTrace();
                // Sleep the thread to make sure messages of the next round
                // are sent after those of the last round.
            }
            String roundType = ((List<String>) CONFIG.get("rounds")).get(currentRound.get());
            game.getRecord().clear();
            switch (PoetryUtils.strip(roundType).toLowerCase()) {
                case "flutteringblossoms": {
                    BroadcastUtils.broadcast(
                            Constants.PREFIX +
                                    "本局為飛花令。",
                            game.getPlayers()
                    );
                    var task = new FBTask(game);
                    task.run();
                    addWinner(List.of(task.getCurrent()));
                    break;
                }
                case "poetryidentification": {
                    BroadcastUtils.broadcast(
                            Constants.PREFIX +
                                    "本局當辨認詩句。",
                            game.getPlayers()
                    );
                    var task = new PITask(game);
                    task.rounds = 1;
                    task.run();
                    addWinner(calculateWinners());
                    break;
                }
                case "poetrywordle": {
                    BroadcastUtils.broadcast(
                            Constants.PREFIX +
                                    "本局當猜測詩句。",
                            game.getPlayers()
                    );
                    var task = new PWTask(game);
                    task.rounds = 1;
                    task.run();
                    addWinner(calculateWinners());
                    break;
                }
                case "poetryfilling": {
                    BroadcastUtils.broadcast(
                            Constants.PREFIX +
                                    "本局當默寫詩句。",
                            game.getPlayers()
                    );
                    var task = new PFTask(game);
                    task.rounds = 1;
                    task.run();
                    addWinner(calculateWinners());
                    break;
                }
            }
        } while (currentRound.incrementAndGet() < ((List<String>) CONFIG.get("rounds")).size());
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
            game.endMix();
        }, 5L);
    }

    private List<UUID> calculateWinners() {
        Map<UUID, Integer> result = new HashMap<>(16);
        List<UUID> ret = new ArrayList<>(16);
        game.getRecord().stream().filter(Game.Session::isCorrect).forEach(session -> {
            if (result.containsKey(session.getPlayer())) {
                result.put(session.getPlayer(), result.get(session.getPlayer()) + 1);
            } else {
                result.put(session.getPlayer(), 1);
            }
        });
        if (result.isEmpty()) {
            return List.of();
        }
        List<Map.Entry<UUID, Integer>> rankListAll = new ArrayList<>(result.entrySet());
        rankListAll.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()) * (-1));
        int topScore = rankListAll.get(0).getValue();
        rankListAll.forEach(rank -> {
            if (rank.getValue() == topScore) {
                ret.add(rank.getKey());
            }
        });
        return ret;
    }

    private void addWinner(List<UUID> uuids) {
        uuids.forEach(uuid -> {
            if (WINNERS.containsKey(uuid)) {
                WINNERS.get(uuid).incrementAndGet();
            } else {
                WINNERS.put(uuid, new AtomicInteger(1));
            }
        });
    }
}
