package net.pvpin.poetrygame.game.flutteringblossoms;

import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.events.flutteringblossom.AsyncFBAnswerEvent;
import net.pvpin.poetrygame.api.events.flutteringblossom.AsyncFBTimeoutEvent;
import net.pvpin.poetrygame.api.poetry.Poem;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.api.poetry.PoetryUtils;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author William_Shi
 */
class FBTask {
    protected FBGame game;
    protected long roundStamp;
    protected AtomicBoolean initNewRound = new AtomicBoolean(true);

    protected FBTask(FBGame game) {
        this.game = game;
    }

    protected void run() {
        new FBCountDown(game, this)
                .runTaskTimerAsynchronously(Main.getPlugin(Main.class), 10L, 10L);
        while (game.getStatus() == 1) {
            if (game.currentGamers.size() <= 1) {
                game.end();
                return;
            }
            if (initNewRound.get()) {
                roundStamp = System.currentTimeMillis();
                BroadcastUtils.broadcast(
                        Constants.PREFIX + "依次第， " +
                                Bukkit.getOfflinePlayer(game.getCurrentPlayer()).getName() + " 行令。",
                        game.getPlayers());
                initNewRound.set(false);
            }
            CompletableFuture<Game.Session> future = new CompletableFuture<>();
            FBListener listener = new FBListener(game.getCurrentPlayer(), future);
            Bukkit.getPluginManager().registerEvents(listener, Main.getPlugin(Main.class));
            try {
                Game.Session session = future.get(ConfigManager.FlutteringBlossoms.TIME_ROUND - (System.currentTimeMillis() - roundStamp), TimeUnit.MILLISECONDS);
                HandlerList.unregisterAll(listener);
                String stripped = PoetryUtils.strip(session.getContent());
                Poem result = PoetryUtils.searchFromPreset(stripped, game.keyWord);
                session.setPoem(result);
                boolean contains = contains(game.record, session);
                game.record.add(session);

                if (contains) {
                    if (ConfigManager.FlutteringBlossoms.TIME_ROUND - (System.currentTimeMillis() - roundStamp) <= 0) {
                        kickCurrent();
                        continue;
                    }
                    AsyncFBAnswerEvent customEvent = new AsyncFBAnswerEvent(game, Bukkit.getPlayer(game.getCurrentPlayer()), session.getContent(), AsyncFBAnswerEvent.Result.REDUNDANT_POEM);
                    Bukkit.getPluginManager().callEvent(customEvent);
                    BroadcastUtils.broadcast(
                            Constants.PREFIX +
                                    Bukkit.getOfflinePlayer(game.getCurrentPlayer()).getName() +
                                    " 行令“" + session.getContent() + "”。同前人重。須另覓佳句。",
                            game.getPlayers());
                    continue;
                }
                if (result == null || (!stripped.contains(PoetryUtils.strip(game.keyWord)))) {
                    if (ConfigManager.FlutteringBlossoms.TIME_ROUND - (System.currentTimeMillis() - roundStamp) <= 0) {
                        kickCurrent();
                        continue;
                    }
                    AsyncFBAnswerEvent customEvent = new AsyncFBAnswerEvent(game, Bukkit.getPlayer(game.getCurrentPlayer()), session.getContent(), AsyncFBAnswerEvent.Result.UNKNOWN_POEM);
                    Bukkit.getPluginManager().callEvent(customEvent);
                    BroadcastUtils.broadcast(
                            Constants.PREFIX +
                                    Bukkit.getOfflinePlayer(game.getCurrentPlayer()).getName() +
                                    " 行令“" + session.getContent() + "”。不知所云者何。",
                            game.getPlayers());
                } else {
                    AsyncFBAnswerEvent customEvent = new AsyncFBAnswerEvent(game, Bukkit.getPlayer(game.getCurrentPlayer()), session.getContent(), AsyncFBAnswerEvent.Result.SUCCESS);
                    Bukkit.getPluginManager().callEvent(customEvent);
                    initNewRound.set(true);
                    BroadcastUtils.broadcast(
                            Constants.PREFIX +
                                    Bukkit.getOfflinePlayer(game.getCurrentPlayer()).getName() +
                                    " 行令“" + session.getContent() + "”。" +
                                    "句出" + result.getAuthor() + "《" + result.getTitle() + "》。",
                            game.getPlayers());
                    game.currentGamers.offerLast(game.currentGamers.pollFirst());
                }
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                HandlerList.unregisterAll(listener);
                kickCurrent();
            }
        }
    }

    private void kickCurrent() {
        initNewRound.set(true);
        UUID player = game.currentGamers.pollFirst();
        if (player == null) {
            return;
        }
        AsyncFBTimeoutEvent event = new AsyncFBTimeoutEvent(game, Bukkit.getPlayer(game.getCurrentPlayer()));
        Bukkit.getPluginManager().callEvent(event);
        BroadcastUtils.broadcast(
                Constants.PREFIX + Bukkit.getOfflinePlayer(player).getName() + " 自罰三樽。"
                , game.getPlayers()
        );
    }

    private boolean contains(List<Game.Session> contents, Game.Session poem) {
        if (poem.getPoem() == null) {
            return false;
        }
        List<String> content = new ArrayList<>(16);
        contents.stream()
                .filter(s -> s.getPoem() != null)
                .forEach(s -> {
                    var poetry = s.getPoem();
                    poetry.getCutParagraphs().forEach(para -> {
                        if (para.contains(game.keyWord) && PoetryUtils.strip(s.getContent()).contains(PoetryUtils.strip(para)))
                            content.add(PoetryUtils.strip(para));
                    });
                });
        List<String> contentPoem = new ArrayList<>(16);
        poem.getPoem().getCutParagraphs().forEach(para -> {
            if (para.contains(game.keyWord) && PoetryUtils.strip(poem.getContent()).contains(PoetryUtils.strip(para)))
                contentPoem.add(PoetryUtils.strip(para));
        });
        AtomicBoolean bool = new AtomicBoolean(true);
        contentPoem.forEach(str -> {
            if (!content.contains(str)) {
                bool.set(false);
            }
        });
        // return contentPoem.stream().anyMatch(content::contains);
        return bool.get();
    }

    private class FBListener implements Listener {
        protected final UUID player;
        protected final CompletableFuture<Game.Session> future;

        protected FBListener(UUID player, CompletableFuture<Game.Session> future) {
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
            future.complete(game.new Session(event.getPlayer().getUniqueId(), System.currentTimeMillis(), poem));
        }
    }
}