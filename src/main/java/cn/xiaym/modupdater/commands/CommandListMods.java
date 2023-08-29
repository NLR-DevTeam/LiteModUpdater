package cn.xiaym.modupdater.commands;

import cn.xiaym.modupdater.Main;
import cn.xiaym.modupdater.data.Mod;
import org.fusesource.jansi.Ansi;

public class CommandListMods implements Command {
    @Override
    public void onCommand(String[] args) {
        System.out.println("Listing mods for profile " + Main.config.getString("selected_profile") + ".\n");

        Main.currentProfile.sortMods();
        for (Mod mod : Main.currentProfile.mods()) {
            StringBuilder sb = new StringBuilder();
            if (mod.type() == Mod.Type.LOCAL) {
                sb.append(Ansi.ansi()
                        .fgBrightCyan().a(mod.name()).reset()
                        .a(" (" + mod.id() + ") - ")
                        .fgBrightGreen().a("LOCAL").reset());
            } else {
                sb.append(Ansi.ansi()
                        .fgBrightCyan().a(mod.name()).reset()
                        .a(" (" + mod.id() + ") - Linked: ")
                        .fgBrightGreen().a(mod.link().toString()).reset());
            }

            for (Mod.Tag tag : mod.tags()) {
                sb.append(" [").append(tag.name()).append("]");
            }

            System.out.println(sb);
        }
    }
}
