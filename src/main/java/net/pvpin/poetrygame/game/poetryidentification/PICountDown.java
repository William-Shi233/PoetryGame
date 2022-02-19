package net.pvpin.poetrygame.game.poetryidentification;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.api.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author William_Shi
 */
public class PICountDown extends BukkitRunnable {
    private PIGame game;

    protected PICountDown(PIGame game) {
        this.game = game;
    }

    @Override
    public void run() {
        if (game.getStatus() == 2) {
            cancel();
            return;
        }
        long startTime = game.roundStamp;
        long currentTime = System.currentTimeMillis();
        long left = ConfigManager.PoetryIdentification.TIME_ROUND - (currentTime - startTime);
        if (left < 0) {
            return;
        }
        game.getPlayers().forEach(uuid -> {
            if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                Player.Spigot spigot = Bukkit.getOfflinePlayer(uuid).getPlayer().spigot();
                spigot.sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(Constants.PREFIX +
                                "尚有 " +
                                Double.valueOf(Math.ceil(left / 1000)).toString().split("\\.")[0] +
                                " 秒"));
            }
        });
    }
}
