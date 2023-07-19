package cn.xiaym.modupdater.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public record Profile(Path path, String gameVersion, List<Mod> mods) {
    public static Profile deserialize(JSONObject jsonObject) {
        Path path = Paths.get(jsonObject.getString("path"));
        String version = jsonObject.getString("version");

        ArrayList<Mod> mods = new ArrayList<>();
        for (Object object : jsonObject.getJSONArray("mods")) {
            if (!(object instanceof JSONObject obj)) {
                continue;
            }

            mods.add(Mod.deserialize(obj));
        }

        return new Profile(path, version, mods);
    }

    public JSONObject serialize() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("path", path.toString());
        jsonObject.put("version", gameVersion);

        ensureSingleModInstance();
        sortMods();
        JSONArray mods = new JSONArray();
        for (Mod mod : this.mods) {
            mods.put(mod.serialize());
        }
        jsonObject.put("mods", mods);

        return jsonObject;
    }

    public void sortMods() {
        mods.sort(Comparator.comparing(Mod::upperCaseName));
    }

    private void ensureSingleModInstance() {
        HashMap<String, Mod> map = new HashMap<>();
        for (Mod mod : new ArrayList<>(mods)) {
            String id = mod.id();

            Mod oldMod = map.getOrDefault(id, null);
            if (oldMod != null) {
                // Simple filter
                if (oldMod.type() == Mod.Type.ONLINE && mod.type() == Mod.Type.LOCAL) {
                    continue;
                }
            }

            map.put(mod.id(), mod);
        }

        mods.clear();
        mods.addAll(map.values());
    }
}
