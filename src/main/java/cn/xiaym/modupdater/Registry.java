package cn.xiaym.modupdater;

import cn.xiaym.modupdater.commands.*;
import cn.xiaym.modupdater.data.link.ModLink;
import cn.xiaym.modupdater.data.link.ModrinthLink;

import java.util.HashMap;

public class Registry {
    public static final HashMap<String, Command> COMMANDS = new HashMap<>() {{
        put("help", new CommandHelp());

        put("addLocalMods".toLowerCase(), new CommandAddLocalMods());
        put("autoRemove".toLowerCase(), new CommandAutoRemove());
        put("listMods".toLowerCase(), new CommandListMods());
        put("linkMods".toLowerCase(), new CommandLinkMods());
        put("updateMods".toLowerCase(), new CommandUpdateMods());
        put("searchMod".toLowerCase(), new CommandSearchMod());

        put("profiles", new CommandProfiles());
    }};

    public static final HashMap<String, ModLink> FALLBACK_LINKS = new HashMap<>() {{
        put("notenoughanimations", new ModrinthLink("MPCX6s5C"));
        put("forcecloseworldloadingscreen", new ModrinthLink("blWBX5n1"));
    }};

    public static final String KEY_CURSEFORGE_APIKEY = "curseforge_apikey";
}
