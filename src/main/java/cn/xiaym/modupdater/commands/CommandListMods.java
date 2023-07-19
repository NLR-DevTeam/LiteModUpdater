package cn.xiaym.modupdater.commands;

import cn.xiaym.modupdater.Main;
import cn.xiaym.modupdater.data.Mod;
import cn.xiaym.modupdater.data.link.ModrinthLink;
import org.fusesource.jansi.Ansi;

public class CommandListMods implements Command {
    @Override
    public void onCommand(String[] args) {
        System.out.println("Listing mods for profile " + Main.config.getString("selected_profile") + ".\n");

        Main.currentProfile.sortMods();
        Main.currentProfile.mods().forEach(mod -> {
            StringBuilder sb = new StringBuilder();
            if (mod.type() == Mod.Type.LOCAL) {
                sb.append(Ansi.ansi()
                        .fgBrightCyan().a(mod.name()).reset()
                        .a(" (" + mod.id() + ") - ")
                        .fgBrightGreen().a("LOCAL").reset());
            } else {
                String linkType = mod.link() instanceof ModrinthLink ? "Modrinth" : "CurseForge";
                sb.append(Ansi.ansi()
                        .fgBrightCyan().a(mod.name()).reset()
                        .a(" (" + mod.id() + ") - Linked: ")
                        .fgBrightGreen().a(linkType + "/" + mod.link().projectId()).reset());
            }

            mod.tags().forEach(tag -> sb.append(" [").append(tag.name()).append("]"));
            System.out.println(sb);
        });
    }
}
