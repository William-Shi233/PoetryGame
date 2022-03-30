package net.pvpin.poetrygame.game.poetryidentification;

import net.md_5.bungee.api.chat.TextComponent;
import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.events.poetryidentification.AsyncPIAnswerEvent;
import net.pvpin.poetrygame.api.events.poetryidentification.AsyncPIQuestionGenEvent;
import net.pvpin.poetrygame.api.events.poetryidentification.AsyncPITimeoutEvent;
import net.pvpin.poetrygame.api.poetry.Poem;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.api.poetry.PoetryUtils;
import net.pvpin.poetrygame.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.nlpcn.commons.lang.util.tuples.Pair;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author William_Shi
 */
public class PITask {
    protected String currentQuestion;
    protected String currentAnswer;
    protected Poem currentAnswerPoem;
    public int rounds = ConfigManager.PoetryIdentification.ROUND_NUMBER;

    protected Game game;
    protected Map<UUID, AtomicInteger> attemptMap = new ConcurrentHashMap<>();
    protected AtomicInteger currentRound = new AtomicInteger(0);
    protected long roundStamp;
    protected AtomicBoolean initNewRound = new AtomicBoolean(true);

    public PITask(Game game) {
        this.game = game;
    }

    public void run() {
        var countDownTask = new PICountDown(game, this)
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
                int questionLength = currentRound.get() <= 3 ? 5 : 7;
                Pair<String, String> pair = generateQuestion(questionLength);
                currentAnswer = pair.getValue0();
                currentQuestion = pair.getValue1();
                currentAnswerPoem = PoetryUtils.searchFromAll(currentAnswer);
                initNewRound.set(false);
                Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
                    BroadcastUtils.broadcast(Constants.PREFIX +
                                    "遴選" + Constants.convertChineseNumbers(currentAnswer.length()) +
                                    "字排字成詩，詩皆出《唐詩三百首》。\n\n" +
                                    BroadcastUtils.SPACE.repeat(4) + ChatColor.GOLD + currentQuestion + "\n",
                            game.getPlayers());
                }, 3L);
            }
            CompletableFuture<Game.Session> future = new CompletableFuture<>();
            PIListener listener = new PIListener(game.getPlayers(), future);
            Bukkit.getPluginManager().registerEvents(listener, Main.getPlugin(Main.class));
            try {
                Game.Session result = future.get(ConfigManager.PoetryIdentification.TIME_ROUND - (System.currentTimeMillis() - roundStamp), TimeUnit.MILLISECONDS);
                HandlerList.unregisterAll(listener);
                AsyncPIAnswerEvent event = new AsyncPIAnswerEvent(game, currentQuestion, currentAnswer, result.getContent(), Bukkit.getPlayer(result.getPlayer()));
                Bukkit.getPluginManager().callEvent(event);
                String stripped = PoetryUtils.strip(result.getContent());
                game.getRecord().add(result);

                if (stripped.equals(PoetryUtils.strip(currentAnswer))) {
                    result.setCorrect(true);
                    result.setPoem(PoetryUtils.searchFromAll(stripped));
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
                this.initNewRound.set(ConfigManager.PoetryIdentification.TIME_ROUND - (System.currentTimeMillis() - roundStamp) <= 0);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                AsyncPITimeoutEvent event = new AsyncPITimeoutEvent(game, currentQuestion, currentAnswer);
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

    protected Pair<String, String> generateQuestion(int length) {
        Pair<String, String> result = new Pair<>("", "");
        List<String> list = new ArrayList<>(1024);
        PoetryUtils.TANG_TOPS.stream().forEach(poem -> {
            List<String> paras = poem.getCutParagraphs();
            paras.stream()
                    .map(PoetryUtils::strip)
                    .filter(str -> str.length() == length)
                    .forEach(list::add);
        });
        int index = ThreadLocalRandom.current().nextInt(list.size());
        String answer = list.get(index);
        int distractorAmount = length == 7 ? 5 : 2;

        List<String> distractors = new ArrayList<>(16);
        while (distractors.size() < distractorAmount) {
            Poem poem = PoetryUtils.TANG_TOPS.get(
                    ThreadLocalRandom.current().nextInt(PoetryUtils.TANG_TOPS.size())
            );
            List<String> paras = poem.getParagraphs();
            String pair = paras.get(ThreadLocalRandom.current().nextInt(paras.size()))
                    .replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "");
            String distractor = String.valueOf(pair.charAt(ThreadLocalRandom.current().nextInt(pair.length())));
            if (!(answer.contains(distractor) || distractors.contains(distractor))) {
                distractors.add(distractor);
            }
        }
        for (char c : answer.toCharArray()) {
            distractors.add(String.valueOf(c));
        }
        Collections.shuffle(distractors);

        AsyncPIQuestionGenEvent event = new AsyncPIQuestionGenEvent(game, new ArrayList<>(distractors), answer);
        Bukkit.getPluginManager().callEvent(event);
        StringJoiner joiner = new StringJoiner("， ");
        event.getQuestion().forEach(joiner::add);
        result = result.setAt0(answer);
        result = result.setAt1(joiner.toString());
        return result;
    }

    private class PIListener implements Listener {
        protected final List<UUID> players;
        protected final CompletableFuture<Game.Session> future;

        protected PIListener(List<UUID> players, CompletableFuture<Game.Session> future) {
            this.players = players;
            this.future = future;
        }

        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            if (!players.contains(event.getPlayer().getUniqueId())) {
                return;
            }
            if (!(event.getMessage().startsWith("辨詩") || event.getMessage().startsWith("辨诗"))) {
                return;
            }
            if (attemptMap.containsKey(event.getPlayer().getUniqueId())) {
                int times = attemptMap.get(event.getPlayer().getUniqueId()).incrementAndGet();
                if (times > ConfigManager.PoetryIdentification.MAX_ATTEMPTS) {
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
