package net.pvpin.poetrygame.api.utils;

import net.pvpin.poetrygame.api.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * @author William_Shi
 */
public class ConfigManager {
    static {
        Main.getPlugin(Main.class).saveResource("config/FlutteringBlossoms.yml", false);
        Main.getPlugin(Main.class).saveResource("config/PoetryWordle.yml", false);
        Main.getPlugin(Main.class).saveResource("config/PoetryIdentification.yml", false);
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
        public static int MAX_ATTEMPTS;
    }

    public static class PoetryIdentification {
        public static int MAX_PLAYERS;
        public static int MIN_PLAYERS;
        public static int TIME_BEFORE_INIT;
        public static int TIME_ROUND;
        public static int ROUND_NUMBER;
    }

    public static void loadConfig() {
        File folder = new File(Main.getPlugin(Main.class).getDataFolder(),"config");
        File flutteringBlossoms = new File(folder,"FlutteringBlossoms.yml");
        YamlConfiguration fbCfg = YamlConfiguration.loadConfiguration(flutteringBlossoms);
        FlutteringBlossoms.MAX_PLAYERS = fbCfg.getInt("maxPlayers");
        FlutteringBlossoms.MIN_PLAYERS = fbCfg.getInt("minPlayers");
        FlutteringBlossoms.TIME_BEFORE_INIT = fbCfg.getInt("timeBeforeInit");
        FlutteringBlossoms.TIME_VOTE = fbCfg.getInt("timeVote");
        FlutteringBlossoms.TIME_ROUND = fbCfg.getInt("timeRound");

        File poetryWordle = new File(folder,"PoetryWordle.yml");
        YamlConfiguration pwCfg = YamlConfiguration.loadConfiguration(poetryWordle);
        PoetryWordle.MAX_PLAYERS = pwCfg.getInt("maxPlayers");
        PoetryWordle.MIN_PLAYERS = pwCfg.getInt("minPlayers");
        PoetryWordle.TIME_BEFORE_INIT = pwCfg.getInt("timeBeforeInit");
        PoetryWordle.MAX_ATTEMPTS = pwCfg.getInt("maxAttempts");

        File poetryIdentification = new File(folder,"PoetryIdentification.yml");
        YamlConfiguration piCfg = YamlConfiguration.loadConfiguration(poetryIdentification);
        PoetryIdentification.MAX_PLAYERS = piCfg.getInt("maxPlayers");
        PoetryIdentification.MIN_PLAYERS = piCfg.getInt("minPlayers");
        PoetryIdentification.TIME_BEFORE_INIT = piCfg.getInt("timeBeforeInit");
        PoetryIdentification.TIME_ROUND = piCfg.getInt("timeRound");
        PoetryIdentification.ROUND_NUMBER = piCfg.getInt("roundNumber");
    }
}
