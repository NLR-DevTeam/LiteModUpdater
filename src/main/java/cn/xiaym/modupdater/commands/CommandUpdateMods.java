package cn.xiaym.modupdater.commands;

import cn.xiaym.modupdater.Main;
import cn.xiaym.modupdater.UpdateChecker;
import cn.xiaym.modupdater.data.Mod;
import cn.xiaym.modupdater.data.ModUpdate;
import org.fusesource.jansi.Ansi;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class CommandUpdateMods implements Command {
    @Override
    public void onCommand(String[] args) {
        System.out.println("Please wait while we're checking mods' updates...");
        List<ModUpdate> updates = UpdateChecker.checkUpdates();
        System.out.println(updates.size() > 0 ?
                "Totally detected " + updates.size() + " mods' updates." :
                "No updates were found.");

        if (updates.size() > 0) {
            System.out.println("\nDownloading started");
            updates.forEach(update -> {
                Mod mod = update.mod();
                String[] split = update.updateURL().split("/");
                Path savePath = Main.currentProfile.path().resolve(URLDecoder.decode(split[split.length - 1], StandardCharsets.UTF_8));
                Path oldPath = mod.path();

                try {
                    HttpURLConnection conn = (HttpURLConnection) URI.create(update.updateURL()).toURL().openConnection();
                    conn.connect();

                    // Delete old mod file
                    Files.deleteIfExists(oldPath);

                    // Save new mod file
                    InputStream is = conn.getInputStream();
                    OutputStream os = Files.newOutputStream(savePath, StandardOpenOption.CREATE);
                    byte[] buf = new byte[1024 * 8];
                    int len;
                    while ((len = is.read(buf)) != -1) {
                        os.write(buf, 0, len);
                    }
                    is.close();

                    Mod newMod = new Mod(mod.id(), mod.name(), savePath, mod.type(), mod.link(), mod.tags());
                    Main.currentProfile.mods().remove(mod);
                    Main.currentProfile.mods().add(newMod);
                    Main.saveCurrentProfile();

                    System.out.println(Ansi.ansi()
                            .a("* Mod ")
                            .fgBrightCyan().a(mod.name()).reset()
                            .a(" downloaded successfully."));
                } catch (Exception ex) {
                    System.err.println("Error: An exception was thrown while downloading update for mod: " + mod.name() + ";");
                    System.err.println("Error: " + ex.getMessage());
                }
            });
        }
    }
}
