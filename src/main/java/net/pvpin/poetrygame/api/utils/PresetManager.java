package net.pvpin.poetrygame.api.utils;

import com.google.gson.Gson;
import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.dev.Preset;
import org.nlpcn.commons.lang.jianfan.JianFan;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author William_Shi
 */
public class PresetManager {
    public static final Map<String, Map<String, Map<String, Object>>> PRESETS = new ConcurrentHashMap<>(32);
    private static final Main instance = Main.getPlugin(Main.class);
    private static final File folder = new File(instance.getDataFolder(), "preset");

    static {
        if (!folder.exists()) {
            folder.mkdirs();
            Preset.PRESET_KEY_WORDS.forEach(str -> {
                        try {
                            PresetManager.loadPreset(str);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
            );
        }
    }

    public static void loadPreset(String keyWord) throws Exception {
        if (PRESETS.containsKey(keyWord)) {
            return;
        }
        InputStream stream = instance.getResource("preset/" + keyWord + ".json");
        if (stream != null) {
            PRESETS.put(keyWord, new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), Map.class));
            return;
        } else {
            File file = new File(folder, keyWord + ".json");
            if (file.exists()) {
                PRESETS.put(keyWord, new Gson().fromJson(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), Map.class));
                return;
            }
        }
        File file = new File(folder, keyWord + ".json");
        file.createNewFile();
        Map<String, Map<String, Object>> map = new HashMap<>(1024);
        Main.ALL_POEMS.entrySet().stream().filter(key -> (key.getKey().contains(keyWord)))
                .forEach(key -> {
                    map.put(JianFan.f2j(key.getKey()), key.getValue());
                });
        PRESETS.put(keyWord, map);
        String str = new Gson().toJson(map, Map.class);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8));
        writer.write(str);
        writer.close();
    }

    public static void unloadPreset(String keyWord) {
        if (!PRESETS.containsKey(keyWord)) {
            return;
        }
        PRESETS.remove(keyWord);
    }
}
