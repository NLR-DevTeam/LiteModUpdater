package cn.xiaym.modupdater;

import cn.xiaym.modupdater.data.Mod;
import cn.xiaym.modupdater.data.link.CurseForgeLink;
import cn.xiaym.modupdater.data.link.ModLink;
import cn.xiaym.modupdater.data.link.ModrinthLink;
import com.vdurmont.emoji.EmojiParser;
import info.debatty.java.stringsimilarity.Cosine;
import org.fusesource.jansi.Ansi;
import org.json.JSONArray;
import org.json.JSONObject;

public class ModLinker {
    public static final String CURSEFORGE_API_KEY = Main.config.optString(Registry.KEY_CURSEFORGE_APIKEY, "");
    public static final Cosine STR_DIFF = new Cosine();

    public static Mod tryLink(Mod mod) {
        String modName = trimModName(mod.name());
        System.out.println(Ansi.ansi()
                .a("* Trying to link mod: ")
                .fgBrightCyan().a(modName).reset());

        String modrinthProj = tryLinkModrinth(modName);
        System.out.println(modrinthProj == null ?
                Ansi.ansi().fgBrightRed().a("Link FAILED through Modrinth.").reset() :
                Ansi.ansi().fgBrightGreen().a("Link successfully through Modrinth: " + modrinthProj).reset());

        if (modrinthProj != null) {
            return mod.modifyLink(new ModrinthLink(modrinthProj));
        }

        if (!CURSEFORGE_API_KEY.isEmpty()) {
            String curseForgeProj = tryLinkCurseForge(modName);
            System.out.println(curseForgeProj == null ?
                    Ansi.ansi().fgBrightRed().a("Link FAILED through CurseForge.").reset() :
                    Ansi.ansi().fgBrightGreen().a("Link successfully through CurseForge: " + curseForgeProj).reset());

            if (curseForgeProj != null) {
                return mod.modifyLink(new CurseForgeLink(curseForgeProj));
            }
        }

        ModLink modLink = Registry.FALLBACK_LINKS.getOrDefault(mod.id(), null);
        if (modLink != null) {
            System.out.println(Ansi.ansi()
                    .fgBrightYellow().a("Using internal fallback link: " + modLink).reset());
            return mod.modifyLink(modLink);
        }

        return null;
    }

    public static String tryLinkModrinth(String modName) {
        JSONArray array = ModSearcher.searchModrinth(modName, 1);
        if (array.length() == 0) {
            return null;
        }

        JSONObject hit = array.getJSONObject(0);
        String projId = hit.getString("project_id");
        String title = EmojiParser.removeAllEmojis(hit.getString("title")).trim();
        double distance = STR_DIFF.distance(trimModName(title).toLowerCase(), modName.toLowerCase());

        if (distance > 0.25) {
            return null;
        }

        return projId;
    }

    public static String tryLinkCurseForge(String modName) {
        if (CURSEFORGE_API_KEY.isEmpty()) {
            System.err.println("CURSEFORGE | API Key is not set.");
            return null;
        }

        JSONArray array = ModSearcher.searchCurseForge(modName, CURSEFORGE_API_KEY, 1);
        if (array.length() == 0) {
            return null;
        }

        JSONObject data = array.getJSONObject(0);
        String id = String.valueOf(data.getInt("id"));
        String name = EmojiParser.removeAllEmojis(data.getString("name")).trim();
        double distance = STR_DIFF.distance(trimModName(name).toLowerCase(), modName.toLowerCase());

        if (distance > 0.25) {
            return null;
        }

        return id;
    }

    public static String trimModName(String modName) {
        modName = nonCaseSensitiveTrim(modName, "for Fabric");
        modName = nonCaseSensitiveTrim(modName, "-Fabric");
        modName = nonCaseSensitiveTrim(modName, "[Fabric]");
        modName = nonCaseSensitiveTrim(modName, "(Fabric)");
        modName = nonCaseSensitiveTrim(modName, "(For Fabric)");
        modName = nonCaseSensitiveTrim(modName, "Fabric Version");
        modName = nonCaseSensitiveTrim(modName, "(Fabric Version)");

        return modName.trim();
    }

    public static String nonCaseSensitiveTrim(String raw, String needle) {
        raw = raw.trim();

        if (raw.toLowerCase().startsWith(needle.toLowerCase())) {
            raw = raw.substring(needle.length());
        }

        if (raw.toLowerCase().endsWith(needle.toLowerCase())) {
            raw = raw.substring(0, raw.length() - needle.length());
        }

        return raw;
    }
}
