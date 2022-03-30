package net.pvpin.poetrygame.game.poetrywordle;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author William_Shi
 */
public class PWTask {
    protected String currentAnswer;
    protected Poem currentAnswerPoem;
    public int rounds = ConfigManager.PoetryWordle.ROUND_NUMBER;

    protected Game game;
    protected Map<UUID, AtomicInteger> attemptMap = new ConcurrentHashMap<>();
    protected AtomicInteger currentRound = new AtomicInteger(0);
    protected long roundStamp;
    protected AtomicBoolean initNewRound = new AtomicBoolean(true);

    public PWTask(Game game) {
        this.game = game;
    }

    public void run() {
        var countDownTask = new PWCountDown(game, this)
                .runTaskTimerAsynchronously(Main.getPlugin(Main.class), 10L, 10L);
        do {
            if (initNewRound.get()) {
                if (currentAnswer != null) {
                    TextComponent component = new TextComponent(Constants.PREFIX);
                    component.addExtra(new TextComponent("答案："));
                    component.addExtra(BroadcastUtils.generatePoemComponent(currentAnswer, currentAnswerPoem));
                    component.addExtra(new TextComponent("。"));
                    BroadcastUtils.broadcast(component, game.getPlayers());
                    if (currentRound.get() == rounds) {
                        break;
                    }
                }
                currentRound.incrementAndGet();
                roundStamp = System.currentTimeMillis();
                attemptMap.clear();
                initNewRound.set(false);
                Poem ranPoem = PoetryUtils.TANG_TOPS.get(ThreadLocalRandom.current().nextInt(PoetryUtils.TANG_TOPS.size()));
                currentAnswer = ranPoem.getCutParagraphs().get(ThreadLocalRandom.current().nextInt(ranPoem.getCutParagraphs().size()));
                AsyncPWQuestionGenEvent event = new AsyncPWQuestionGenEvent(game, currentAnswer);
                Bukkit.getPluginManager().callEvent(event);
                currentAnswer = event.getAnswer();
                currentAnswerPoem = ranPoem;
                Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
                    BroadcastUtils.broadcast(
                            Constants.PREFIX +
                                    Constants.convertChineseNumbers(currentAnswer.length()) +
                                    "言詩一句。詩出《唐詩三百首》。請君臆斷。",
                            game.getPlayers());
                }, 3L);
            }
            CompletableFuture<Game.Session> future = new CompletableFuture<>();
            PWTask.PWListener listener = new PWTask.PWListener(game.getPlayers(), future);
            Bukkit.getPluginManager().registerEvents(listener, Main.getPlugin(Main.class));
            try {
                Game.Session result = future.get(ConfigManager.PoetryWordle.TIME_ROUND - (System.currentTimeMillis() - roundStamp), TimeUnit.MILLISECONDS);
                HandlerList.unregisterAll(listener);
                AsyncPWAnswerEvent customEvent = new AsyncPWAnswerEvent(game, currentAnswer, result.getContent(), Bukkit.getPlayer(result.getPlayer()));
                Bukkit.getPluginManager().callEvent(customEvent);
                String stripped = PoetryUtils.strip(result.getContent());
                game.getRecord().add(result);

                if (stripped.equals(PoetryUtils.strip(currentAnswer))) {
                    result.setCorrect(true);
                    result.setPoem(PoetryUtils.searchFromAll(stripped));
                    this.initNewRound.set(true);
                    BroadcastUtils.broadcast(
                            Constants.PREFIX +
                                    Bukkit.getOfflinePlayer(result.getPlayer()).getName() +
                                    " 猜出“" + currentAnswer + "”。",
                            game.getPlayers());
                    continue;
                }
                BroadcastUtils.broadcast(
                        Constants.PREFIX +
                                Bukkit.getOfflinePlayer(result.getPlayer()).getName() +
                                " 猜作“§r " + highlightWordle(result.getContent()) + "§r§6”。非是。",
                        game.getPlayers());
                this.initNewRound.set(ConfigManager.PoetryWordle.TIME_ROUND - (System.currentTimeMillis() - roundStamp) <= 0);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                HandlerList.unregisterAll(listener);
                AsyncPWTimeoutEvent customEvent = new AsyncPWTimeoutEvent(game);
                Bukkit.getPluginManager().callEvent(customEvent);
                this.initNewRound.set(true);
            }
        } while ((currentRound.get() <= rounds) && game.getStatus() == 1);
        countDownTask.cancel();
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
            game.end();
        }, 5L);
    }

    private String highlightWordle(String str) {
        String strippedAnswerPlayer = PoetryUtils.strip(str);
        String strippedAnswerCorrect = PoetryUtils.strip(currentAnswer);
        if (strippedAnswerPlayer.length() != strippedAnswerCorrect.length()) {
            return str;
        }
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < strippedAnswerPlayer.length(); index++) {
            char character = strippedAnswerPlayer.charAt(index);
            if (!strippedAnswerCorrect.contains(String.valueOf(character))) {
                builder.append(ChatColor.RESET);
                builder.append(ChatColor.RED);
                builder.append(character);
                builder.append(" ");
                continue;
            }
            if (strippedAnswerPlayer.charAt(index) == strippedAnswerCorrect.charAt(index)) {
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
