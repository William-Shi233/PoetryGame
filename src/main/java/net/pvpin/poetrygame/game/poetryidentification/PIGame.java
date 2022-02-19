package net.pvpin.poetrygame.game.poetryidentification;

import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.game.Game;
import net.pvpin.poetrygame.game.GameType;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author William_Shi
 */
public class PIGame extends Game {
    protected AtomicInteger currentRound;
    protected final CopyOnWriteArrayList<List<String>> record;
    protected long roundStamp;
    protected String currentQuestion;
    protected String currentAnswer;

    public PIGame() {
        super(GameType.POETRY_IDENTIFICATION);
        this.currentRound = new AtomicInteger(0);
        this.record = new CopyOnWriteArrayList<>();
    }

    @Override
    public void start() {
        super.start();
        BroadcastUtils.broadcast(
                Constants.PREFIX +
                        "足下之目视，明足以察秋毫之末乎？",
                players);
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(Main.class),
                () -> {
                    new PITask(this).run();
                });
        new PICountDown(this)
                .runTaskTimerAsynchronously(Main.getPlugin(Main.class), 0L, 10L);
    }

    @Override
    public void end() {
        super.end();
        Map<String, Integer> result = new HashMap<>(16);
        record.forEach(list -> {
            String name = list.get(0);
            if (result.containsKey(name)) {
                result.put(name, result.get(name) + 1);
            } else {
                result.put(name, 1);
            }
        });
        if (result.isEmpty()) {
            BroadcastUtils.broadcast(
                    Constants.PREFIX + "無人應答。此豈名家詩詞之不知。實乃業荒於嬉也。",
                    players);
            return;
        }
        List<Map.Entry<String, Integer>> rankListAll = new ArrayList<>(result.entrySet());
        rankListAll.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()) * (-1));
        int topScore = rankListAll.get(0).getValue();
        List<String> tops = new ArrayList<>(16);
        rankListAll.forEach(rank -> {
            if (rank.getValue() == topScore) {
                tops.add(rank.getKey());
            }
        });
        StringBuilder builder = new StringBuilder();
        tops.forEach(top -> {
            builder.append(top);
            builder.append(" ");
        });
        BroadcastUtils.broadcast(
                Constants.PREFIX + builder.toString() +
                        "辛勤萬卷讀，不負百年眼。",
                players
        );
    }
}
