package net.pvpin.poetrygame.game;

import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.events.common.AsyncGameEndEvent;
import net.pvpin.poetrygame.api.events.common.AsyncGameStartEvent;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.game.flutteringblossoms.FBGame;
import net.pvpin.poetrygame.game.mix.MixGame;
import net.pvpin.poetrygame.game.poetryfilling.PFGame;
import net.pvpin.poetrygame.game.poetryidentification.PIGame;
import net.pvpin.poetrygame.game.poetrywordle.PWGame;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author William_Shi
 */
public final class GameManager {
    protected static final Map<GameType, List<Game>> GAMES = new ConcurrentHashMap<>(16);

    static {
        Arrays.stream(GameType.values()).forEach(type -> {
            if (!GAMES.containsKey(type)) {
                GAMES.put(type, new CopyOnWriteArrayList<>());
            }
        });
        new StartTask().runTaskTimerAsynchronously(Main.getPlugin(Main.class), 0L, 20L);
        new EndTask().runTaskTimerAsynchronously(Main.getPlugin(Main.class), 0L, 20L);
    }

    public static Game getGame(UUID player, GameType type) {
        List<Game> gamesList = GAMES.get(type);
        if (gamesList.stream().anyMatch(game -> game.players.contains(player))) {
            for (Game game : gamesList) {
                if (game.players.contains(player)) {
                    return game;
                }
            }
        }
        return null;
    }

    public static boolean join(UUID player, GameType type, String... exact) {
        List<Game> gamesList = GAMES.get(type);
        if (gamesList.stream().anyMatch(game -> game.players.contains(player))) {
            return false;
        }
        if (type != GameType.MIX) {
            List<Game> available = gamesList.stream()
                    .filter(game -> game.status.intValue() == 0)
                    .filter(game -> game.players.size() < game.getMaxPlayers())
                    .collect(Collectors.toList());
            if (!available.isEmpty()) {
                available.get(0).addOrRemovePlayer(player);
                return true;
            }
            Game newGame = createGame(type);
            newGame.addOrRemovePlayer(player);
            return true;
        } else {
            List<Game> available = gamesList.stream()
                    .map(game -> (MixGame) game)
                    .filter(game -> Objects.equals(game.getType(), exact[0]))
                    .filter(game -> game.status.intValue() == 0)
                    .filter(game -> game.players.size() < game.getMaxPlayers())
                    .collect(Collectors.toList());
            if (!available.isEmpty()) {
                available.get(0).addOrRemovePlayer(player);
                return true;
            }
            Game newGame = new MixGame(exact[0]);
            GAMES.get(type).add(newGame);
            newGame.addOrRemovePlayer(player);
            return true;
        }
    }

    public static boolean quit(UUID player, GameType type) {
        List<Game> gamesList = GAMES.get(type);
        if (!gamesList.stream().anyMatch(game -> game.players.contains(player))) {
            return false;
        }
        Game current = getGame(player, type);
        if (current.status.intValue() == 0) {
            current.addOrRemovePlayer(player);
            return true;
        } else if (current.status.intValue() == 1 && current.players.size() == 1) {
            current.end();
            AsyncGameEndEvent event = new AsyncGameEndEvent(current, AsyncGameEndEvent.EndReason.PLAYER_QUIT);
            Bukkit.getPluginManager().callEvent(event);
            BroadcastUtils.broadcast(
                    Constants.PREFIX + "乘興而來。盡興而歸。",
                    current.players
            );
            GameManager.GAMES.get(current.type).remove(current);
            return true;
        } else {
            return false;
        }
    }

    public static synchronized Game createGame(GameType type) {
        switch (type) {
            case FLUTTERING_BLOSSOMS: {
                Game game = new FBGame();
                GAMES.get(type).add(game);
                return game;
            }
            case POETRY_WORDLE: {
                Game game = new PWGame();
                GAMES.get(type).add(game);
                return game;
            }
            case POETRY_IDENTIFICATION: {
                Game game = new PIGame();
                GAMES.get(type).add(game);
                return game;
            }
            case POETRY_FILLING: {
                Game game = new PFGame();
                GAMES.get(type).add(game);
                return game;
            }
            default: {
            }
        }
        return null;
    }
}

class StartTask extends BukkitRunnable {
    @Override
    public void run() {
        GameManager.GAMES.forEach((type, games) -> {
            games.forEach(game -> {
                if (game.status.intValue() == 0
                        && game.players.size() >= game.getMinPlayers()
                        && System.currentTimeMillis() - game.createTime > game.getTimeBeforeInit()) {
                    AsyncGameStartEvent event = new AsyncGameStartEvent(game);
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(Main.class), game::start);
                    }
                }
            });
        });
    }
}

class EndTask extends BukkitRunnable {
    @Override
    public void run() {
        List<Game> tempNoPlayer = new ArrayList<>(16);
        List<Game> tempEnded = new ArrayList<>(16);
        GameManager.GAMES.forEach((type, games) -> {
            games.forEach(game -> {
                if (game.status.intValue() == 2) {
                    tempEnded.add(game);
                    return;
                }
                if (game.status.intValue() == 0 && game.players.size() < game.getMinPlayers()
                        && System.currentTimeMillis() - game.createTime > game.getTimeBeforeInit()) {
                    tempNoPlayer.add(game);
                }
            });
        });
        tempNoPlayer.forEach(game -> {
            AsyncGameEndEvent event = new AsyncGameEndEvent(game, AsyncGameEndEvent.EndReason.PLAYER_NOT_ENOUGH);
            Bukkit.getPluginManager().callEvent(event);
            BroadcastUtils.broadcast(
                    Constants.PREFIX + "雅令曲高和寡。一時無人相與。可再擇他日。",
                    game.players
            );
            GameManager.GAMES.get(game.type).remove(game);
        });
        tempEnded.forEach(game -> {
            AsyncGameEndEvent event = new AsyncGameEndEvent(game, AsyncGameEndEvent.EndReason.GAME_FINISH);
            Bukkit.getPluginManager().callEvent(event);
            BroadcastUtils.broadcast(
                    Constants.PREFIX + "乘興而來。盡興而歸。",
                    game.players
            );
            GameManager.GAMES.get(game.type).remove(game);
        });
    }
}