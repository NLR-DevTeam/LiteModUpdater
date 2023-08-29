package cn.xiaym.modupdater;

import cn.xiaym.modupdater.utils.QueryBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ModSearcher {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static JSONArray searchModrinth(String modName, int limit) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new QueryBuilder("https://api.modrinth.com/v2/search")
                            .addQuery("limit", limit)
                            .addQuery("query", modName)
                            .addQuery("facets", "[[\"categories:fabric\"],[\"project_type:mod\"]]")
                            .build())
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            return new JSONObject(response.body()).getJSONArray("hits");
        } catch (Exception ignored) {
            // Do nothing
        }

        return new JSONArray();
    }

    public static JSONArray searchCurseForge(String modName, String apiKey, int limit) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new QueryBuilder("https://api.curseforge.com/v1/mods/search")
                            .addQuery("gameId", 432)
                            .addQuery("pageSize", limit)
                            .addQuery("searchFilter", modName)
                            .addQuery("modLoaderType", "Fabric")
                            .build())
                    .header("Accept", "application/json")
                    .header("x-api-key", apiKey)
                    .build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            return new JSONObject(response.body()).getJSONArray("data");
        } catch (Exception ignored) {
            // Do nothing
        }

        return new JSONArray();
    }
}
