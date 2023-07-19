package cn.xiaym.modupdater;

import cn.xiaym.modupdater.utils.QueryBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ModSearcher {
    private static final HttpClient client = HttpClient.newHttpClient();

    public static JSONObject searchModrinth(String modName) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new QueryBuilder("https://api.modrinth.com/v2/search")
                            .addQuery("limit", 1)
                            .addQuery("query", modName)
                            .addQuery("facets", "[[\"categories:fabric\"],[\"project_type:mod\"]]")
                            .build())
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONArray array = new JSONObject(response.body()).getJSONArray("hits");
            if (array.length() == 0) {
                return null;
            }

            return array.getJSONObject(0);
        } catch (Exception ignored) {
            // Do nothing
        }

        return null;
    }

    public static JSONObject searchCurseForge(String modName, String apiKey) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new QueryBuilder("https://api.curseforge.com/v1/mods/search")
                            .addQuery("gameId", 432)
                            .addQuery("pageSize", 1)
                            .addQuery("searchFilter", modName)
                            .addQuery("modLoaderType", "Fabric")
                            .build())
                    .header("Accept", "application/json")
                    .header("x-api-key", apiKey)
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONArray array = new JSONObject(response.body()).getJSONArray("data");
            if (array.length() == 0) {
                return null;
            }

            return array.getJSONObject(0);
        } catch (Exception ignored) {
            // Do nothing
        }

        return null;
    }
}
