package cn.xiaym.modupdater.data;

import cn.xiaym.modupdater.ModLinker;
import cn.xiaym.modupdater.data.link.ModLink;
import org.json.JSONObject;

public record ModSearchResult(String name, ModLink link, String originSearch, JSONObject data) {
    public double relevance() {
        return 1d - ModLinker.STR_DIFF.distance(name, originSearch);
    }
}
