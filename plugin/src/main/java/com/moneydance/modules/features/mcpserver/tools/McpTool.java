package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;

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
}
