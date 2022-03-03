package net.pvpin.poetrygame.dev;

import com.google.gson.Gson;
import net.pvpin.poetrygame.api.poetry.Poem;
import org.nlpcn.commons.lang.jianfan.JianFan;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author William_Shi
 */
public class Preset {
    public static final List<String> PRESET_KEY_WORDS = List.of(
            "飛", "花", "人", "間", "平", "生", "萬", "里", "何", "處", "知", "見", "青", "山", "春", "風", "歸", "來", "悠", "月", "明"
    );
    public static final File PRESET_OUTPUT_DIR = new File("C:/Users/williamshi/Documents/Code/PoetryGame/src/main/resources/preset");

    public static void main(String[] args) throws Exception {
        var gson = new Gson();
        PRESET_OUTPUT_DIR.mkdirs();
        var allPoems = Collect.collectAll();
        var rankedPoems = Rank.getTops(Rank.AMOUNT);
        PRESET_KEY_WORDS.forEach(key -> {
            try {
                var rankMatch = matchByWord(key, rankedPoems);
                var matchedPoems = (rankMatch.size() < 100 ? matchByWord(key, allPoems) : rankMatch);
                var result = new ArrayList<List<Long>>(1024);
                matchedPoems.forEach(poem -> {
                    result.add(List.of(poem.getId().getMostSignificantBits(), poem.getId().getLeastSignificantBits()));
                });
                var str = gson.toJson(result, List.class);
                var outputFile = new File(PRESET_OUTPUT_DIR, key + ".json");
                outputFile.createNewFile();
                var writer = new BufferedWriter(new FileWriter(outputFile));
                writer.write(str);
                writer.close();
            } catch (IOException ignored) {
            }
        });
    }

    public static List<Poem> matchByWord(String keyWord, List<Poem> all) {
        return all.stream()
                .filter(
                        map -> map.getParagraphs()
                                .stream()
                                .anyMatch(str -> str.contains(keyWord))
                )
                .collect(Collectors.toList());
    }
}
