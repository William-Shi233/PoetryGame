package net.pvpin.poetrygame.api.poetry;


import java.util.*;

/**
 * @author William_Shi
 */
public class Poem {
    protected final String author;
    protected final List<String> paragraphs;
    protected final List<String> cutParagraphs = new ArrayList<>(16);
    protected final String title;
    protected final UUID id;
    protected final List<String> tags;

    public Poem(String author, List<String> paragraphs, String title, UUID id, List<String> tags) {
        this.author = author;
        this.paragraphs = paragraphs;
        this.title = title;
        this.id = id;
        this.tags = tags;
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getParagraphs() {
        return paragraphs;
    }

    public List<String> getCutParagraphs() {
        if (!this.cutParagraphs.isEmpty()) {
            return List.copyOf(this.cutParagraphs);
        }
        paragraphs.forEach(para -> {
            List<String> paras = PoetryUtils.cut(para);
            cutParagraphs.addAll(paras);
        });
        return List.copyOf(this.cutParagraphs);
    }

    public String getTitle() {
        return title;
    }

    public UUID getId() {
        return id;
    }

    public List<String> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Poem poem = (Poem) o;
        return id.equals(poem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, paragraphs, title, id, tags);
    }

    public static Poem deserialize(Map<String, Object> map) {
        var id = map.get("id");
        UUID uuid = null;
        if (id instanceof UUID) {
            uuid = (UUID) id;
        }
        if (id instanceof String) {
            uuid = UUID.fromString((String) id);
        }
        if (id instanceof List) {
            Double most = (Double) ((List<?>) id).get(0);
            Double least = (Double) ((List<?>) id).get(1);
            uuid = new UUID(most.longValue(), least.longValue());
        }
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        return new Poem(
                (String) map.get("author"),
                (List<String>) map.get("paragraphs"),
                (String) map.get("title"),
                uuid,
                map.containsKey("tags") ? (List<String>) map.get("tags") : List.of()
        );
    }

    public Map<String, Object> serialize() {
        return Map.of(
                "author", author,
                "title", title,
                "paragraphs", paragraphs,
                "id", id,
                "tags", tags == null ? List.of() : tags
        );
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\"author\":\"");
        builder.append(author);
        builder.append("\",");
        builder.append("\"paragraphs\":[");
        StringJoiner paragraphsJoiner = new StringJoiner(",");
        paragraphs.stream().map(str -> "\"" + str + "\"").forEach(paragraphsJoiner::add);
        builder.append(paragraphsJoiner);
        builder.append("],");
        builder.append("\"title\":\"");
        builder.append(title);
        builder.append("\",");
        if (tags != null) {
            if (!tags.isEmpty()) {
                builder.append("\"tags\":[");
                StringJoiner tagsJoiner = new StringJoiner(",");
                tags.stream().map(str -> "\"" + str + "\"").forEach(tagsJoiner::add);
                builder.append(tagsJoiner);
                builder.append("],");
            }
        }
        builder.append("\"id\":[");
        builder.append(id.getMostSignificantBits());
        builder.append(",");
        builder.append(id.getLeastSignificantBits());
        builder.append("],");
        builder.append("}");
        return builder.toString();
    }
}
