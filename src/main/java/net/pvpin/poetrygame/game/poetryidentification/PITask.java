package net.pvpin.poetrygame.game.poetryidentification;

import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.events.poetryidentification.AsyncPlayerAnswerEvent;
import net.pvpin.poetrygame.api.events.poetryidentification.AsyncRoundTimeoutEvent;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.api.utils.PoetryUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author William_Shi
 */
public class PITask {
    private PIGame game;
    private AtomicBoolean initNewRound = new AtomicBoolean(true);

    protected PITask(PIGame game) {
        this.game = game;
    }

    protected void run() {
        if (game.currentRound.get() >= ConfigManager.PoetryIdentification.ROUND_NUMBER) {
            BroadcastUtils.broadcast(
                    Constants.PREFIX + "答案：" + game.currentAnswer + "。",
                    game.getPlayers());
            Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
                game.end();
            }, 5L);
            return;
        }
        if (initNewRound.get()) {
            if (game.currentAnswer != null) {
                BroadcastUtils.broadcast(
                        Constants.PREFIX + "答案：" + game.currentAnswer + "。",
                        game.getPlayers());
            }
            game.currentRound.incrementAndGet();
            game.roundStamp = System.currentTimeMillis();
            generateQuestion();
            initNewRound.set(false);
            Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
                BroadcastUtils.broadcast(
                        Constants.PREFIX +
                                "遴選" + (game.currentAnswer.length() == 5 ? "五" : "七") +
                                "字排字成詩，詩皆出《唐詩三百首》。\n\n" +
                                "        " + game.currentQuestion + "\n",
                        game.getPlayers());
            }, 3L);
        }
        CompletableFuture<List<Object>> future = new CompletableFuture<>();
        PIListener listener = new PIListener(game.getPlayers(), future);
        Bukkit.getPluginManager().registerEvents(listener, Main.getPlugin(Main.class));
        try {
            List<Object> result = future.get(ConfigManager.PoetryIdentification.TIME_ROUND - (System.currentTimeMillis() - game.roundStamp), TimeUnit.MILLISECONDS);
            String poem = (String) result.get(0);
            UUID answerer = (UUID) result.get(1);
            AsyncPlayerAnswerEvent event = new AsyncPlayerAnswerEvent(game, game.currentQuestion, game.currentAnswer, poem, Bukkit.getPlayer(answerer));
            Bukkit.getPluginManager().callEvent(event);
            String stripped = PoetryUtils.strip(poem);
            if (stripped.equals(PoetryUtils.strip(game.currentAnswer))) {
                this.initNewRound.set(true);
                BroadcastUtils.broadcast(
                        Constants.PREFIX +
                                Bukkit.getOfflinePlayer(answerer).getName() +
                                " 辨出“" + poem.replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "") + "”。",
                        game.getPlayers());
                game.record.add(List.of(Bukkit.getOfflinePlayer(answerer).getName(), "值" + System.currentTimeMillis() + ", " +
                        Bukkit.getOfflinePlayer(answerer).getName() + " 辨識" + poem));
                HandlerList.unregisterAll(listener);
                run();
                return;
            }
            if (ConfigManager.PoetryIdentification.TIME_ROUND - (System.currentTimeMillis() - game.roundStamp) <= 0) {
                this.initNewRound.set(true);
            }
            BroadcastUtils.broadcast(
                    Constants.PREFIX +
                            Bukkit.getOfflinePlayer(answerer).getName() +
                            " 認定“" + poem.replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "") + "”。非是。",
                    game.getPlayers());
            HandlerList.unregisterAll(listener);
            run();
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            AsyncRoundTimeoutEvent event = new AsyncRoundTimeoutEvent(game, game.currentQuestion, game.currentAnswer);
            Bukkit.getPluginManager().callEvent(event);
            this.initNewRound.set(true);
            HandlerList.unregisterAll(listener);
            run();
        }
    }

    private void generateQuestion() {
        if (game.currentRound.intValue() <= 3) {
            List<Map.Entry<String, Map<String, Object>>> list = Main.TANG_TOPS.entrySet().stream().collect(Collectors.toList());
            int index = ThreadLocalRandom.current().nextInt(list.size());
            Map.Entry<String, Map<String, Object>> entry = list.get(index);
            String right = entry.getKey();
            if (right.length() != 5) {
                generateQuestion();
                return;
            }
            index = ThreadLocalRandom.current().nextInt(list.size());
            entry = list.get(index);
            String obstacleOne;
            String obstacleTwo;
            try {
                obstacleOne = ((List<String>) entry.getValue().get("paragraphs")).get(0).substring(2, 3);
                obstacleTwo = ((List<String>) entry.getValue().get("paragraphs")).get(1).substring(2, 3);
                if (right.contains(obstacleOne) || right.contains(obstacleTwo) || obstacleOne.equals(obstacleTwo)
                        || obstacleOne.isBlank() || obstacleTwo.isBlank() || obstacleOne.isEmpty() || obstacleTwo.isEmpty()) {
                    generateQuestion();
                    return;
                }
            } catch (IndexOutOfBoundsException ex) {
                generateQuestion();
                return;
            }
            List<String> all = new ArrayList<>(16);
            for (char c : right.toCharArray()) {
                all.add(String.valueOf(c));
            }
            all.addAll(List.of(obstacleOne, obstacleTwo));
            Collections.shuffle(all);
            StringBuilder builder = new StringBuilder();
            for (String s : all) {
                builder.append(s);
                builder.append("， ");
            }
            game.currentAnswer = right;
            game.currentQuestion = builder.toString();
        } else {
            List<Map.Entry<String, Map<String, Object>>> list = Main.TANG_TOPS.entrySet().stream().collect(Collectors.toList());
            int index = ThreadLocalRandom.current().nextInt(list.size());
            Map.Entry<String, Map<String, Object>> entry = list.get(index);
            String right = entry.getKey();
            if (right.length() != 7) {
                generateQuestion();
                return;
            }
            index = ThreadLocalRandom.current().nextInt(list.size());
            entry = list.get(index);
            String obstacleOne;
            String obstacleTwo;
            String obstacleThree;
            String obstacleFour;
            String obstacleFive;
            try {
                obstacleOne = ((List<String>) entry.getValue().get("paragraphs")).get(0).substring(0, 1);
                obstacleTwo = ((List<String>) entry.getValue().get("paragraphs")).get(0).substring(1, 2);
                obstacleThree = ((List<String>) entry.getValue().get("paragraphs")).get(0).substring(4, 5);
                obstacleFour = ((List<String>) entry.getValue().get("paragraphs")).get(1).substring(4, 5);
                obstacleFive = ((List<String>) entry.getValue().get("paragraphs")).get(1).substring(5, 6);
                List<String> obstacles = List.of(obstacleOne, obstacleTwo, obstacleThree, obstacleFour, obstacleFive);
                for (String obstacle : obstacles) {
                    int same = 0;
                    for (String str : obstacles) {
                        if (str.equals(obstacle)) {
                            same += 1;
                        }
                    }
                    if (same > 1) {
                        generateQuestion();
                        return;
                    }
                    if (right.contains(obstacle)) {
                        generateQuestion();
                        return;
                    }
                    if (obstacle.isBlank() || obstacle.isEmpty()) {
                        generateQuestion();
                        return;
                    }
                }
            } catch (IndexOutOfBoundsException ex) {
                generateQuestion();
                return;
            }
            List<String> all = new ArrayList<>(16);
            for (char c : right.toCharArray()) {
                all.add(String.valueOf(c));
            }
            all.addAll(List.of(obstacleOne, obstacleTwo, obstacleThree, obstacleFour, obstacleFive));
            Collections.shuffle(all);
            StringBuilder builder = new StringBuilder();
            for (String s : all) {
                builder.append(s);
                builder.append("， ");
            }
            game.currentAnswer = right;
            game.currentQuestion = builder.toString();
        }
    }

    private class PIListener implements Listener {
        protected final List<UUID> players;
        protected final CompletableFuture<List<Object>> future;

        protected PIListener(List<UUID> players, CompletableFuture<List<Object>> future) {
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
            String poem = event.getMessage().substring(2);
            future.complete(List.of(poem, event.getPlayer().getUniqueId()));
        }
    }
}
