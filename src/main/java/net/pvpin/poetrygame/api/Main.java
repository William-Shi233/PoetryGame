package net.pvpin.poetrygame.api;

import com.google.gson.Gson;
import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.game.flutteringblossoms.FBCmd;
import net.pvpin.poetrygame.game.poetryidentification.PICmd;
import org.bukkit.*;
import org.bukkit.plugin.java.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author William_Shi
 */
public class Main extends JavaPlugin {
    public static final Map<String, Map<String, Object>> ALL_POEMS = new ConcurrentHashMap<>();
    public static final Map<String, Map<String, Object>> TANG_TOPS = new ConcurrentHashMap<>();

    @Override
    public void onLoad() {
        Gson gson = new Gson();
        gson.fromJson(new InputStreamReader(
                        this.getResource("rank.json"),
                        StandardCharsets.UTF_8
                ), List.class)
                .forEach(obj -> {
                    Map<String, Object> map = (Map<String, Object>) obj;
                    List<String> paras = (List<String>) map.get("paragraphs");
                    paras.forEach(content -> {
                        String stripped[] = content.split("，|。|？|！+");
                        Arrays.stream(stripped)
                                .forEach(str -> {
                                    ALL_POEMS.put(str, map);
                                });
                    });
                });
        gson.fromJson(new InputStreamReader(
                        this.getResource("tangtop.json"),
                        StandardCharsets.UTF_8
                ), List.class)
                .forEach(obj -> {
                    Map<String, Object> map = (Map<String, Object>) obj;
                    List<String> paras = (List<String>) map.get("paragraphs");
                    paras.forEach(content -> {
                        String stripped[] = content.split("，|。|？|！+");
                        Arrays.stream(stripped)
                                .forEach(str -> {
                                    TANG_TOPS.put(str, map);
                                });
                    });
                });
    }

    @Override
    public void onEnable() {
        try {
            Class.forName(ConfigManager.class.getName());
        } catch (ClassNotFoundException ignored) {
            // Do nothing.
        }
        Bukkit.getPluginCommand("flutteringblossoms").setExecutor(new FBCmd());
        Bukkit.getPluginCommand("flutteringblossoms").setTabCompleter(new FBCmd());
        Bukkit.getPluginCommand("poetryidentification").setExecutor(new PICmd());
        Bukkit.getPluginCommand("poetryidentification").setTabCompleter(new PICmd());
    }

}
