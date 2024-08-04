package cn.xiaym.modupdater.utils;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class QueryBuilder {
    private final String rawURL;
    private final StringBuilder query = new StringBuilder();

    public QueryBuilder(String rawURL) {
        if (rawURL.contains("?")) {
            throw new IllegalArgumentException("Raw URL cannot contain query string.");
        }

        this.rawURL = rawURL;
    }

    private static String encodeURL(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }

    public QueryBuilder addQuery(String key, Object value) {
        if (!query.isEmpty()) {
            query.append("&");
        }

        query.append(encodeURL(key)).append("=").append(encodeURL(value.toString()));
        return this;
    }

    public URI build() {
        return URI.create(rawURL + "?" + query);
    }
}
