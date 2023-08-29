package cn.xiaym.modupdater.commands;

public class CommandHelp implements Command {
    @Override
    public void onCommand(String[] args) {
        System.out.println("""
                LiteModUpdater v1.0.0 - Help
                # Usage
                 java -jar LiteModUpdater.jar [command] [...subcommand or options]
                                
                # About CurseForge API Key
                 CurseForge's API requires an API key to access, and we don't provide it.
                 You have to request an API key if you want to access CurseForge's services (e.g. Search API).
                 Link: https://console.curseforge.com/
                                
                # Commands:
                 addLocalMods
                  - Adds new local mods to the current profile.
                 autoRemove
                  - Automatically removes mods that are no more exist on the disk.
                 help
                  - Shows this help.
                 linkMods
                  - Automatically links local mods as online mods (Supports Modrinth & CurseForge)
                 listMods
                  - Lists current profile's mods.
                 profiles
                  - Manages profiles.
                 searchMod
                  - Searches a mod on Modrinth or CurseForge through CLI.
                 updateMods
                  - Automatically detects and updates the current profile's mods.
                """);
    }
}
