package cn.xiaym.modupdater.commands;

import cn.xiaym.modupdater.Main;
import cn.xiaym.modupdater.ModLinker;
import cn.xiaym.modupdater.ModSearcher;
import cn.xiaym.modupdater.data.Mod;
import cn.xiaym.modupdater.data.ModSearchResult;
import cn.xiaym.modupdater.data.link.CurseForgeLink;
import cn.xiaym.modupdater.data.link.ModrinthLink;
import cn.xiaym.modupdater.utils.QueryBuilder;
import org.fusesource.jansi.Ansi;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class CommandSearchMod implements Command {
    public static void printHelp() {
        System.out.println("""
                Showing the help for subcommand: searchMod
                Usage: searchMod <platform> <mod name>
                                
                Available platforms:
                 modrinth, m\t - Modrinth
                 curseforge, c\t - CurseForge
                                
                If your mod name argument contains space, you should surround it with quotes.""");
    }

    public static ArrayList<ModSearchResult> sortByRelevance(ArrayList<ModSearchResult> results) {
        ArrayList<ModSearchResult> res = new ArrayList<>();
        HashSet<Double> relevanceSet = new HashSet<>();
        for (ModSearchResult result : results) {
            relevanceSet.add(result.relevance());
        }

        List<Double> sorted = relevanceSet.stream().sorted().toList();
        for (int i = sorted.size() - 1; i >= 0; i--) {
            for (ModSearchResult result : results) {
                if (result.relevance() == sorted.get(i)) {
                    res.add(result);
                }
            }
        }

        return res;
    }

    public static void showCategories(List<String> categoryNames) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String categoryName : categoryNames) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(" ");
            }

            stringBuilder.append("[")
                    .append(Ansi.ansi().fgBrightCyan().a(categoryName).reset())
                    .append("]");
        }

        System.out.println("* Categories:\t" + stringBuilder);
    }

    public static Set<String> getVersions(ModSearchResult result) {
        HashSet<String> versions = new HashSet<>();
        if (result.link() instanceof CurseForgeLink) {
            JSONArray array = result.data().getJSONArray("latestFilesIndexes");
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                if (object.optInt("modLoader") != 4) {
                    continue;
                }

                versions.add(object.getString("gameVersion"));
            }
        } else {
            JSONArray array = result.data().getJSONArray("versions");
            for (int i = 0; i < array.length(); i++) {
                versions.add(array.getString(i));
            }
        }

        return versions.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static URL getDownloadURL(ModSearchResult result, String version) throws Exception {
        if (result.link() instanceof ModrinthLink) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new QueryBuilder("https://api.modrinth.com/v2/project/" + result.link().projectId() + "/version")
                            .addQuery("loaders", "[\"fabric\"]")
                            .addQuery("game_versions", "[\"" + version + "\"]")
                            .build())
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = Main.CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            String url = new JSONArray(response.body()).getJSONObject(0)
                    .getJSONArray("files").getJSONObject(0).getString("url");
            return new URL(url);
        }

        String fileID = null;
        JSONArray array = result.data().getJSONArray("latestFilesIndexes");
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            if (object.optInt("modLoader") != 4) {
                continue;
            }

            if (object.getString("gameVersion").equals(version)) {
                fileID = object.get("fileId").toString();
                break;
            }
        }

        if (fileID == null) {
            return null;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.curseforge.com/v1/mods/" + result.link().projectId() + "/files/" + fileID + "/download-url"))
                .header("Accept", "application/json")
                .header("x-api-key", ModLinker.CURSEFORGE_API_KEY)
                .build();
        HttpResponse<String> response = Main.CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return new URL(new JSONObject(response.body()).getString("data"));
    }

    public static void showModSelectMenu(ArrayList<ModSearchResult> results) {
        ModSearchResult result;
        if (results.size() > 1) {
            System.out.println("\n=== Result ===");

            results = sortByRelevance(results);
            for (int i = 0; i < results.size(); i++) {
                System.out.println(Ansi.ansi()
                        .a("[")
                        .fgBright(Ansi.Color.WHITE).a(i).reset()
                        .a("] ")
                        .fgBrightCyan().a(results.get(i).name()).reset());
            }

            System.out.println("\nPlease type the index number of a mod to show the mod's information.");
            try {
                int index = Integer.parseInt(Main.readLine("Index> ", false));
                if (index < 0 || results.size() < index) {
                    System.out.println("Invalid index, abort.");
                    return;
                }

                result = results.get(index);
            } catch (NumberFormatException ex) {
                System.out.println("Abort.");
                return;
            }
        } else {
            result = results.get(0);
        }

        System.out.println("\n* Mod Name:\t" + Ansi.ansi().fgBrightCyan().a(result.name()).reset());
        System.out.println("* Mod ID:\t" + Ansi.ansi().fgBrightGreen().a(result.link()).reset());

        List<String> categories = new ArrayList<>();
        if (result.link() instanceof CurseForgeLink) {
            JSONArray categoriesArray = result.data().getJSONArray("categories");
            for (int i = 0; i < categoriesArray.length(); i++) {
                categories.add(categoriesArray.getJSONObject(i).getString("name"));
            }
        } else {
            JSONArray categoriesArray = result.data().getJSONArray("display_categories");
            for (int i = 0; i < categoriesArray.length(); i++) {
                categories.add(categoriesArray.getString(i));
            }
        }

        showCategories(categories);

        System.out.println("* Description:\t" + Ansi.ansi()
                .fgBright(Ansi.Color.WHITE).a(
                        result.data().optString(result.link() instanceof ModrinthLink ? "description" : "summary", "<No Data>")
                ).reset());

        Set<String> versions = getVersions(result);
        System.out.println("* Versions:\t" + versions);

        System.out.println("\nPlease type a version to download or press ^C to cancel.");
        String currentGameVersion = Main.currentProfile.gameVersion();

        String versionToDownload = Main.readLine(versions.contains(currentGameVersion)
                ? "Version (" + currentGameVersion + ")> "
                : "Version> ", false);
        if (versionToDownload.isEmpty()) {
            if (versions.contains(currentGameVersion)) {
                versionToDownload = currentGameVersion;
            } else {
                System.out.println("Abort.");
                return;
            }
        }

        if (!versions.contains(versionToDownload)) {
            System.out.println("Invalid version, abort.");
            return;
        }

        System.out.println("\nFetching version data for minecraft version " + versionToDownload + "...");
        try {
            URL url = Objects.requireNonNull(getDownloadURL(result, versionToDownload));

            String path = url.getPath();
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            Path savePath = Main.currentProfile.path().resolve(fileName);
            System.out.println("Downloading: " + fileName);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            Files.copy(conn.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Download completed, importing...");

            JSONObject modManifest = CommandAddLocalMods.readModJSON(savePath.toFile());
            if (modManifest == null) {
                System.err.println("Error: Failed to read mod manifest.");
                return;
            }

            Main.currentProfile.mods().add(
                    new Mod(modManifest.getString("id"), modManifest.getString("name"), savePath,
                            Mod.Type.ONLINE, result.link(), new HashSet<>())
            );
            Main.saveCurrentProfile();

            System.out.println("Done.");
        } catch (Exception e) {
            System.err.println(Ansi.ansi().fgBrightRed().a("An exception occurred!\n" + e).reset());
        }
    }

    @Override
    public void onCommand(String[] args) {
        if (args.length < 2) {
            printHelp();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "modrinth", "m" -> {
                System.out.println("Now searching on Modrinth: " + args[1]);

                JSONArray data = ModSearcher.searchModrinth(args[1], 10);
                if (data.length() == 0) {
                    System.out.println("No mods were found.");
                    return;
                }

                ArrayList<ModSearchResult> resultList = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    resultList.add(new ModSearchResult(obj.getString("title").trim(),
                            new ModrinthLink(obj.getString("project_id")), args[1], obj));
                }

                showModSelectMenu(resultList);
            }

            case "curseforge", "c" -> {
                if (ModLinker.CURSEFORGE_API_KEY.isEmpty()) {
                    System.out.println(Ansi.ansi().fgBrightRed().a("""
                            \nError! You haven't set the CurseForge API Key!
                            Searching CurseForge won't work unless you set it in the configuration file.
                            """).reset());

                    return;
                }

                System.out.println("Now searching on CurseForge: " + args[1]);

                JSONArray data = ModSearcher.searchCurseForge(args[1], ModLinker.CURSEFORGE_API_KEY, 10);
                if (data.length() == 0) {
                    System.out.println("No mods were found, sometimes this is a bug of curseforge, please try again later.");
                    return;
                }

                ArrayList<ModSearchResult> resultList = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    resultList.add(new ModSearchResult(obj.getString("name").trim(),
                            new CurseForgeLink(String.valueOf(obj.getInt("id"))), args[1], obj));
                }

                showModSelectMenu(resultList);
            }

            default -> printHelp();
        }
    }
}
