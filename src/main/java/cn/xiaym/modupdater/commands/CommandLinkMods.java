package cn.xiaym.modupdater.commands;

import cn.xiaym.modupdater.Main;
import cn.xiaym.modupdater.ModLinker;
import cn.xiaym.modupdater.data.Mod;
import org.fusesource.jansi.Ansi;

import java.util.ArrayList;

public class CommandLinkMods implements Command {
    @Override
    public void onCommand(String[] args) {
        if (ModLinker.curseForgeAPIKey.isEmpty()) {
            System.out.println(Ansi.ansi().fgBrightYellow().a("""
                    \nWarning! You haven't set the CurseForge API Key!
                    Linking through CurseForge won't work unless you set it in the configuration file.
                    """).reset());
        }

        new ArrayList<>(Main.currentProfile.mods()).forEach(mod -> {
            if (mod.type() == Mod.Type.ONLINE) {
                return;
            }

            try {
                Mod linked = ModLinker.tryLink(mod);
                if (linked != null) {
                    Main.currentProfile.mods().remove(mod);
                    Main.currentProfile.mods().add(linked);
                }
            } catch (Exception e) {
                System.err.println("Error: An exception was thrown while linking mod: " + mod.name() + ";");
                System.err.println("Error: " + e.getMessage());
            }

            Main.saveCurrentProfile();
        });
    }
}
