package com.moneydance.modules.features.mcpserver.resources;

import com.moneydance.apps.md.controller.FeatureModuleContext;

/**
 * Interface for all MCP Resource implementations.
 */
public interface McpResource {
    /**
     * @return The unique URI for this resource (e.g., mcp://moneydance/accounts/hierarchy)
     */
    String getUri();

    /**
     * @return A human-readable name for the resource.
     */
    String getName();

    /**
     * @return A description of what this resource contains.
     */
    String getDescription();

    /**
     * @return The MIME type of the resource (usually application/json).
     */
    String getMimeType();

    /**
     * Reads the resource content.
     * @param context The Moneydance context.
     * @return The content string.
     */
    String read(FeatureModuleContext context);
}
