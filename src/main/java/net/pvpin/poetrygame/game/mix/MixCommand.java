package net.pvpin.poetrygame.game.mix;

import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.api.utils.BroadcastUtils;
import net.pvpin.poetrygame.api.utils.Constants;
import net.pvpin.poetrygame.game.GameManager;
import net.pvpin.poetrygame.game.GameType;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author William_Shi
 */
public class MixCommand implements CommandExecutor, TabCompleter {

    protected static Map<String, String> COMMANDS = new HashMap<>(16);

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafe.get(null);
            Field module = Class.class.getDeclaredField("module");
            long offset = unsafe.objectFieldOffset(module);
            unsafe.putObject(MixCommand.class, offset, Object.class.getModule());
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    public static void registerCommand(String type, String command) {
        Constructor<PluginCommand> constructor;
        PluginCommand pluginCmd = null;
        try {
            constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            // Constructor is protected.
            pluginCmd = constructor.newInstance(command, Main.getPlugin(Main.class));
            Field cmdMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            cmdMapField.setAccessible(true);
            CommandMap map = (CommandMap) cmdMapField.get(Bukkit.getServer().getPluginManager());
            map.register(command, pluginCmd);
        } catch (InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | NoSuchMethodException
                | SecurityException
                | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        Bukkit.getPluginCommand(command).setExecutor(new MixCommand());
        Bukkit.getPluginCommand(command).setTabCompleter(new MixCommand());
        COMMANDS.put(command.toLowerCase(), type.toLowerCase());
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "join": {
                    BroadcastUtils.send(
                            Constants.PREFIX + "請尋飛花雅集之所。戴侍中之席。",
                            ((Player) sender).getUniqueId()
                    );
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
                        boolean result = GameManager.join(
                                ((Player) sender).getUniqueId(),
                                GameType.MIX,
                                COMMANDS.get(label.toLowerCase())
                        );
                        if (result) {
                            // ...
                        } else {
                            BroadcastUtils.send(
                                    Constants.PREFIX + "尋之不得。足下少安毋躁。",
                                    ((Player) sender).getUniqueId()
                            );
                        }
                    }, 3L);
                    break;
                }
                case "quit": {
                    Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(Main.class), () -> {
                        boolean result = GameManager.quit(((Player) sender).getUniqueId(), GameType.MIX);
                        if (result) {
                            BroadcastUtils.send(
                                    Constants.PREFIX + "足下離席矣。",
                                    ((Player) sender).getUniqueId()
                            );
                        } else {
                            BroadcastUtils.send(
                                    Constants.PREFIX + "雖慾離席。未之能也。",
                                    ((Player) sender).getUniqueId()
                            );
                        }
                    });
                    break;
                }
                case "tip": {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
                        BroadcastUtils.send(
                                Constants.PREFIX + "無從提示足下也。",
                                ((Player) sender).getUniqueId()
                        );
                    }, 1L);
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return List.of("join", "quit", "tip");
    }
}
