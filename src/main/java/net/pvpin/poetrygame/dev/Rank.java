package net.pvpin.poetrygame.dev;

import com.google.gson.*;
import net.pvpin.poetrygame.api.poetry.Poem;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author William_Shi
 */
public class Rank {
    public static final int AMOUNT = 30000; // Select Top 10000 poems in the list.
    public static final String RANK_OUTPUT = "C:/Users/williamshi/Documents/Code/PoetryGame/src/main/resources/rank.json";
    public static final String POEMS_DIR = "C:/Users/williamshi/Documents/Code/PoetryGame/chinese-poetry-master/json";
    public static final String RANKS_DIR = "C:/Users/williamshi/Documents/Code/PoetryGame/chinese-poetry-master/rank/poet";

    public static void main(String[] args) throws Exception {
        var gson = new GsonBuilder()
                .registerTypeAdapter(Poem.class,
                        (JsonSerializer<Poem>) (poem, type, jsonSerializationContext) -> {
                            JsonObject obj = new JsonObject();
                            obj.addProperty("author", poem.getAuthor());
                            obj.addProperty("title", poem.getTitle());
                            JsonArray parasArray = new JsonArray(poem.getParagraphs().size());
                            poem.getParagraphs().forEach(parasArray::add);
                            obj.add("paragraphs", parasArray);
                            if (poem.getTags() != null) {
                                if (!poem.getTags().isEmpty()) {
                                    JsonArray tagsArray = new JsonArray(poem.getParagraphs().size());
                                    poem.getTags().forEach(tagsArray::add);
                                    obj.add("tags", tagsArray);
                                }
                            }
                            JsonArray idArray = new JsonArray(2);
                            idArray.add(poem.getId().getMostSignificantBits());
                            idArray.add(poem.getId().getLeastSignificantBits());
                            obj.add("id", idArray);
                            return obj;
                        })
                .create();
        var rankListTops = getTops(AMOUNT);
        rankListTops.addAll(Collect.collectChuCi());
        rankListTops.addAll(Collect.collectShiJing());
        var str = gson.toJson(rankListTops, List.class);
        var outputFile = new File(RANK_OUTPUT);
        outputFile.createNewFile();
        var writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(str);
        writer.close();
    }

    public static List<Poem> getTops(int amount) throws Exception {
        var gson = new Gson();
        var rankResultMap = new HashMap<Map<String, Object>, Integer>(524288);// 2^19
        var poemsDir = new File(POEMS_DIR);
        var ranksDir = new File(RANKS_DIR);
        Arrays.stream(poemsDir.listFiles((dir, name) -> name.startsWith("poet"))).forEach(file -> {
            try {
                var split = file.getName().split("\\.");
                var sb = new StringBuilder("poet.");
                sb.append(split[1]).append(".rank.").append(split[2]).append(".json");
                var rankName = sb.toString();
                var rankFile = new File(ranksDir, rankName);
                var listRank = gson.fromJson(new FileReader(rankFile), List.class);
                var listPoet = gson.fromJson(new FileReader(file), List.class);
                for (int index = 0; index < listRank.size(); index++) {
                    var rank = ((Double) ((Map<String, Object>) listRank.get(index)).get("so360")).intValue();
                    rankResultMap.put((Map<String, Object>) listPoet.get(index), rank);
                }
            } catch (Exception ignored) {
            }
        });
        List<Map.Entry<Map<String, Object>, Integer>> rankListAll = new ArrayList<>(rankResultMap.entrySet());
        rankListAll.sort((o1, o2) -> o1.getValue().compareTo(o2.getValue()) * (-1));
        return rankListAll.subList(0, amount).stream()
                .map(Map.Entry::getKey)
                .peek(map -> {
                    if (!map.containsKey("id")) {
                        map.put("id", UUID.randomUUID());
                    }
                })
                .map(Poem::deserialize)
                .collect(Collectors.toList());
    }
}
