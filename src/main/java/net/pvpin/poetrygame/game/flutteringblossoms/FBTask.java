package net.pvpin.poetrygame.game.flutteringblossoms;

import net.md_5.bungee.api.chat.TextComponent;
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
    protected final ConcurrentLinkedDeque<UUID> currentGamers = new ConcurrentLinkedDeque<>();
    public String keyWord;

    protected Game game;
    protected long roundStamp;
    protected AtomicBoolean initNewRound = new AtomicBoolean(true);

    protected FBTask(Game game) {
        this.game = game;
    }

    protected void run() {
        try {
            BroadcastUtils.broadcast(
                    Constants.PREFIX + "諸君各言行令之字。",
                    game.getPlayers());
            CompletableFuture<String> future = new CompletableFuture<>();
            FBVote vote = new FBVote(game, future);
            vote.schedule();
            this.keyWord = future.get();
            PoetryUtils.PresetManager.loadPreset(keyWord);
            BroadcastUtils.broadcast(
                    Constants.PREFIX + "行令字" + keyWord + "字。",
                    game.getPlayers());
            new FBCountDown(game, this)
                    .runTaskTimerAsynchronously(Main.getPlugin(Main.class), 10L, 10L);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        while (game.getStatus() == 1) {
            if (currentGamers.size() <= 1) {
                game.end();
                return;
            }
            if (initNewRound.get()) {
                roundStamp = System.currentTimeMillis();
                BroadcastUtils.broadcast(Constants.PREFIX + "依次第， " +
                                Bukkit.getOfflinePlayer(currentGamers.peekFirst()).getName() + " 行令。",
                        game.getPlayers());
                initNewRound.set(false);
            }
            CompletableFuture<Game.Session> future = new CompletableFuture<>();
            FBListener listener = new FBListener(currentGamers.peekFirst(), future);
            Bukkit.getPluginManager().registerEvents(listener, Main.getPlugin(Main.class));
            try {
                Game.Session session = future.get(ConfigManager.FlutteringBlossoms.TIME_ROUND - (System.currentTimeMillis() - roundStamp), TimeUnit.MILLISECONDS);
                HandlerList.unregisterAll(listener);
                String stripped = PoetryUtils.strip(session.getContent());
                Poem result = PoetryUtils.searchFromPreset(stripped, keyWord);
                session.setPoem(result);
                boolean contains = contains(game.getRecord(), session);
                game.getRecord().add(session);

                if (contains) {
                    session.setCorrect(false);
                    if (ConfigManager.FlutteringBlossoms.TIME_ROUND - (System.currentTimeMillis() - roundStamp) <= 0) {
                        kickCurrent();
                        continue;
                    }
                    AsyncFBAnswerEvent customEvent = new AsyncFBAnswerEvent(game, Bukkit.getPlayer(currentGamers.peekFirst()), session.getContent(), AsyncFBAnswerEvent.Result.REDUNDANT_POEM);
                    Bukkit.getPluginManager().callEvent(customEvent);
                    BroadcastUtils.broadcast(Constants.PREFIX +
                                    Bukkit.getOfflinePlayer(currentGamers.peekFirst()).getName() +
                                    " 行令“" + session.getContent() + "”。同前人重。須另覓佳句。",
                            game.getPlayers());
                    continue;
                }
                if (result == null || (!stripped.contains(PoetryUtils.strip(keyWord)))) {
                    session.setCorrect(false);
                    if (ConfigManager.FlutteringBlossoms.TIME_ROUND - (System.currentTimeMillis() - roundStamp) <= 0) {
                        kickCurrent();
                        continue;
                    }
                    AsyncFBAnswerEvent customEvent = new AsyncFBAnswerEvent(game, Bukkit.getPlayer(currentGamers.peekFirst()), session.getContent(), AsyncFBAnswerEvent.Result.UNKNOWN_POEM);
                    Bukkit.getPluginManager().callEvent(customEvent);
                    BroadcastUtils.broadcast(Constants.PREFIX +
                                    Bukkit.getOfflinePlayer(currentGamers.peekFirst()).getName() +
                                    " 行令“" + session.getContent() + "”。不知所云者何。",
                            game.getPlayers());
                } else {
                    session.setCorrect(true);
                    AsyncFBAnswerEvent customEvent = new AsyncFBAnswerEvent(game, Bukkit.getPlayer(currentGamers.peekFirst()), session.getContent(), AsyncFBAnswerEvent.Result.SUCCESS);
                    Bukkit.getPluginManager().callEvent(customEvent);
                    initNewRound.set(true);
                    TextComponent component = new TextComponent(Constants.PREFIX);
                    component.addExtra(new TextComponent(Bukkit.getOfflinePlayer(currentGamers.peekFirst()).getName()));
                    component.addExtra(new TextComponent(" 行令“"));
                    component.addExtra(new TextComponent(session.getContent()));
                    component.addExtra(new TextComponent("”。句出《"));
                    component.addExtra(BroadcastUtils.generatePoemComponent(result.getTitle(), result));
                    component.addExtra(new TextComponent("》。"));
                    BroadcastUtils.broadcast(component, game.getPlayers());
                    currentGamers.offerLast(currentGamers.pollFirst());
                }
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                HandlerList.unregisterAll(listener);
                kickCurrent();
            }
        }
    }

    private void kickCurrent() {
        initNewRound.set(true);
        UUID player = currentGamers.pollFirst();
        if (player == null) {
            return;
        }
        AsyncFBTimeoutEvent event = new AsyncFBTimeoutEvent(game, Bukkit.getPlayer(currentGamers.peekFirst()));
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
                        if (para.contains(keyWord) && PoetryUtils.strip(s.getContent()).contains(PoetryUtils.strip(para)))
                            content.add(PoetryUtils.strip(para));
                    });
                });
        List<String> contentPoem = new ArrayList<>(16);
        poem.getPoem().getCutParagraphs().forEach(para -> {
            if (para.contains(keyWord) && PoetryUtils.strip(poem.getContent()).contains(PoetryUtils.strip(para)))
                contentPoem.add(PoetryUtils.strip(para));
        });
        AtomicBoolean bool = new AtomicBoolean(true);
        contentPoem.forEach(str -> {
            if (!content.contains(str)) {
                bool.set(false);
            }
        });
        // return contentPoem.stream().anyMatch(content::contains);
        // Need false when at least one para is not mentioned previously.
        // Some paras' having been mentioned can be accepted.
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