package com.moneydance.modules.features.mcpserver;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonParser;
import com.moneydance.modules.features.mcpserver.tools.McpTool;
import com.moneydance.modules.features.mcpserver.tools.ToolRegistry;

/**
 * Implements the MCP JSON-RPC protocol using a ToolRegistry.
 */
public class McpProtocolHandler {

    private static final String PROTOCOL_VERSION = "2024-11-05";
    private static final String SERVER_NAME = "moneydance-mcp";
    private static final String SERVER_VERSION = "0.1.0";

    private final Main extension;
    private final ToolRegistry toolRegistry;

    public McpProtocolHandler(Main extension, ToolRegistry toolRegistry) {
        this.extension = extension;
        this.toolRegistry = toolRegistry;
    }

    public String handleRequest(String requestBody) {
        try {
            String method = JsonParser.getString(requestBody, "method");
            String id = JsonParser.getValue(requestBody, "id");

            if (id == null || id.isEmpty() || "null".equals(id)) {
                McpLogger.log("Notification received: " + method);
                return "";
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
            McpLogger.log("Error handling request: " + e.getMessage());
            return jsonRpcError("null", -32603, "Internal error: " + e.getMessage());
        }
    }

    private String handleInitialize(String id) {
        String result = new JsonObjectBuilder()
            .put("protocolVersion", PROTOCOL_VERSION)
            .putObject("capabilities", new JsonObjectBuilder()
                .putObject("tools", new JsonObjectBuilder()
                    .put("listChanged", false)))
            .putObject("serverInfo", new JsonObjectBuilder()
                .put("name", SERVER_NAME)
                .put("version", SERVER_VERSION))
            .build();
        return jsonRpcResult(id, result);
    }

    private String handlePing(String id) {
        return jsonRpcResult(id, "{}");
    }

    private String handleToolsList(String id) {
        return jsonRpcResult(id, toolRegistry.listToolsJson());
    }

    private String handleToolsCall(String id, String requestBody) {
        String paramsBlock = JsonParser.getValue(requestBody, "params");
        String toolName = JsonParser.getString(paramsBlock, "name");
        String toolParams = JsonParser.getValue(paramsBlock, "arguments");

        if (toolName == null) {
            return jsonRpcError(id, -32602, "Invalid params: missing tool name");
        }

        McpTool tool = toolRegistry.getTool(toolName);
        if (tool == null) {
            return jsonRpcError(id, -32602, "Unknown tool: " + toolName);
        }

        try {
            FeatureModuleContext ctx = extension.getMDContext();
            String result = tool.execute(toolParams, ctx);
            return jsonRpcResult(id, result);
        } catch (Exception e) {
            return jsonRpcError(id, -32603, "Tool execution error: " + e.getMessage());
        }
    }

    // ---- JSON-RPC helpers ----

    private String jsonRpcResult(String id, String result) {
        return "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":" + result + "}";
    }

    private String jsonRpcError(String id, int code, String message) {
        String error = new JsonObjectBuilder()
            .put("code", code)
            .put("message", message)
            .build();
        return "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"error\":" + error + "}";
    }
}
