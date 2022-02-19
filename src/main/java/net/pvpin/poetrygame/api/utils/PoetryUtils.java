package net.pvpin.poetrygame.api.utils;

import net.pvpin.poetrygame.api.Main;
import org.nlpcn.commons.lang.jianfan.JianFan;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author William_Shi
 */
public class PoetryUtils {
    public static Map<String, Object> searchFromAll(String poem) {
        String stripped = strip(poem);
        AtomicReference<Map<String, Object>> result = null;
        Main.ALL_POEMS.forEach((str, map) -> {
            if (JianFan.f2j(str).equals(stripped)) {
                result.set(map);
            }
        });
        return result.get();
    }

    public static Map<String, Object> searchFromPreset(String poem, String key) {
        String stripped = strip(poem);
        try {
            PresetManager.loadPreset(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return PresetManager.PRESETS.get(key).get(stripped);
    }

    public static String strip(String origin) {
        String stripped = origin.replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "");
        return JianFan.f2j(stripped);
    }
}
