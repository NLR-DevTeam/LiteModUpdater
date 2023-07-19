package cn.xiaym.modupdater.commands;

import cn.xiaym.modupdater.Main;
import cn.xiaym.modupdater.data.Mod;
import org.fusesource.jansi.Ansi;

import java.nio.file.Files;
import java.util.ArrayList;

public class CommandAutoRemove implements Command {
    @Override
    public void onCommand(String[] args) {
        int removed = 0;
        for (Mod mod : new ArrayList<>(Main.currentProfile.mods())) {
            if (Files.exists(mod.path())) {
                continue;
            }

            Main.currentProfile.mods().remove(mod);
            removed++;

            System.out.println(Ansi.ansi()
                    .a("* Auto removed mod: ")
                    .fgBrightCyan().a(mod.name()).reset()
                    .a(" (" + mod.id() + ")"));
        }

        System.out.println(removed == 0 ?
                "Done, removed nothing." :
                "Done, auto removed " + removed + " non-exists mods.");
        Main.saveCurrentProfile();
    }
}