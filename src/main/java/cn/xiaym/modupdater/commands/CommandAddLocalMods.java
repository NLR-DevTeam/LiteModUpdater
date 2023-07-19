package cn.xiaym.modupdater.commands;

import cn.xiaym.modupdater.Main;
import cn.xiaym.modupdater.data.Mod;
import org.fusesource.jansi.Ansi;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CommandAddLocalMods implements Command {
    public static JSONObject readModJSON(File file) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(file));

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.getName().equalsIgnoreCase("fabric.mod.json")) {
                    continue;
                }

                byte[] bytes = zis.readAllBytes();
                zis.closeEntry();

                return new JSONObject(new String(bytes));
            }
        } catch (Exception ignored) {
            // Do nothing
        }

        return null;
    }

    @Override
    public void onCommand(String[] args) {
        Path path = Main.currentProfile.path();
        if (Files.notExists(path)) {
            System.out.println("FATAL: The mod directory is not exists.");
            return;
        }

        List<Mod> mods = new ArrayList<>(Main.currentProfile.mods());
        HashMap<String, Mod> modsMap = new HashMap<>();
        for (Mod mod : mods) {
            modsMap.put(mod.id(), mod);
        }

        int added = 0;
        System.out.println("Scanning started");

        File dir = path.toFile();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (!file.getName().endsWith(".jar")) {
                continue;
            }

            JSONObject object = readModJSON(file);
            if (object != null) {
                String modId = object.getString("id");

                Mod oldMod = modsMap.getOrDefault(modId, null);
                if (oldMod != null) {
                    if (Files.exists(oldMod.path())) {
                        continue;
                    }

                    Main.currentProfile.mods().remove(oldMod);
                }

                Mod mod = new Mod(modId, object.getString("name"), file.toPath(), Mod.Type.LOCAL, null, new HashSet<>());
                mods.add(mod);
                added++;

                System.out.println(Ansi.ansi()
                        .a("* Found new local mod: ")
                        .fgBrightCyan().a(mod.name()).reset()
                        .a(" (" + mod.id() + ")"));
            }
        }

        System.out.println(added == 0 ?
                "Done, no new local mods were found." :
                ("Done, found " + added + " new local mods."));

        Main.currentProfile.mods().clear();
        Main.currentProfile.mods().addAll(mods);
        Main.saveCurrentProfile();
    }
}
