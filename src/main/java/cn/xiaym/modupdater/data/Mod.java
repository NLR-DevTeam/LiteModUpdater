package cn.xiaym.modupdater.data;

import cn.xiaym.modupdater.data.link.CurseForgeLink;
import cn.xiaym.modupdater.data.link.ModLink;
import cn.xiaym.modupdater.data.link.ModrinthLink;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public record Mod(String id, String name, Path path, Type type, ModLink link, Set<Tag> tags) {
    public static Mod deserialize(JSONObject jsonObject) {
        String id = jsonObject.getString("id");
        String name = jsonObject.getString("name");
        Path path = Paths.get(jsonObject.getString("path"));
        Type type = Type.valueOf(jsonObject.getString("type"));

        ModLink link = null;
        if (type == Type.ONLINE) {
            JSONObject linkObject = jsonObject.getJSONObject("link");
            String proj = linkObject.getString("proj");

            link = switch (linkObject.getString("type")) {
                case "modrinth" -> new ModrinthLink(proj);
                case "curseforge" -> new CurseForgeLink(proj);
                default -> null;
            };
        }

        Set<Tag> tags = new HashSet<>();
        if (jsonObject.has("tags")) {
            JSONArray tagsArray = jsonObject.getJSONArray("tags");
            for (int i = 0; i < tagsArray.length(); i++) {
                String key = tagsArray.getString(i);
                tags.add(Tag.valueOf(key));
            }
        }

        return new Mod(id, name, path, type, link, tags);
    }

    public JSONObject serialize() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("name", name);
        jsonObject.put("path", path.toString());
        jsonObject.put("type", type.name());

        if (type == Type.ONLINE) {
            JSONObject linkObject = new JSONObject();
            linkObject.put("type", link instanceof ModrinthLink ? "modrinth" : "curseforge");
            linkObject.put("proj", link.projectId());

            jsonObject.put("link", linkObject);
        }

        if (tags.size() > 0) {
            JSONArray arr = new JSONArray();
            tags.forEach(tag -> arr.put(tag.name()));

            jsonObject.put("tags", arr);
        }

        return jsonObject;
    }

    public Mod modifyLink(ModLink link) {
        return link == null ?
                new Mod(id, name, path, Type.LOCAL, null, tags) :
                new Mod(id, name, path, Type.ONLINE, link, tags);
    }

    public String upperCaseName() {
        return name.toUpperCase();
    }

    public enum Tag {
        KEEP
    }

    public enum Type {
        ONLINE,
        LOCAL
    }
}
