package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

/**
 * Interface for all MCP tools.
 */
public interface McpTool {
    /**
     * @return The unique name of the tool (e.g., "get_accounts").
     */
    String getName();

    /**
     * @return A human-readable description of what the tool does.
     */
    String getDescription();

    /**
     * @return The JSON schema for the tool's input parameters.
     */
    String getInputSchema();

    /**
     * Executes the tool.
     * @param paramsJson The JSON string containing the tool's parameters.
     * @param context The Moneydance feature module context.
     * @return The JSON result of the tool execution.
     */
    String execute(String paramsJson, FeatureModuleContext context);

    /**
     * Helper to create a standardized error response.
     */
    default String errorResponse(String message) {
        return new JsonObjectBuilder()
            .putArray("content", new JsonArrayBuilder()
                .addObject(new JsonObjectBuilder()
                    .put("type", "text")
                    .put("text", "Error: " + message)))
            .put("isError", true)
            .build();
    }
}
