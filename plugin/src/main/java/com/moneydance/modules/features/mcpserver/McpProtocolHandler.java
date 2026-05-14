package com.moneydance.modules.features.mcpserver;

import com.moneydance.apps.md.controller.FeatureModuleContext;

/**
 * Implements the MCP JSON-RPC protocol.
 *
 * We implement just enough of the MCP spec to handle:
 *   - initialize (client handshake)
 *   - notifications/initialized (client ack — ignored)
 *   - tools/list (discover tools)
 *   - tools/call (execute a tool)
 *   - ping (keepalive)
 *
 * JSON is handled with simple string manipulation to avoid pulling in
 * Jackson/Gson — keeping the plugin dependency-free.
 *
 * MCP Spec: https://spec.modelcontextprotocol.io/
 */
public class McpProtocolHandler {

    private static final String PROTOCOL_VERSION = "2024-11-05";
    private static final String SERVER_NAME = "moneydance-mcp";
    private static final String SERVER_VERSION = "0.1.0";

    private final Main extension;

    public McpProtocolHandler(Main extension) {
        this.extension = extension;
    }

    /**
     * Parse a JSON-RPC request and route to the appropriate handler.
     * Returns the JSON-RPC response string.
     */
    public String handleRequest(String requestBody) {
        try {
            String method = extractJsonString(requestBody, "method");
            String id = extractJsonValue(requestBody, "id");

            // Notifications have no id — just acknowledge them
            if (id == null || id.isEmpty() || "null".equals(id)) {
                // This is a notification (e.g., notifications/initialized)
                Main.log("Notification received: " + method);
                return ""; // No response for notifications
            }

            if (method == null) {
                return jsonRpcError(id, -32600, "Invalid request: missing method");
            }

            return switch (method) {
                case "initialize" -> handleInitialize(id);
                case "ping" -> handlePing(id);
                case "tools/list" -> handleToolsList(id);
                case "tools/call" -> handleToolsCall(id, requestBody);
                default -> jsonRpcError(id, -32601, "Method not found: " + method);
            };
        } catch (Exception e) {
            Main.log("Error handling request: " + e.getMessage());
            e.printStackTrace(System.err);
            return jsonRpcError("null", -32603, "Internal error: " + e.getMessage());
        }
    }

    /**
     * Handle the MCP initialize handshake.
     * Returns server capabilities and protocol version.
     */
    private String handleInitialize(String id) {
        return jsonRpcResult(id, """
            {
                "protocolVersion": "%s",
                "capabilities": {
                    "tools": {
                        "listChanged": false
                    }
                },
                "serverInfo": {
                    "name": "%s",
                    "version": "%s"
                }
            }
            """.formatted(PROTOCOL_VERSION, SERVER_NAME, SERVER_VERSION));
    }

    /**
     * Handle ping — simple keepalive.
     */
    private String handlePing(String id) {
        return jsonRpcResult(id, "{}");
    }

    /**
     * Handle tools/list — return available tools and their schemas.
     */
    private String handleToolsList(String id) {
        return jsonRpcResult(id, """
            {
                "tools": [
                    {
                        "name": "ping",
                        "description": "Test connectivity to Moneydance. Returns server status, Moneydance version, and the currently open data file name.",
                        "inputSchema": {
                            "type": "object",
                            "properties": {},
                            "required": []
                        }
                    }
                ]
            }
            """);
    }

    /**
     * Handle tools/call — execute a tool by name.
     */
    private String handleToolsCall(String id, String requestBody) {
        // Extract tool name from params.name
        String paramsBlock = extractJsonObject(requestBody, "params");
        String toolName = extractJsonString(paramsBlock, "name");

        if (toolName == null) {
            return jsonRpcError(id, -32602, "Invalid params: missing tool name");
        }

        return switch (toolName) {
            case "ping" -> executePingTool(id);
            default -> jsonRpcError(id, -32602, "Unknown tool: " + toolName);
        };
    }

    /**
     * Execute the 'ping' tool — returns server status and MD metadata.
     */
    private String executePingTool(String id) {
        String status = "pong";
        String mdVersion = "unknown";
        String dataFile = "none";

        try {
            FeatureModuleContext ctx = extension.getMDContext();
            if (ctx != null) {
                // Try to get the Moneydance version
                try {
                    mdVersion = String.valueOf(ctx.getBuild());
                } catch (Exception e) {
                    mdVersion = "error: " + e.getMessage();
                }

                // Try to get the current data file name
                try {
                    var currentAccount = ctx.getCurrentAccountBook();
                    if (currentAccount != null) {
                        dataFile = currentAccount.getName();
                    }
                } catch (Exception e) {
                    dataFile = "error: " + e.getMessage();
                }
            }
        } catch (Exception e) {
            Main.log("Error getting MD context: " + e.getMessage());
        }

        String content = """
            {
                "status": "%s",
                "moneydance_build": "%s",
                "data_file": "%s",
                "server_version": "%s"
            }
            """.formatted(
                escapeJson(status),
                escapeJson(mdVersion),
                escapeJson(dataFile),
                escapeJson(SERVER_VERSION)
            );

        return jsonRpcResult(id, """
            {
                "content": [
                    {
                        "type": "text",
                        "text": "%s"
                    }
                ],
                "isError": false
            }
            """.formatted(escapeJson(content.strip())));
    }

    // ---- JSON-RPC helpers (minimal, no-dependency JSON) ----

    private String jsonRpcResult(String id, String result) {
        return """
            {"jsonrpc": "2.0", "id": %s, "result": %s}
            """.formatted(id, result.strip()).strip();
    }

    private String jsonRpcError(String id, int code, String message) {
        return """
            {"jsonrpc": "2.0", "id": %s, "error": {"code": %d, "message": "%s"}}
            """.formatted(id, code, escapeJson(message)).strip();
    }

    /**
     * Extract a string value from a JSON object (very naive parser).
     * Looks for "key": "value" patterns.
     */
    static String extractJsonString(String json, String key) {
        if (json == null) return null;
        String pattern = "\"" + key + "\"";
        int keyIdx = json.indexOf(pattern);
        if (keyIdx < 0) return null;

        int colonIdx = json.indexOf(':', keyIdx + pattern.length());
        if (colonIdx < 0) return null;

        // Find the opening quote of the value
        int openQuote = json.indexOf('"', colonIdx + 1);
        if (openQuote < 0) return null;

        // Find the closing quote (handle escaped quotes)
        int closeQuote = openQuote + 1;
        while (closeQuote < json.length()) {
            if (json.charAt(closeQuote) == '"' && json.charAt(closeQuote - 1) != '\\') {
                break;
            }
            closeQuote++;
        }
        if (closeQuote >= json.length()) return null;

        return json.substring(openQuote + 1, closeQuote);
    }

    /**
     * Extract any JSON value (string, number, object, array, bool, null) for a key.
     * Returns the raw value string.
     */
    static String extractJsonValue(String json, String key) {
        if (json == null) return null;
        String pattern = "\"" + key + "\"";
        int keyIdx = json.indexOf(pattern);
        if (keyIdx < 0) return null;

        int colonIdx = json.indexOf(':', keyIdx + pattern.length());
        if (colonIdx < 0) return null;

        // Skip whitespace after colon
        int valueStart = colonIdx + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        if (valueStart >= json.length()) return null;

        char first = json.charAt(valueStart);

        if (first == '"') {
            // String value — find closing quote
            int closeQuote = valueStart + 1;
            while (closeQuote < json.length()) {
                if (json.charAt(closeQuote) == '"' && json.charAt(closeQuote - 1) != '\\') {
                    break;
                }
                closeQuote++;
            }
            return json.substring(valueStart, closeQuote + 1);
        } else if (first == '{' || first == '[') {
            // Object or array — need bracket matching
            return extractBracketedValue(json, valueStart, first);
        } else {
            // Number, boolean, or null — read until delimiter
            int valueEnd = valueStart;
            while (valueEnd < json.length() && !isJsonDelimiter(json.charAt(valueEnd))) {
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd).trim();
        }
    }

    /**
     * Extract a JSON object value for a key. Returns the object as a string including braces.
     */
    static String extractJsonObject(String json, String key) {
        String value = extractJsonValue(json, key);
        if (value != null && value.startsWith("{")) {
            return value;
        }
        return null;
    }

    private static String extractBracketedValue(String json, int start, char openBracket) {
        char closeBracket = openBracket == '{' ? '}' : ']';
        int depth = 0;
        boolean inString = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                if (c == '"' && json.charAt(i - 1) != '\\') {
                    inString = false;
                }
            } else {
                if (c == '"') {
                    inString = true;
                } else if (c == openBracket) {
                    depth++;
                } else if (c == closeBracket) {
                    depth--;
                    if (depth == 0) {
                        return json.substring(start, i + 1);
                    }
                }
            }
        }
        return null;
    }

    private static boolean isJsonDelimiter(char c) {
        return c == ',' || c == '}' || c == ']' || Character.isWhitespace(c);
    }

    private static String escapeJson(String value) {
        if (value == null) return "null";
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
