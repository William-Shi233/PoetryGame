package net.pvpin.poetrygame.api.poetry;

import com.google.gson.Gson;
import net.pvpin.poetrygame.api.Main;
import net.pvpin.poetrygame.dev.Preset;
import org.nlpcn.commons.lang.jianfan.JianFan;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author William_Shi
 */
public class PoetryUtils {
    public static final Map<UUID, Poem> ALL_POEMS = new ConcurrentHashMap<>(524288);
    public static final List<Poem> TANG_TOPS = new ArrayList<>(512);

    static {
        Gson gson = new Gson();
        ((List<Map<String, Object>>) gson.fromJson(new InputStreamReader(
                Main.getPlugin(Main.class).getResource("rank.json"),
                StandardCharsets.UTF_8
        ), List.class))
                .stream()
                .map(Poem::deserialize)
                .peek(Poem::getCutParagraphs)
                .forEach(poem -> ALL_POEMS.put(poem.getId(), poem));
        ((List<Map<String, Object>>) gson.fromJson(new InputStreamReader(
                Main.getPlugin(Main.class).getResource("tangtop.json"),
                StandardCharsets.UTF_8
        ), List.class))
                .stream()
                .map(Poem::deserialize)
                .peek(Poem::getCutParagraphs)
                .forEach(TANG_TOPS::add);
    }

    public static Poem searchFromAll(String poem) {
        AtomicReference<UUID> reference = new AtomicReference<>();
        String stripped = PoetryUtils.strip(poem);
        for (char c : stripped.toCharArray()) {
            String key = String.valueOf(c);
            if (PresetManager.PRESETS_CACHE.containsKey(key)) {
                Poem match = searchInCache(stripped, key);
                if (match != null) {
                    return match;
                }
            }
        }
        if (stripped.length() <= 7) {
            for (Map.Entry<UUID, Poem> entry : ALL_POEMS.entrySet()) {
                if (entry.getValue().getCutParagraphs()
                        .stream().map(PoetryUtils::strip)
                        .anyMatch(str -> str.equals(stripped))) {
                    return ALL_POEMS.get(entry.getKey());
                }
            }
        }
        for (Map.Entry<UUID, Poem> entry : ALL_POEMS.entrySet()) {
            UUID uuid = entry.getKey();
            List<String> paras = entry.getValue().getCutParagraphs()
                    .stream().map(PoetryUtils::strip).collect(Collectors.toList());
            boolean contains = paras.contains(stripped);
            for (int index = 0; index < paras.size(); index++) {
                for (int max = 1; max < paras.size() - index; max++) {
                    StringBuilder builder = new StringBuilder();
                    for (int current = 0; current <= max; current++) builder.append(paras.get(index + current));
                    String full = builder.toString(); // Already stripped.
                    contains = contains || full.equals(stripped);
                    if (contains) break;
                }
                if (contains) break;
            }
            if (contains) {
                reference.set(uuid);
                break;
            }
        }
        return reference.get() == null ? null : ALL_POEMS.get(reference.get());
    }

    public static Poem searchFromPreset(String poem, String key) {
        try {
            PresetManager.loadPreset(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String stripped = PoetryUtils.strip(poem);
        if (PresetManager.PRESETS_CACHE.get(key).containsKey(stripped)) {
            return ALL_POEMS.get(PresetManager.PRESETS_CACHE.get(key).get(poem));
        }
        Poem match = searchInCache(stripped, key);
        return match != null ? match : searchFromAll(poem);
    }

    private static Poem searchInCache(String stripped, String key) {
        for (Map.Entry<String, UUID> entry : PresetManager.PRESETS_CACHE.get(key).entrySet()) {
            if (stripped.contains(entry.getKey())) {
                var poetry = ALL_POEMS.get(entry.getValue());
                List<String> paras = poetry.getCutParagraphs();
                boolean contains = false;
                for (int index = 0; index < paras.size(); index++) {
                    for (int max = 1; max < paras.size() - index; max++) {
                        StringBuilder builder = new StringBuilder();
                        for (int current = 0; current <= max; current++) builder.append(paras.get(index + current));
                        String full = strip(builder.toString());
                        contains = contains || full.equals(stripped);
                        if (contains) break;
                    }
                    if (contains) break;
                }
                return contains ? poetry : null;
            }
        }
        return null;
    }

    public static String strip(String origin) {
        String stripped = origin.replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "");
        return JianFan.f2j(stripped);
    }

    public static List<String> cut(String origin) {
        String str[] = origin.split("，|。|？|！");
        return Arrays.stream(str)
                .map(result -> result.replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", ""))
                .collect(Collectors.toList());
    }

    public static class PresetManager {
        public static final Map<String, List<UUID>> PRESETS = new ConcurrentHashMap<>(32);
        public static final Map<String, Map<String, UUID>> PRESETS_CACHE = new ConcurrentHashMap<>(32); // Map<Key, Map<Simplified Poetry, UUID>>
        private static final File folder = new File(Main.getPlugin(Main.class).getDataFolder(), "preset");

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
            InputStream stream = Main.getPlugin(Main.class).getResource("preset/" + keyWord + ".json");
            if (stream != null) {
                PRESETS.put(keyWord, ((List<List<Double>>)
                                new Gson().fromJson(
                                        new InputStreamReader(stream, StandardCharsets.UTF_8), List.class
                                )
                        ).stream()
                                .map(list -> new UUID(list.get(0).longValue(), list.get(1).longValue()))
                                .collect(Collectors.toList())
                );
                generateCache(keyWord);
                return;
            } else {
                File file = new File(folder, keyWord + ".json");
                if (file.exists()) {
                    PRESETS.put(keyWord, ((List<List<Double>>)
                                    new Gson().fromJson(
                                            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), List.class
                                    )
                            ).stream()
                                    .map(list -> new UUID(list.get(0).longValue(), list.get(1).longValue()))
                                    .collect(Collectors.toList())
                    );
                    generateCache(keyWord);
                    return;
                }
            }
            File file = new File(folder, keyWord + ".json");
            file.createNewFile();
            List<UUID> result = new ArrayList<>(1024);
            ALL_POEMS.entrySet().stream().forEach(entry -> {
                List<String> paras = entry.getValue().getParagraphs();
                if (paras.stream().anyMatch(str -> str.contains(keyWord))) {
                    result.add(entry.getKey());
                }
            });
            PRESETS.put(keyWord, result);
            generateCache(keyWord);
            String str = new Gson().toJson(
                    result.stream()
                            .map(uuid -> List.of(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()))
                            .collect(Collectors.toList()),
                    List.class
            );
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

        public static void generateCache(String key) {
            Map<String, UUID> result = new HashMap<>(128);
            ALL_POEMS.entrySet().stream()
                    .filter(entry -> entry.getValue().getParagraphs().stream().anyMatch(
                            str -> str.contains(key)
                    ))
                    .forEach(entry -> {
                        Poem poem = entry.getValue();
                        poem.cutParagraphs.stream()
                                .filter(str -> str.contains(key))
                                .map(PoetryUtils::strip)
                                .forEach(str -> result.put(str, entry.getKey()));
                    });
            PRESETS_CACHE.put(key, result);
        }
    }
}
