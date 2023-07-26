package cn.xiaym.modupdater;

import cn.xiaym.modupdater.commands.*;
import cn.xiaym.modupdater.data.link.ModLink;
import cn.xiaym.modupdater.data.link.ModrinthLink;

import java.util.HashMap;

public class Registry {
    public static final HashMap<String, Command> COMMANDS = new HashMap<>() {{
        put("help", new CommandHelp());

        put("addLocalMods", new CommandAddLocalMods());
        put("autoRemove", new CommandAutoRemove());
        put("listMods", new CommandListMods());
        put("linkMods", new CommandLinkMods());
        put("updateMods", new CommandUpdateMods());

        put("profiles", new CommandProfiles());
    }};

    public static final HashMap<String, ModLink> FALLBACK_LINKS = new HashMap<>() {{
        put("NotEnoughAnimations", new ModrinthLink("MPCX6s5C"));
    }};

    public static final String KEY_CURSEFORGE_APIKEY = "curseforge_apikey";
}
