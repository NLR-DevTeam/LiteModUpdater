package cn.xiaym.modupdater;

import cn.xiaym.modupdater.data.Mod;
import cn.xiaym.modupdater.data.ModUpdate;
import cn.xiaym.modupdater.data.link.CurseForgeLink;
import cn.xiaym.modupdater.data.link.ModrinthLink;
import cn.xiaym.modupdater.utils.QueryBuilder;
import org.fusesource.jansi.Ansi;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UpdateChecker {
    public static List<ModUpdate> checkUpdates() {
        ArrayList<ModUpdate> updates = new ArrayList<>();

        for (Mod mod : Main.currentProfile.mods()) {
            if (mod.type() == Mod.Type.LOCAL || mod.tags().contains(Mod.Tag.KEEP)) {
                continue;
            }

            if (mod.link() instanceof CurseForgeLink) {
                if (ModLinker.CURSEFORGE_API_KEY.isEmpty()) {
                    continue;
                }

                ModUpdate modUpdate = checkCurseForge(mod);
                if (modUpdate != null) {
                    System.out.println(Ansi.ansi()
                            .a("* Detected a update for mod: ")
                            .fgBrightCyan().a(mod.name()).reset()
                            .a(" (")
                            .fgBrightGreen().a("CurseForge").reset()
                            .a(")"));
                    updates.add(modUpdate);
                }
            }

            if (mod.link() instanceof ModrinthLink) {
                ModUpdate modUpdate = checkModrinth(mod);
                if (modUpdate != null) {
                    System.out.println(Ansi.ansi()
                            .a("* Detected a update for mod: ")
                            .fgBrightCyan().a(mod.name()).reset()
                            .a(" (")
                            .fgBrightGreen().a("Modrinth").reset()
                            .a(")"));
                    updates.add(modUpdate);
                }
            }
        }

        return updates;
    }

    public static ModUpdate checkModrinth(Mod mod) {
        String sha1 = getModSHA(mod, true);
        String sha512 = getModSHA(mod, false);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new QueryBuilder("https://api.modrinth.com/v2/project/" + mod.link().projectId() + "/version")
                            .addQuery("loaders", "[\"fabric\"]")
                            .addQuery("game_versions", "[\"" + Main.currentProfile.gameVersion() + "\"]")
                            .build())
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = Main.CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray array = new JSONArray(response.body());

            if (array.length() == 0) {
                return null;
            }

            JSONObject object = array.getJSONObject(0).getJSONArray("files").getJSONObject(0);
            JSONObject hashes = object.getJSONObject("hashes");
            String sha1Hash = hashes.getString("sha1");
            String sha512Hash = hashes.getString("sha512");

            if (Objects.equals(sha512, sha512Hash) || Objects.equals(sha1, sha1Hash)) {
                return null;
            }

            return new ModUpdate(mod, object.getString("url"));
        } catch (Exception ex) {
            System.err.println("Error: An exception was thrown while checking update for mod: " + mod.name() + ";");
            System.err.println("Error: " + ex.getMessage());
        }

        return null;
    }

    public static ModUpdate checkCurseForge(Mod mod) {
        if (ModLinker.CURSEFORGE_API_KEY.isEmpty()) {
            System.err.println("CURSEFORGE | API Key is not set.");
            return null;
        }

        String sha1 = getModSHA(mod, true);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new QueryBuilder("https://api.curseforge.com/v1/mods/" + mod.link().projectId() + "/files")
                            .addQuery("gameVersion", Main.currentProfile.gameVersion())
                            .addQuery("modLoaderType", "Fabric")
                            .addQuery("pageSize", 1)
                            .build())
                    .header("Accept", "application/json")
                    .header("x-api-key", ModLinker.CURSEFORGE_API_KEY)
                    .build();
            HttpResponse<String> response = Main.CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray array = new JSONObject(response.body()).getJSONArray("data");

            if (array.length() == 0) {
                return null;
            }

            JSONObject object = array.getJSONObject(0);
            String sha1Hash = object.getJSONArray("hashes").getJSONObject(0).getString("value");

            if (Objects.equals(sha1, sha1Hash)) {
                return null;
            }

            return new ModUpdate(mod, object.getString("downloadUrl"));
        } catch (Exception ex) {
            System.err.println("Error: An exception was thrown while checking update for mod: " + mod.name() + ";");
            System.err.println("Error: " + ex.getMessage());
        }

        return null;
    }

    public static String getModSHA(Mod mod, boolean useSHA1) {
        String type = "SHA-" + (useSHA1 ? "1" : "512");
        File modFile = mod.path().toFile();
        if (!modFile.exists()) {
            return null;
        }

        byte[] buffer = new byte[1024 * 8];
        try (FileInputStream fis = new FileInputStream(modFile)) {
            MessageDigest digest = MessageDigest.getInstance(type);

            int len;
            while ((len = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }

            return new BigInteger(1, digest.digest()).toString(16);
        } catch (Exception ex) {
            System.err.println("Error: An exception was thrown while getting " + type + " for mod: " + mod.name() + ";");
            System.err.println("Error: " + ex.getMessage());
        }

        return null;
    }
}
