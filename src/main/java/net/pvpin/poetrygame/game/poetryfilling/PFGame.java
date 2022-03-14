package net.pvpin.poetrygame.game.poetryfilling;

import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.poetry.Poem;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.game.Game;
import net.pvpin.poetrygame.game.GameType;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author William_Shi
 */
public class PFGame extends Game {

    protected PFTask task;

    public PFGame() {
        super(GameType.POETRY_FILLING);
    }

    @Override
    public void start() {
        super.start();
        BroadcastUtils.broadcast(
                Constants.PREFIX +
                        "《詩》云：“日就月將學有緝熙于光明”。詩詞之記誦不可廢止。傳不習乎？",
                players);

        this.task = new PFTask(this);
        Bukkit.getScheduler().runTaskAsynchronously(
                Main.getPlugin(Main.class),
                task::run
                // Do not block GameManager#StartTask.
                // Block another thread using Future#get.
        );
    }

    @Override
    public void end() {
        super.end();
        Map<String, Integer> result = new HashMap<>(16);
        record.stream().filter(Session::isCorrect).forEach(session -> {
            String name = Bukkit.getOfflinePlayer(session.getPlayer()).getName();
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
        StringJoiner joiner = new StringJoiner(" ");
        rankListAll.forEach(rank -> {
            if (rank.getValue() == topScore) {
                joiner.add(rank.getKey());
            }
        });
        BroadcastUtils.broadcast(
                Constants.PREFIX + joiner +
                        " 熟讀《唐詩三百首》。",
                players
        );
    }
}
