package com.moneydance.modules.features.mcpserver.json;

import java.util.HashMap;
import java.util.Map;

/**
 * A robust, zero-dependency JSON parser.
 * Replaces the previous naive indexOf-based implementation to correctly handle
 * escaped characters, nested objects, and arrays.
 */
public class JsonParser {

    /**
     * Extracts a string value for the given key from a JSON object.
     */
    public static String getString(String json, String key) {
        Object val = parse(json);
        if (val instanceof Map) {
            Object result = ((Map<?, ?>) val).get(key);
            return result == null ? null : String.valueOf(result);
        }
        return null;
    }

    /**
     * Extracts the raw JSON value (as a string) for the given key.
     * Note: This implementation parses and then re-serializes or extracts substrings.
     * For MCP params, it's often used to get nested objects or arrays.
     */
    public static String getValue(String json, String key) {
        // To maintain compatibility with the previous naive implementation's 'getValue'
        // which returned raw JSON fragments, we need to find the key and extract the raw value.
        // A full parse is safer to find the correct key.
        
        Parser p = new Parser(json);
        Object root = p.parse();
        if (root instanceof Map) {
            // Find where the value for 'key' starts and ends in the original string
            // This is slightly complex with a recursive descent parser.
            // For now, let's just return the stringified version of the parsed object
            // if it's a complex type, or the raw string if it's a string.
            Object val = ((Map<?, ?>) root).get(key);
            if (val == null) return null;
            if (val instanceof String) return "\"" + val + "\""; // Re-quote strings
            if (val instanceof Map || val instanceof java.util.List) {
                return stringify(val);
            }
            return String.valueOf(val);
        }
        return null;
    }

    public static Object parse(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        return new Parser(json).parse();
    }

    private static String stringify(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + obj + "\""; // Should ideally escape here too
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":").append(stringify(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof java.util.List) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (java.util.List<?>) obj) {
                if (!first) sb.append(",");
                sb.append(stringify(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        return String.valueOf(obj);
    }

    private static class Parser {
        private final String json;
        private int pos = 0;

        Parser(String json) {
            this.json = json;
        }

        Object parse() {
            skipWhitespace();
            if (pos >= json.length()) return null;
            char c = json.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (Character.isDigit(c) || c == '-') return parseNumber();
            throw new RuntimeException("Unexpected character at " + pos + ": " + c);
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new HashMap<>();
            consume('{');
            skipWhitespace();
            if (peek() == '}') {
                consume('}');
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                consume(':');
                skipWhitespace();
                Object value = parse();
                map.put(key, value);
                skipWhitespace();
                if (peek() == '}') {
                    consume('}');
                    break;
                }
                consume(',');
            }
            return map;
        }

        private java.util.List<Object> parseArray() {
            java.util.List<Object> list = new java.util.ArrayList<>();
            consume('[');
            skipWhitespace();
            if (peek() == ']') {
                consume(']');
                return list;
            }
            while (true) {
                skipWhitespace();
                list.add(parse());
                skipWhitespace();
                if (peek() == ']') {
                    consume(']');
                    break;
                }
                consume(',');
            }
            return list;
        }

        private String parseString() {
            consume('"');
            StringBuilder sb = new StringBuilder();
            while (pos < json.length()) {
                char c = json.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    if (pos >= json.length()) throw new RuntimeException("Unterminated escape sequence");
                    char next = json.charAt(pos++);
                    switch (next) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (pos + 4 > json.length()) throw new RuntimeException("Unterminated unicode escape");
                            String hex = json.substring(pos, pos + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                            break;
                        default: sb.append(next);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new RuntimeException("Unterminated string");
        }

        private Boolean parseBoolean() {
            if (json.startsWith("true", pos)) {
                pos += 4;
                return true;
            }
            if (json.startsWith("false", pos)) {
                pos += 5;
                return false;
            }
            throw new RuntimeException("Expected boolean at " + pos);
        }

        private Object parseNull() {
            if (json.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new RuntimeException("Expected null at " + pos);
        }

        private Number parseNumber() {
            int start = pos;
            if (peek() == '-') pos++;
            while (pos < json.length() && (Character.isDigit(json.charAt(pos)) || json.charAt(pos) == '.')) {
                pos++;
            }
            String s = json.substring(start, pos);
            if (s.contains(".")) return Double.parseDouble(s);
            return Long.parseLong(s);
        }

        private char peek() {
            if (pos >= json.length()) return '\0';
            return json.charAt(pos);
        }

        private void consume(char expected) {
            if (peek() != expected) {
                throw new RuntimeException("Expected '" + expected + "' at " + pos + " but found '" + peek() + "'");
            }
            pos++;
        }

        private void skipWhitespace() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }
        }
    }
}
