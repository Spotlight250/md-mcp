package com.moneydance.modules.features.mcpserver.json;

import java.util.ArrayList;
import java.util.List;

public class JsonArrayBuilder {
    private final List<String> elements = new ArrayList<>();

    public int size() {
        return elements.size();
    }

    public JsonArrayBuilder add(String value) {
        elements.add("\"" + escape(value) + "\"");
        return this;
    }

    public JsonArrayBuilder add(Number value) {
        elements.add(String.valueOf(value));
        return this;
    }

    public JsonArrayBuilder add(boolean value) {
        elements.add(String.valueOf(value));
        return this;
    }

    public JsonArrayBuilder addNull() {
        elements.add("null");
        return this;
    }

    public JsonArrayBuilder addObject(JsonObjectBuilder builder) {
        elements.add(builder.build());
        return this;
    }

    public JsonArrayBuilder addArray(JsonArrayBuilder builder) {
        elements.add(builder.build());
        return this;
    }

    public JsonArrayBuilder addRaw(String rawJson) {
        elements.add(rawJson);
        return this;
    }

    public String build() {
        return "[" + String.join(",", elements) + "]";
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
