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
    private final com.moneydance.modules.features.mcpserver.resources.ResourceRegistry resourceRegistry;

    public McpProtocolHandler(Main extension, ToolRegistry toolRegistry, com.moneydance.modules.features.mcpserver.resources.ResourceRegistry resourceRegistry) {
        this.extension = extension;
        this.toolRegistry = toolRegistry;
        this.resourceRegistry = resourceRegistry;
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
                case "resources/list" -> handleResourcesList(id);
                case "resources/read" -> handleResourcesRead(id, requestBody);
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
                    .put("listChanged", false))
                .putObject("resources", new JsonObjectBuilder()
                    .put("listChanged", false)
                    .put("subscribe", false)))
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

    private String handleResourcesList(String id) {
        com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder resourcesArray = new com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder();
        for (com.moneydance.modules.features.mcpserver.resources.McpResource resource : resourceRegistry.getAllResources()) {
            resourcesArray.addObject(new JsonObjectBuilder()
                .put("uri", resource.getUri())
                .put("name", resource.getName())
                .put("description", resource.getDescription())
                .put("mimeType", resource.getMimeType()));
        }

        String result = new JsonObjectBuilder()
            .putArray("resources", resourcesArray)
            .build();
        return jsonRpcResult(id, result);
    }

    private String handleResourcesRead(String id, String requestBody) {
        String paramsBlock = JsonParser.getValue(requestBody, "params");
        String uri = JsonParser.getString(paramsBlock, "uri");

        if (uri == null) {
            return jsonRpcError(id, -32602, "Invalid params: missing uri");
        }

        com.moneydance.modules.features.mcpserver.resources.McpResource resource = resourceRegistry.getResource(uri);
        if (resource == null) {
            return jsonRpcError(id, -32602, "Unknown resource: " + uri);
        }

        try {
            String content = resource.read(extension.getMDContext());
            com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder contentsArray = new com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder();
            contentsArray.addObject(new JsonObjectBuilder()
                .put("uri", resource.getUri())
                .put("mimeType", resource.getMimeType())
                .put("text", content));

            String result = new JsonObjectBuilder()
                .putArray("contents", contentsArray)
                .build();
            return jsonRpcResult(id, result);
        } catch (Exception e) {
            return jsonRpcError(id, -32603, "Resource read error: " + e.getMessage());
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
