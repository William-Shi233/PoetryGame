package net.pvpin.poetrygame.api.utils;

import net.pvpin.poetrygame.api.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.nlpcn.commons.lang.jianfan.JianFan;

import java.util.List;
import java.util.UUID;

/**
 * @author William_Shi
 */
public class BroadcastUtils {
    public static void broadcast(String msg, List<UUID> uuids) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
            uuids.forEach(uuid -> {
                OfflinePlayer pl = Bukkit.getOfflinePlayer(uuid);
                if (pl.isOnline()) {
                    if (pl.getPlayer().getLocale().equals("zh_cn")) {
                        pl.getPlayer().sendMessage(JianFan.f2j(msg));
                    } else {
                        pl.getPlayer().sendMessage(msg);
                    }
                }
            });
        }, 1L);
    }

    public static void send(String msg, UUID uuid) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
            OfflinePlayer pl = Bukkit.getOfflinePlayer(uuid);
            if (pl.isOnline()) {
                if (pl.getPlayer().getLocale().equals("zh_cn")) {
                    pl.getPlayer().sendMessage(JianFan.f2j(msg));
                } else {
                    pl.getPlayer().sendMessage(msg);
                }
            }
        }, 1L);
    }
}
