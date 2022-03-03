package net.pvpin.poetrygame.game.poetrywordle;

import net.md_5.bungee.api.ChatColor;
import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.events.poetrywordle.AsyncPWAnswerEvent;
import net.pvpin.poetrygame.api.events.poetrywordle.AsyncPWQuestionGenEvent;
import net.pvpin.poetrygame.api.events.poetrywordle.AsyncPWTimeoutEvent;
import net.pvpin.poetrygame.api.poetry.Poem;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.api.poetry.PoetryUtils;
import net.pvpin.poetrygame.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author William_Shi
 */
class PWTask {
    protected PWGame game;
    protected Map<UUID, AtomicInteger> attemptMap = new ConcurrentHashMap<>();
    protected long startStamp = System.currentTimeMillis();
    protected static final long MAX_DURATION = 60000L;

    protected PWTask(PWGame game) {
        this.game = game;
    }

    protected void run() {
        new PWCountDown(game, this)
                .runTaskTimerAsynchronously(Main.getPlugin(Main.class), 10L, 10L);
        Poem ranPoem = PoetryUtils.TANG_TOPS.get(ThreadLocalRandom.current().nextInt(PoetryUtils.TANG_TOPS.size()));
        game.currentAnswer = ranPoem.getCutParagraphs().get(ThreadLocalRandom.current().nextInt(ranPoem.getCutParagraphs().size()));
        AsyncPWQuestionGenEvent event = new AsyncPWQuestionGenEvent(game, game.currentAnswer);
        Bukkit.getPluginManager().callEvent(event);
        game.currentAnswer = event.getAnswer();
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
            BroadcastUtils.broadcast(
                    Constants.PREFIX +
                            Constants.convertChineseNumbers(game.currentAnswer.length()) +
                            "言詩一句。詩出《唐詩三百首》。請君臆斷。",
                    game.getPlayers());
        }, 3L);
        while ((System.currentTimeMillis() - startStamp <= MAX_DURATION) && game.getStatus() == 1) {
            CompletableFuture<Game.Session> future = new CompletableFuture<>();
            PWTask.PWListener listener = new PWTask.PWListener(game.getPlayers(), future);
            Bukkit.getPluginManager().registerEvents(listener, Main.getPlugin(Main.class));
            try {
                Game.Session result = future.get(MAX_DURATION - (System.currentTimeMillis() - startStamp), TimeUnit.MILLISECONDS);
                HandlerList.unregisterAll(listener);
                AsyncPWAnswerEvent customEvent = new AsyncPWAnswerEvent(game, game.currentAnswer, result.getContent(), Bukkit.getPlayer(result.getPlayer()));
                Bukkit.getPluginManager().callEvent(customEvent);
                String stripped = PoetryUtils.strip(result.getContent());
                game.record.add(result);

                if (stripped.equals(PoetryUtils.strip(game.currentAnswer))) {
                    result.setCorrect(true);
                    result.setPoem(PoetryUtils.searchFromAll(stripped));
                    BroadcastUtils.broadcast(
                            Constants.PREFIX +
                                    Bukkit.getOfflinePlayer(result.getPlayer()).getName() +
                                    " 猜出“" + result.getContent().replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "") + "”。",
                            game.getPlayers());
                    break;
                } else {
                    BroadcastUtils.broadcast(
                            Constants.PREFIX +
                                    Bukkit.getOfflinePlayer(result.getPlayer()).getName() +
                                    " 猜作“§r " + highlightWordle(result.getContent().replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "")) + "§r§6”。非是。",
                            game.getPlayers());
                }
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                HandlerList.unregisterAll(listener);
                AsyncPWTimeoutEvent customEvent = new AsyncPWTimeoutEvent(game);
                Bukkit.getPluginManager().callEvent(customEvent);
                BroadcastUtils.broadcast(
                        Constants.PREFIX + "答案：" + game.currentAnswer.replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "") + "。",
                        game.getPlayers());
                break;
            }
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
            game.end();
        }, 5L);
    }

    private String highlightWordle(String str) {
        if (str.length() != game.currentAnswer.length()) {
            return str;
        }
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < str.length(); index++) {
            char character = str.charAt(index);
            if (!game.currentAnswer.contains(String.valueOf(character))) {
                builder.append(ChatColor.RESET);
                builder.append(ChatColor.RED);
                builder.append(character);
                builder.append(" ");
                continue;
            }
            if (str.charAt(index) == game.currentAnswer.charAt(index)) {
                builder.append(ChatColor.RESET);
                builder.append(ChatColor.GREEN);
                builder.append(character);
            } else {
                builder.append(ChatColor.RESET);
                builder.append(ChatColor.YELLOW);
                builder.append(character);
            }
            builder.append(" ");
        }
        return builder.toString();
    }


    private class PWListener implements Listener {
        protected final List<UUID> players;
        protected final CompletableFuture<Game.Session> future;

        protected PWListener(List<UUID> players, CompletableFuture<Game.Session> future) {
            this.players = players;
            this.future = future;
        }

        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            if (!players.contains(event.getPlayer().getUniqueId())) {
                return;
            }
            if (!(event.getMessage().startsWith("猜詩") || event.getMessage().startsWith("猜诗"))) {
                return;
            }
            if (attemptMap.containsKey(event.getPlayer().getUniqueId())) {
                int times = attemptMap.get(event.getPlayer().getUniqueId()).incrementAndGet();
                if (times > ConfigManager.PoetryWordle.MAX_ATTEMPTS) {
                    BroadcastUtils.broadcast(
                            Constants.PREFIX + event.getPlayer().getName() + " 三猜不中的。",
                            game.getPlayers());
                    return;
                }
            } else {
                attemptMap.put(event.getPlayer().getUniqueId(), new AtomicInteger(1));
            }
            String poem = event.getMessage().substring(2);
            future.complete(game.new Session(event.getPlayer().getUniqueId(), System.currentTimeMillis(), poem));
        }
    }
}
