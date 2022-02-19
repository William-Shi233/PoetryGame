package net.pvpin.poetrygame.game.flutteringblossoms;

import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.api.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author William_Shi
 */
public class FBVote {
    private FBGame game;
    private CompletableFuture<String> future;
    private VoteListener listener;
    private Map<UUID, String> result = new ConcurrentHashMap<>();

    protected FBVote(FBGame game, CompletableFuture<String> future) {
        this.game = game;
        this.future = future;
        this.listener = new VoteListener();
        Bukkit.getPluginManager().registerEvents(listener, Main.getPlugin(Main.class));
        // future.completeOnTimeout(getResult(), 10000L, TimeUnit.MILLISECONDS);
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
            future.complete(getResult());
        }, (ConfigManager.FlutteringBlossoms.TIME_VOTE / 1000) * 20L);
    }

    private String getResult() {
        HandlerList.unregisterAll(listener);
        Map<String, AtomicInteger> temp = new HashMap<>();
        result.forEach((uuid, s) -> {
            var value = temp.containsKey(s) ? temp.get(s).incrementAndGet() : temp.put(s, new AtomicInteger(1));
        });
        List<Map.Entry<String, AtomicInteger>> rankListAll = new ArrayList<>(temp.entrySet());
        rankListAll.sort((o1, o2) -> ((Integer) o1.getValue().intValue()).compareTo(o2.getValue().intValue()) * (-1));
        return rankListAll.get(0).getKey();
    }

    private class VoteListener implements Listener {
        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            if (!game.getPlayers().contains(event.getPlayer().getUniqueId())) {
                return;
            }
            String vote = event.getMessage().substring(0, 1);
            BroadcastUtils.broadcast(
                    Constants.PREFIX +
                            event.getPlayer().getName() + "言可行“ " + vote + " ”令。",
                    game.getPlayers());
            result.put(event.getPlayer().getUniqueId(), vote);
        }
    }
}
