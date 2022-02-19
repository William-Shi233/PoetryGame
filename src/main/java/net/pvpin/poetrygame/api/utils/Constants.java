package net.pvpin.poetrygame.api.utils;

import org.bukkit.ChatColor;

import java.util.List;

/**
 * @author William_Shi
 */
public class Constants {
    public static final String PREFIX = new StringBuilder()
            .append(ChatColor.BOLD)
            .append(ChatColor.LIGHT_PURPLE)
            .append("[")
            .append(ChatColor.RESET)
            .append(ChatColor.AQUA)
            .append("飛")
            .append(ChatColor.RED)
            .append("❀")
            .append(ChatColor.AQUA)
            .append("令")
            .append(ChatColor.BOLD)
            .append(ChatColor.LIGHT_PURPLE)
            .append("] ")
            .append(ChatColor.RESET)
            .append(ChatColor.GOLD)
            .toString();
    public static final List<String> FLUTTERING_BLOSSOMS_POEMS =
            List.of(
                    "亂絮飛花撲行路，正如東郭放船時。",
                    "啼鳥傍簷春寂寂，飛花掠水晚翩翩。",
                    "春城無處不飛花，寒食東風御柳斜。",
                    "夢覺巫山春色，醉眼飛花狼藉。",
                    "六出飛花入戶時，坐看青竹變瓊枝。",
                    "風約飛花滿曲廊，蕭然無客共焚香。",
                    "解把飛花蒙日月，不知天地有清霜。",
                    "解籜有聲驚倦枕，飛花無力點清池。",
                    "馬頭風捲飛花過，又得殘春一日晴。",
                    "飛花兩岸照船紅，百里榆堤半日風。"
            );
}
