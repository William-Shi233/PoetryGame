package net.pvpin.poetrygame.game.flutteringblossoms;

import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.events.flutteringblossom.AsyncPlayerRespondEvent;
import net.pvpin.poetrygame.api.events.flutteringblossom.AsyncPlayerTimeoutEvent;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.api.utils.PoetryUtils;
import net.pvpin.poetrygame.api.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.nlpcn.commons.lang.jianfan.JianFan;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author William_Shi
 */
class FBTask {
    private FBGame game;
    private AtomicBoolean initNewRound = new AtomicBoolean(true);

    protected FBTask(FBGame game) {
        this.game = game;
    }

    protected void run() {
        if (game.currentGamers.size() <= 1) {
            game.end();
            return;
        }
        if (initNewRound.get()) {
            game.roundStamp = System.currentTimeMillis();
            BroadcastUtils.broadcast(
                    Constants.PREFIX + "依次第， " +
                            Bukkit.getOfflinePlayer(game.getCurrentPlayer()).getName() + " 行令。",
                    game.getPlayers());
            initNewRound.set(false);
        }
        CompletableFuture<String> future = new CompletableFuture<>();
        FBListener listener = new FBListener(game.getCurrentPlayer(), future);
        Bukkit.getPluginManager().registerEvents(listener, Main.getPlugin(Main.class));
        try {
            String poem = future.get(ConfigManager.FlutteringBlossoms.TIME_ROUND - (System.currentTimeMillis() - game.roundStamp), TimeUnit.MILLISECONDS);
            String stripped = PoetryUtils.strip(poem);
            Map<String, Object> result = PoetryUtils.searchFromPreset(stripped, game.keyWord);

            if (game.record.stream().map(JianFan::f2j)
                    .anyMatch(str -> str.contains(stripped))) {
                HandlerList.unregisterAll(listener);
                if (ConfigManager.FlutteringBlossoms.TIME_ROUND - (System.currentTimeMillis() - game.roundStamp) <= 0) {
                    kickCurrent();
                }
                BroadcastUtils.broadcast(
                        Constants.PREFIX +
                                Bukkit.getOfflinePlayer(game.getCurrentPlayer()).getName() +
                                " 行令“" + poem.replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "") + "”。同前人重。須另覓佳句。",
                        game.getPlayers());
                run();
                return;
            }
            if (result == null) {
                HandlerList.unregisterAll(listener);
                if (ConfigManager.FlutteringBlossoms.TIME_ROUND - (System.currentTimeMillis() - game.roundStamp) <= 0) {
                    kickCurrent();
                }
                BroadcastUtils.broadcast(
                        Constants.PREFIX +
                                Bukkit.getOfflinePlayer(game.getCurrentPlayer()).getName() +
                                " 行令“" + poem.replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "") + "”。不知所云者何。",
                        game.getPlayers());
            } else {
                initNewRound.set(true);
                BroadcastUtils.broadcast(
                        Constants.PREFIX +
                                Bukkit.getOfflinePlayer(game.getCurrentPlayer()).getName() +
                                " 行令“" + poem.replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "") + "”。" +
                                "句出" + result.get("author") + "《" + result.get("title") + "》。",
                        game.getPlayers());
                game.record.add("值" + System.currentTimeMillis() + ", " +
                        Bukkit.getOfflinePlayer(game.getCurrentPlayer()).getName() + " 行令" + poem);
                game.currentGamers.offerLast(game.currentGamers.pollFirst());
            }
            run();
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            AsyncPlayerTimeoutEvent event = new AsyncPlayerTimeoutEvent(game, Bukkit.getPlayer(game.getCurrentPlayer()));
            Bukkit.getPluginManager().callEvent(event);
            HandlerList.unregisterAll(listener);
            kickCurrent();
        }
    }

    private void kickCurrent() {
        initNewRound.set(true);
        UUID player = game.currentGamers.pollFirst();
        if (player == null) {
            return;
        }
        BroadcastUtils.broadcast(
                Constants.PREFIX + Bukkit.getOfflinePlayer(player).getName() + " 自罰三樽。"
                , game.getPlayers()
        );
        game.record.add("值" + System.currentTimeMillis() + ",  " +
                Bukkit.getOfflinePlayer(player).getName() + " 不能行令。");
        run();
    }

    private class FBListener implements Listener {
        protected final UUID player;
        protected final CompletableFuture<String> future;

        protected FBListener(UUID player, CompletableFuture<String> future) {
            this.player = player;
            this.future = future;
        }

        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            if (!event.getPlayer().getUniqueId().equals(player)) {
                return;
            }
            if (!(event.getMessage().startsWith("行令"))) {
                return;
            }
            String poem = event.getMessage().substring(2);
            AsyncPlayerRespondEvent customEvent = new AsyncPlayerRespondEvent(game, Bukkit.getPlayer(game.getCurrentPlayer()), poem);
            Bukkit.getPluginManager().callEvent(customEvent);
            future.complete(poem);
        }
    }
}