package cn.xiaym.modupdater.commands;

import cn.xiaym.modupdater.Main;
import cn.xiaym.modupdater.UpdateChecker;
import cn.xiaym.modupdater.data.Mod;
import cn.xiaym.modupdater.data.ModUpdate;
import cn.xiaym.modupdater.utils.Retryer;
import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class CommandUpdateMods implements Command {
    public static String getValidPath(String defaultPath, String... tries) {
        for (String pathStr : tries) {
            if (pathStr == null) {
                continue;
            }

            try {
                Path path = Paths.get(pathStr);
                if (Files.notExists(path)) {
                    Files.createDirectory(path);
                    Files.delete(path);
                }

                return pathStr;
            } catch (Exception ignored) {
                // Do nothing
            }
        }

        return defaultPath;
    }

    @Override
    public void onCommand(String[] args) {
        System.out.println("Please wait while we're checking mods' updates...");
        List<ModUpdate> updates = UpdateChecker.checkUpdates();
        System.out.println(updates.isEmpty() ? "No updates were found." :
                "Totally detected " + updates.size() + " mods' updates.");

        if (!updates.isEmpty()) {
            System.out.println("\nDownloading started");
            for (ModUpdate update : updates) {
                Mod mod = update.mod();
                String[] split = update.updateURL().split("/");
                Path savePath = Main.currentProfile.path().resolve(URLDecoder.decode(split[split.length - 1], StandardCharsets.UTF_8));
                Path oldPath = mod.path();

                // Auto backup
                if (Main.config.optBoolean("auto_backup_mods") && Files.exists(mod.path())) {
                    try {
                        String alias = Main.currentProfile.alias();
                        Path profileDir = Main.BACKUP_DIRECTORY.resolve(getValidPath("FALLBACK", alias, Main.currentProfile.gameVersion()));
                        if (Files.notExists(profileDir)) {
                            Files.createDirectories(profileDir);
                        }

                        Path backupPath = profileDir.resolve(mod.path().getFileName().toString());
                        Files.copy(oldPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

                        System.out.println(Ansi.ansi()
                                .a("* Auto backed up mod ")
                                .fgBrightCyan().a(mod.name()).reset()
                                .a(" to: ")
                                .a(backupPath.toString()));
                    } catch (IOException ex) {
                        System.out.println(Ansi.ansi()
                                .fgBrightYellow().a("Warning: Failed to backup mod ").reset()
                                .fgBrightCyan().a(mod.name()).reset()
                                .fgBrightYellow().a(", skipping update!"));
                        continue;
                    }
                }

                try {
                    Retryer.execute(() -> {
                        HttpURLConnection conn = (HttpURLConnection) URI.create(update.updateURL()).toURL().openConnection();
                        conn.connect();

                        // Delete old mod file
                        Files.deleteIfExists(oldPath);

                        // Save new mod file
                        InputStream is = conn.getInputStream();
                        Files.copy(is, savePath, StandardCopyOption.REPLACE_EXISTING);

                        Mod newMod = new Mod(mod.id(), mod.name(), savePath, mod.type(), mod.link(), mod.tags());
                        Main.currentProfile.mods().remove(mod);
                        Main.currentProfile.mods().add(newMod);
                        Main.saveCurrentProfile();

                        System.out.println(Ansi.ansi()
                                .a("* Mod ")
                                .fgBrightCyan().a(mod.name()).reset()
                                .a(" downloaded successfully."));
                    }, 5);
                } catch (Throwable th) {
                    System.err.println("Error: An exception was thrown while downloading update for mod: " + mod.name() + ";");
                    System.err.println("Error: " + th.getMessage());
                }
            }
        }
    }
}
