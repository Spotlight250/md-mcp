package com.moneydance.modules.features.mcpserver.json;

import java.util.ArrayList;
import java.util.List;

public class JsonObjectBuilder {
    private final List<String> entries = new ArrayList<>();

    public JsonObjectBuilder put(String key, String value) {
        entries.add("\"" + escape(key) + "\":\"" + escape(value) + "\"");
        return this;
    }

    public JsonObjectBuilder put(String key, Number value) {
        entries.add("\"" + escape(key) + "\":" + value);
        return this;
    }

    public JsonObjectBuilder put(String key, boolean value) {
        entries.add("\"" + escape(key) + "\":" + value);
        return this;
    }

    public JsonObjectBuilder putNull(String key) {
        entries.add("\"" + escape(key) + "\":null");
        return this;
    }

    public JsonObjectBuilder putObject(String key, JsonObjectBuilder builder) {
        entries.add("\"" + escape(key) + "\":" + builder.build());
        return this;
    }

    public JsonObjectBuilder putArray(String key, JsonArrayBuilder builder) {
        entries.add("\"" + escape(key) + "\":" + builder.build());
        return this;
    }

    public JsonObjectBuilder putRaw(String key, String rawJson) {
        entries.add("\"" + escape(key) + "\":" + rawJson);
        return this;
    }

    public String build() {
        return "{" + String.join(",", entries) + "}";
    }

    private String escape(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String hex = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(hex.substring(hex.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
