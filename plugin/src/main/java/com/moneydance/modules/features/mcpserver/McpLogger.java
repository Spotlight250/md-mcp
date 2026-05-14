package com.moneydance.modules.features.mcpserver;

/**
 * Simple logger that doesn't depend on Moneydance classes,
 * safe for use in unit tests.
 */
public class McpLogger {
    public static void log(String message) {
        System.err.println("[MCP Server] " + message);
    }
}
