package net.pvpin.poetrygame.game.poetryfilling;

import net.md_5.bungee.api.chat.TextComponent;
import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.events.poetryfilling.AsyncPFAnswerEvent;
import net.pvpin.poetrygame.api.events.poetryfilling.AsyncPFQuestionGenEvent;
import net.pvpin.poetrygame.api.events.poetryfilling.AsyncPFTimeoutEvent;
import net.pvpin.poetrygame.api.poetry.Poem;
import net.pvpin.poetrygame.api.poetry.PoetryUtils;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author William_Shi
 */
public class PFTask {
    protected String currentQuestion;
    protected String currentAnswer;
    protected Poem currentAnswerPoem;
    public int rounds = ConfigManager.PoetryFilling.ROUND_NUMBER;

    protected Game game;
    protected Map<UUID, AtomicInteger> attemptMap = new ConcurrentHashMap<>();
    protected AtomicInteger currentRound = new AtomicInteger(0);
    protected long roundStamp;
    protected AtomicBoolean initNewRound = new AtomicBoolean(true);

    public PFTask(Game game) {
        this.game = game;
    }

    public void run() {
        var countDownTask = new PFCountDown(game, this)
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
                String answerFull = ranPoem.getCutParagraphs().get(ThreadLocalRandom.current().nextInt(ranPoem.getCutParagraphs().size()));
                int startChar = ThreadLocalRandom.current().nextInt(answerFull.length());
                int endChar = startChar + (startChar == answerFull.length() - 1 ? 1 : ThreadLocalRandom.current().nextInt(1) + 1);
                String undividedQuestion = new StringBuilder(answerFull)
                        .replace(startChar, endChar, "\u2587").toString();
                StringJoiner questionJoiner = new StringJoiner(" ");
                for (char c : undividedQuestion.toCharArray())
                    questionJoiner.add(String.valueOf(c));
                currentQuestion = questionJoiner.toString();
                currentAnswer = answerFull.substring(startChar, endChar);
                AsyncPFQuestionGenEvent event = new AsyncPFQuestionGenEvent(game, currentQuestion, currentAnswer);
                Bukkit.getPluginManager().callEvent(event);
                currentQuestion = event.getQuestion();
                currentAnswer = event.getAnswer();
                currentAnswerPoem = ranPoem;
                Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
                    BroadcastUtils.broadcast(
                            Constants.PREFIX + "塡空：" + currentQuestion,
                            game.getPlayers());
                }, 3L);
            }
            CompletableFuture<Game.Session> future = new CompletableFuture<>();
            PFTask.PFListener listener = new PFTask.PFListener(game.getPlayers(), future);
            Bukkit.getPluginManager().registerEvents(listener, Main.getPlugin(Main.class));
            try {
                Game.Session result = future.get(ConfigManager.PoetryFilling.TIME_ROUND - (System.currentTimeMillis() - roundStamp), TimeUnit.MILLISECONDS);
                HandlerList.unregisterAll(listener);
                AsyncPFAnswerEvent event = new AsyncPFAnswerEvent(game, currentQuestion, currentAnswer, result.getContent(), Bukkit.getPlayer(result.getPlayer()));
                Bukkit.getPluginManager().callEvent(event);
                String stripped = PoetryUtils.strip(result.getContent());
                game.getRecord().add(result);

                if (stripped.equals(PoetryUtils.strip(currentAnswer))) {
                    result.setCorrect(true);
                    result.setPoem(currentAnswerPoem);
                    this.initNewRound.set(true);
                    BroadcastUtils.broadcast(Constants.PREFIX +
                                    Bukkit.getOfflinePlayer(result.getPlayer()).getName() +
                                    " 辨出“" + result.getContent() + "”。",
                            game.getPlayers());
                    continue;
                }
                BroadcastUtils.broadcast(Constants.PREFIX +
                                Bukkit.getOfflinePlayer(result.getPlayer()).getName() +
                                " 認定“" + result.getContent() + "”。非是。",
                        game.getPlayers());
                this.initNewRound.set(ConfigManager.PoetryFilling.TIME_ROUND - (System.currentTimeMillis() - roundStamp) <= 0);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                AsyncPFTimeoutEvent event = new AsyncPFTimeoutEvent(game, currentQuestion, currentAnswer);
                Bukkit.getPluginManager().callEvent(event);
                HandlerList.unregisterAll(listener);
                this.initNewRound.set(true);
            }
        } while ((currentRound.get() <= rounds) && game.getStatus() == 1);
        countDownTask.cancel();
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
            game.end();
        }, 5L);
    }

    private class PFListener implements Listener {
        protected final List<UUID> players;
        protected final CompletableFuture<Game.Session> future;

        protected PFListener(List<UUID> players, CompletableFuture<Game.Session> future) {
            this.players = players;
            this.future = future;
        }

        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            if (!players.contains(event.getPlayer().getUniqueId())) {
                return;
            }
            if (!(event.getMessage().startsWith("塡空") || event.getMessage().startsWith("填空"))) {
                return;
            }
            if (attemptMap.containsKey(event.getPlayer().getUniqueId())) {
                int times = attemptMap.get(event.getPlayer().getUniqueId()).incrementAndGet();
                if (times > ConfigManager.PoetryFilling.MAX_ATTEMPTS) {
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
