package net.pvpin.poetrygame.game.poetrywordle;

import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.events.common.AsyncPlayerTipEvent;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.api.poetry.PoetryUtils;
import net.pvpin.poetrygame.game.GameManager;
import net.pvpin.poetrygame.game.GameType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author William_Shi
 */
public class PWCmd implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "join": {
                    BroadcastUtils.send(
                            Constants.PREFIX + "請覽《唐詩三百首》以尋佳句。",
                            ((Player) sender).getUniqueId()
                    );
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
                        boolean result = GameManager.join(((Player) sender).getUniqueId(), GameType.POETRY_WORDLE);
                        if (result) {
                            // ...
                        } else {
                            BroadcastUtils.send(
                                    Constants.PREFIX + "尋之不得。足下少安毋躁。",
                                    ((Player) sender).getUniqueId()
                            );
                        }
                    }, 3L);
                    break;
                }
                case "quit": {
                    Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(Main.class), () -> {
                        boolean result = GameManager.quit(((Player) sender).getUniqueId(), GameType.POETRY_WORDLE);
                        if (result) {
                            BroadcastUtils.send(
                                    Constants.PREFIX + "足下離席矣。",
                                    ((Player) sender).getUniqueId()
                            );
                        } else {
                            BroadcastUtils.send(
                                    Constants.PREFIX + "雖慾離席。未之能也。",
                                    ((Player) sender).getUniqueId()
                            );
                        }
                    });
                    break;
                }
                case "tip": {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
                        PWGame game = (PWGame) GameManager.getGame(((Player) sender).getUniqueId(), GameType.POETRY_WORDLE);
                        if (game == null) {
                            BroadcastUtils.send(
                                    Constants.PREFIX + "足下尚未入席。",
                                    ((Player) sender).getUniqueId()
                            );
                            return;
                        }
                        if (game.getStatus() != 1) {
                            BroadcastUtils.send(
                                    Constants.PREFIX + "無從提示足下也。",
                                    ((Player) sender).getUniqueId()
                            );
                        }
                        String answer = game.currentAnswer.replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "");
                        int index = ThreadLocalRandom.current().nextInt(answer.length());
                        String idStr = Constants.convertChineseNumbers(index + 1);
                        String tip = "提示：此詩" +
                                PoetryUtils.searchFromAll(answer).getAuthor() +
                                "作。第" + idStr + "字為“ " + answer.charAt(index) + " ”。";
                        AsyncPlayerTipEvent event = new AsyncPlayerTipEvent(game, (Player) sender, tip);
                        Bukkit.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            tip = event.getTip();
                            BroadcastUtils.send(
                                    Constants.PREFIX + tip,
                                    ((Player) sender).getUniqueId()
                            );
                        }
                    }, 1L);
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return List.of("join", "quit", "tip");
    }
}
