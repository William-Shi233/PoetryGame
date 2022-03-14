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
            "不", "人", "一", "無", "山", "風", "有", "來",
            "天", "日", "何", "如", "中", "自", "生", "時",
            "雲", "年", "爲", "春", "花", "知", "此", "月",
            "水", "我", "得", "相", "上", "未", "心", "君",
            "清", "歸", "子", "老", "行", "見", "長", "事",
            "今", "三", "可", "是", "去", "江", "白", "雨",
            "與", "千", "下", "萬", "秋", "空", "明", "已",
            "在", "高", "寒", "家", "詩", "誰", "夜", "處"
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
