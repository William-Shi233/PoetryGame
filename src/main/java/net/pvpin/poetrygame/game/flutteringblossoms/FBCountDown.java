package net.pvpin.poetrygame.game.flutteringblossoms;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * @author William_Shi
 */
class FBCountDown extends BukkitRunnable {
    private Game game;
    private FBTask task;

    protected FBCountDown(Game game, FBTask task) {
        this.game = game;
        this.task = task;
    }

    @Override
    public void run() {
        if (game.getStatus() == 2) {
            cancel();
            return;
        }
        long startTime = task.roundStamp;
        long currentTime = System.currentTimeMillis();
        long left = ConfigManager.FlutteringBlossoms.TIME_ROUND - (currentTime - startTime);
        if (left < 0) {
            return;
        }
        boolean online = Bukkit.getOfflinePlayer(task.currentGamers.peekFirst()).isOnline();
        if (!online) {
            return;
        }
        Player.Spigot spigot = Bukkit.getOfflinePlayer(task.currentGamers.peekFirst()).getPlayer().spigot();
        spigot.sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent(Constants.PREFIX +
                        "尚有 " +
                        Double.valueOf(Math.ceil(left / 1000)).toString().split("\\.")[0] +
                        " 秒")
        );
    }
}
