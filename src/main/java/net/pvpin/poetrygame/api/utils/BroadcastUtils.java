package net.pvpin.poetrygame.api.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.poetry.Poem;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.nlpcn.commons.lang.jianfan.JianFan;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author William_Shi
 */
public class BroadcastUtils {
    public static final String SPACE = ChatColor.BOLD + " " + ChatColor.RESET + " ";

    public static void send(String msg, UUID uuid) {
        broadcast(msg, List.of(uuid));
    }

    public static void broadcast(String msg, List<UUID> uuids) {
        broadcast(new TextComponent(msg), uuids);
    }

    public static void broadcast(BaseComponent msg, List<UUID> uuids) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
            uuids.forEach(uuid -> {
                OfflinePlayer pl = Bukkit.getOfflinePlayer(uuid);
                if (pl.isOnline()) {
                    if (pl.getPlayer().getLocale().equals("zh_cn")) {
                        String json = ComponentSerializer.toString(msg);
                        String simplified = JianFan.f2j(json);
                        pl.getPlayer().spigot().sendMessage(ComponentSerializer.parse(simplified));
                    } else {
                        pl.getPlayer().spigot().sendMessage(msg);
                    }
                }
            });
        }, 1L);
    }

    public static BaseComponent generatePoemComponent(String origin, Poem poem) {
        TextComponent result = new TextComponent();
        result.setText(origin);
        result.setUnderlined(true);
        String blanks = SPACE.repeat(Math.max(
                0, ((poem.getParagraphs().stream().mapToInt(String::length).sum()
                        / poem.getParagraphs().size())
                        - (poem.getTitle().length() + 2)) / 2
        ));
        List<Content> contentsInfo = List.of(
                new Text(
                        new StringBuilder()
                                .append(ChatColor.BOLD)
                                .append(ChatColor.AQUA)
                                .append(blanks)
                                .append("《")
                                .append(poem.getTitle())
                                .append("》\n")
                                .toString()
                ),
                new Text(
                        new StringBuilder()
                                .append(ChatColor.BOLD)
                                .append(ChatColor.AQUA)
                                .append(blanks)
                                .append(SPACE.repeat(poem.getTitle().length() + 2))
                                .append(poem.getAuthor())
                                .append("\n")
                                .toString()
                )
        );
        List<Content> contents = new ArrayList<>(16);
        contents.addAll(contentsInfo);
        poem.getParagraphs().stream()
                .map(str -> ChatColor.BOLD + str)
                .map(str -> ChatColor.GOLD + str)
                .map(str -> str + "\n")
                .map(Text::new)
                .forEach(contents::add);
        result.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, contents
        ));
        return result;
    }
}
