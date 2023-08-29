package cn.xiaym.modupdater.commands;

import cn.xiaym.modupdater.Main;
import cn.xiaym.modupdater.ModLinker;
import cn.xiaym.modupdater.ModSearcher;
import cn.xiaym.modupdater.data.ModSearchResult;
import cn.xiaym.modupdater.data.link.CurseForgeLink;
import cn.xiaym.modupdater.data.link.ModrinthLink;
import org.fusesource.jansi.Ansi;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

    public static void showModSelectMenu(ArrayList<ModSearchResult> results) {
        System.out.println();

        results = sortByRelevance(results);
        for (int i = 0; i < results.size(); i++) {
            System.out.println(Ansi.ansi()
                    .a("[")
                    .fgBright(Ansi.Color.WHITE).a(i).reset()
                    .a("] ")
                    .fgBrightCyan().a(results.get(i).name()).reset());
        }

        System.out.println("\nPlease type the index number of a mod to show the mod's information.");
        ModSearchResult result;
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

        System.out.println("Fetching " + result.link() + "...");
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
