package net.pvpin.poetrygame.api;

import com.google.gson.Gson;
import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.api.poetry.PoetryUtils;
import net.pvpin.poetrygame.game.flutteringblossoms.FBCmd;
import net.pvpin.poetrygame.game.poetryfilling.PFCmd;
import net.pvpin.poetrygame.game.poetryidentification.PICmd;
import net.pvpin.poetrygame.game.poetrywordle.PWCmd;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.plugin.java.*;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * @author William_Shi
 */
public class Main extends JavaPlugin {
    @Override
    public void onLoad() {
        try {
            Class.forName(PoetryUtils.class.getName());
            Class.forName(PoetryUtils.PresetManager.class.getName());
        } catch (ClassNotFoundException ignored) {
            // Do nothing.
        }
    }

    @Override
    public void onEnable() {
        try {
            Bukkit.getScheduler().runTaskAsynchronously(this, this::updateStats);

            Class.forName(ConfigManager.class.getName());
        } catch (Exception ignored) {
            // Do nothing.
        }

        Bukkit.getPluginCommand("flutteringblossoms").setExecutor(new FBCmd());
        Bukkit.getPluginCommand("flutteringblossoms").setTabCompleter(new FBCmd());
        Bukkit.getPluginCommand("poetrywordle").setExecutor(new PWCmd());
        Bukkit.getPluginCommand("poetrywordle").setTabCompleter(new PWCmd());
        Bukkit.getPluginCommand("poetryidentification").setExecutor(new PICmd());
        Bukkit.getPluginCommand("poetryidentification").setTabCompleter(new PICmd());
        Bukkit.getPluginCommand("poetryfilling").setExecutor(new PFCmd());
        Bukkit.getPluginCommand("poetryfilling").setTabCompleter(new PFCmd());
    }

    private void updateStats() {
        try {
            var bStatsID = 14731;
            Metrics metrics = new Metrics(this, bStatsID);

            String url = "https://api.github.com/repos/MinerTribe/PoetryGame/releases/latest";
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            var json = new Gson().fromJson(new InputStreamReader(connection.getInputStream()), Map.class);
            var version = (String) json.get("tag_name");
            if (!version.startsWith(this.getDescription().getVersion())) {
                getLogger().warning("插件需要更新方可使用！");
                Bukkit.getScheduler().runTask(this, () -> Bukkit.getPluginManager().disablePlugin(this));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
