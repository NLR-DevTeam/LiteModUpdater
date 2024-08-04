package cn.xiaym.modupdater.utils;

public class JSONFormatter {
    /**
     * Formats the JSON String with specified indents.
     *
     * @param jsonString Raw JSON String
     * @param indent     Indents
     * @return Formatted JSON String
     * @author XIAYM-gh
     */
    public static String format(String jsonString, int indent) {
        char[] json = jsonString.toCharArray();
        int currentIndent = 0;
        boolean beginning = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < json.length; i++) {
            char token = json[i];

            switch (token) {
                case '{', '[' -> {
                    if (beginning) {
                        sb.append(" ".repeat(currentIndent));
                    }

                    while (Character.isWhitespace(json[i + 1])) {
                        i++;
                    }

                    if (json[i + 1] == '}' || json[i + 1] == ']') {
                        sb.append(token).append(json[i + 1]);
                        i++;
                    } else {
                        sb.append(token).append("\n");
                        currentIndent += indent;
                    }

                    beginning = true;
                }

                case '}', ']' -> {
                    currentIndent -= indent;
                    sb.append("\n").append(" ".repeat(currentIndent)).append(token);
                }

                case ',' -> {
                    sb.append(",\n");
                    beginning = true;
                }

                case ':' -> sb.append(": ");

                default -> {
                    if (beginning) {
                        sb.append(" ".repeat(currentIndent));
                        beginning = false;
                    }

                    if (Character.isWhitespace(token)) {
                        continue;
                    }

                    sb.append(token);
                }
            }
        }

        return sb.toString();
    }
}

