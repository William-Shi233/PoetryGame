package net.pvpin.poetrygame.dev;

import net.pvpin.poetrygame.api.poetry.Poem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author William_Shi
 */
public class Keyword {
    public static final Map<Character, AtomicInteger> KEY_WORD_STATISTICS = new ConcurrentHashMap<>();
    public static final int AMOUNT = 64;

    public static void main(String[] args) throws Exception {
        System.out.println(getTopKeyWords(AMOUNT));
    }

    public static List<String> getTopKeyWords(int amount) {
        KEY_WORD_STATISTICS.clear();
        List<Poem> poems = new ArrayList<>(524288);
        try {
            poems.addAll(Collect.collectAll());
            poems.forEach(poem -> {
                StringBuilder builder = new StringBuilder();
                poem.getParagraphs().forEach(builder::append);
                String paras = builder.toString().replaceAll("（[^）]*）", "");
                String stripped = paras.replaceAll("\\pP|\\pS|\\pC|\\pN|\\pZ", "");
                for (char c : stripped.toCharArray()) {
                    if (KEY_WORD_STATISTICS.containsKey(c)) {
                        KEY_WORD_STATISTICS.get(c).incrementAndGet();
                    } else {
                        KEY_WORD_STATISTICS.put(c, new AtomicInteger(1));
                    }
                }
            });
            List<Map.Entry<Character, AtomicInteger>> rankListAll = new ArrayList<>(KEY_WORD_STATISTICS.entrySet());
            rankListAll.sort((o1, o2) -> Integer.compare(o1.getValue().get(), o2.getValue().get()) * (-1));
            List<Map.Entry<Character, AtomicInteger>> result = rankListAll.subList(0, AMOUNT);
            return result.stream()
                    .map(Map.Entry::getKey)
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return List.of();
    }
}
