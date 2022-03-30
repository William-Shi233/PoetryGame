package net.pvpin.poetrygame.dev;

import com.google.gson.Gson;
import net.pvpin.poetrygame.api.poetry.Poem;
import org.nlpcn.commons.lang.jianfan.JianFan;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * @author William_Shi
 */
public class Collect {
    public static final File COLLECT_OUTPUT = new File("C:/Users/williamshi/Documents/Code/PoetryGame/src/main/resources/all.json");
    public static final File TANG_INPUT = new File("C:/Users/williamshi/Documents/Code/PoetryGame/src/main/resources/cache/tangtops.json");
    public static final File TANG_OUTPUT = new File("C:/Users/williamshi/Documents/Code/PoetryGame/src/main/resources/poetry/tangtops.gz");

    public static void main(String[] args) throws Exception {
        var writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(TANG_OUTPUT)), StandardCharsets.UTF_8));
        var str = Files.readString(Paths.get(TANG_INPUT.toURI()), StandardCharsets.UTF_8);
        writer.write(str);
        writer.close();
    }

    public static ArrayList<Poem> collectAll() throws Exception {
        var gson = new Gson();
        var allPoetryList = new ArrayList<Poem>(524288);// 2^19
        var poemsDir = new File("C:/Users/williamshi/Documents/Code/PoetryGame/chinese-poetry-master/json");
        Arrays.stream(poemsDir.listFiles((dir, name) -> name.startsWith("poet")))
                .forEach(file -> {
                    try {
                        var listPoet = gson.fromJson(new FileReader(file), List.class);
                        allPoetryList.addAll((Collection<? extends Poem>)
                                listPoet.stream()
                                        .map(obj -> Poem.deserialize((Map<String, Object>) obj))
                                        .collect(Collectors.toList())
                        );
                    } catch (Exception ignored) {
                    }
                });
        allPoetryList.addAll(collectChuCi());
        allPoetryList.addAll(collectShiJing());
        return allPoetryList;
    }

    public static List<Poem> collectChuCi() throws Exception {
        var gson = new Gson();
        var chuciFile = new File("C:/Users/williamshi/Documents/Code/PoetryGame/chinese-poetry-master/chuci/chuci.json");
        var chuciFileList = (List<Poem>) gson.fromJson(new FileReader(chuciFile), List.class).stream()
                .map(obj -> {
                    var map = (Map) obj;
                    var author = map.get("author");
                    var title = "楚辞·" + (map.get("section").equals(map.get("title")) ? map.get("section") : map.get("section") + "·" + map.get("title"));
                    var paragraphs = map.get("content");
                    var id = UUID.randomUUID();
                    var result = Map.of(
                            "author", author,
                            "title", title,
                            "paragraphs", paragraphs,
                            "id", id
                    );
                    return result;
                })
                .map(obj -> Poem.deserialize((Map<String, Object>) obj))
                .collect(Collectors.toList());
        return chuciFileList;
    }

    public static List<Poem> collectShiJing() throws Exception {
        var gson = new Gson();
        var shijingFile = new File("C:/Users/williamshi/Documents/Code/PoetryGame/chinese-poetry-master/shijing/shijing.json");
        var shijingFileList = (List<Poem>) gson.fromJson(new FileReader(shijingFile), List.class).stream()
                .map(obj -> {
                    var map = (Map) obj;
                    var author = "先秦";
                    var title = "诗经·" + map.get("chapter") + "·" + map.get("section") + "·" + map.get("title");
                    var paragraphs = map.get("content");
                    var id = UUID.randomUUID();
                    var result = Map.of(
                            "author", author,
                            "title", title,
                            "paragraphs", paragraphs,
                            "id", id
                    );
                    return result;
                })
                .map(obj -> Poem.deserialize((Map<String, Object>) obj))
                .collect(Collectors.toList());
        return shijingFileList;
    }
}
