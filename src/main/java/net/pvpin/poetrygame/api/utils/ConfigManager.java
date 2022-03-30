package net.pvpin.poetrygame.api.utils;

import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.game.mix.MixCommand;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author William_Shi
 */
public class ConfigManager {
    static {
        Main.getPlugin(Main.class).saveResource("config/FlutteringBlossoms.yml", false);
        Main.getPlugin(Main.class).saveResource("config/PoetryWordle.yml", false);
        Main.getPlugin(Main.class).saveResource("config/PoetryIdentification.yml", false);
        Main.getPlugin(Main.class).saveResource("config/PoetryFilling.yml", false);
        Main.getPlugin(Main.class).saveResource("config/Mix.yml", false);
        loadConfig();
    }

    public static class FlutteringBlossoms {
        public static int MAX_PLAYERS;
        public static int MIN_PLAYERS;
        public static int TIME_BEFORE_INIT;
        public static int TIME_VOTE;
        public static int TIME_ROUND;
    }

    public static class PoetryWordle {
        public static int MAX_PLAYERS;
        public static int MIN_PLAYERS;
        public static int TIME_BEFORE_INIT;
        public static int TIME_ROUND;
        public static int ROUND_NUMBER;
        public static int MAX_ATTEMPTS;
    }

    public static class PoetryIdentification {
        public static int MAX_PLAYERS;
        public static int MIN_PLAYERS;
        public static int TIME_BEFORE_INIT;
        public static int TIME_ROUND;
        public static int ROUND_NUMBER;
        public static int MAX_ATTEMPTS;
    }

    public static class PoetryFilling {
        public static int MAX_PLAYERS;
        public static int MIN_PLAYERS;
        public static int TIME_BEFORE_INIT;
        public static int TIME_ROUND;
        public static int ROUND_NUMBER;
        public static int MAX_ATTEMPTS;
    }

    public static class Mix {
        public static Map<String, MemorySection> TYPES = new HashMap<>(16);
    }

    public static void loadConfig() {
        File folder = new File(Main.getPlugin(Main.class).getDataFolder(), "config");
        File flutteringBlossoms = new File(folder, "FlutteringBlossoms.yml");
        YamlConfiguration fbCfg = YamlConfiguration.loadConfiguration(flutteringBlossoms);
        FlutteringBlossoms.MAX_PLAYERS = fbCfg.getInt("maxPlayers");
        FlutteringBlossoms.MIN_PLAYERS = fbCfg.getInt("minPlayers");
        FlutteringBlossoms.TIME_BEFORE_INIT = fbCfg.getInt("timeBeforeInit");
        FlutteringBlossoms.TIME_VOTE = fbCfg.getInt("timeVote");
        FlutteringBlossoms.TIME_ROUND = fbCfg.getInt("timeRound");

        File poetryWordle = new File(folder, "PoetryWordle.yml");
        YamlConfiguration pwCfg = YamlConfiguration.loadConfiguration(poetryWordle);
        PoetryWordle.MAX_PLAYERS = pwCfg.getInt("maxPlayers");
        PoetryWordle.MIN_PLAYERS = pwCfg.getInt("minPlayers");
        PoetryWordle.TIME_BEFORE_INIT = pwCfg.getInt("timeBeforeInit");
        PoetryWordle.TIME_ROUND = pwCfg.getInt("timeRound");
        PoetryWordle.ROUND_NUMBER = pwCfg.getInt("roundNumber");
        PoetryWordle.MAX_ATTEMPTS = pwCfg.getInt("maxAttempts");

        File poetryIdentification = new File(folder, "PoetryIdentification.yml");
        YamlConfiguration piCfg = YamlConfiguration.loadConfiguration(poetryIdentification);
        PoetryIdentification.MAX_PLAYERS = piCfg.getInt("maxPlayers");
        PoetryIdentification.MIN_PLAYERS = piCfg.getInt("minPlayers");
        PoetryIdentification.TIME_BEFORE_INIT = piCfg.getInt("timeBeforeInit");
        PoetryIdentification.TIME_ROUND = piCfg.getInt("timeRound");
        PoetryIdentification.ROUND_NUMBER = piCfg.getInt("roundNumber");
        PoetryIdentification.MAX_ATTEMPTS = piCfg.getInt("maxAttempts");

        File poetryFilling = new File(folder, "PoetryFilling.yml");
        YamlConfiguration pfCfg = YamlConfiguration.loadConfiguration(poetryFilling);
        PoetryFilling.MAX_PLAYERS = pfCfg.getInt("maxPlayers");
        PoetryFilling.MIN_PLAYERS = pfCfg.getInt("minPlayers");
        PoetryFilling.TIME_BEFORE_INIT = pfCfg.getInt("timeBeforeInit");
        PoetryFilling.TIME_ROUND = pfCfg.getInt("timeRound");
        PoetryFilling.ROUND_NUMBER = pfCfg.getInt("roundNumber");
        PoetryFilling.MAX_ATTEMPTS = pfCfg.getInt("maxAttempts");

        File mix = new File(folder, "Mix.yml");
        YamlConfiguration mixCfg = YamlConfiguration.loadConfiguration(mix);
        mixCfg.getKeys(false).forEach(str -> {
            Mix.TYPES.put(str, (MemorySection) mixCfg.get(str));
            MixCommand.registerCommand(str, (String) ((MemorySection) mixCfg.get(str)).get("command"));
        });
    }
}
