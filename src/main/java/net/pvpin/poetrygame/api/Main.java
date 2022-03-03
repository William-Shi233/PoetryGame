package net.pvpin.poetrygame.api;

import net.pvpin.poetrygame.api.utils.ConfigManager;
import net.pvpin.poetrygame.api.poetry.PoetryUtils;
import net.pvpin.poetrygame.game.flutteringblossoms.FBCmd;
import net.pvpin.poetrygame.game.poetryidentification.PICmd;
import net.pvpin.poetrygame.game.poetrywordle.PWCmd;
import org.bukkit.*;
import org.bukkit.plugin.java.*;

/**
 * @author William_Shi
 */
public class Main extends JavaPlugin {
    @Override
    public void onLoad() {
        try {
            Class.forName(PoetryUtils.class.getName());
        } catch (ClassNotFoundException ignored) {
            // Do nothing.
        }
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
        Bukkit.getPluginCommand("poetrywordle").setExecutor(new PWCmd());
        Bukkit.getPluginCommand("poetrywordle").setTabCompleter(new PWCmd());
    }

}
