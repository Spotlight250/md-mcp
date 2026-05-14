package com.moneydance.modules.features.mcpserver;

import com.moneydance.apps.md.controller.FeatureModule;
import com.moneydance.apps.md.controller.FeatureModuleContext;

/**
 * Moneydance extension entry point.
 * 
 * Lifecycle:
 *   init()        — called when the extension is loaded
 *   handleEvent() — called on MD events (file open/close, etc.)
 *   cleanup()     — called when the extension is being removed
 *   unload()      — called when MD is shutting down
 *
 * This extension starts an MCP (Model Context Protocol) server on localhost
 * when a data file is opened, allowing AI agents to query financial data.
 */
public class Main extends FeatureModule {

    private static final String EXTN_ID = "mcpserver";
    private static final String EXTN_NAME = "MCP Server";

    private McpServerManager serverManager;

    @Override
    public void init() {
        log("Initializing " + EXTN_NAME + " extension (build " + getBuild() + ")");

        // Register a toolbar feature so users can manually trigger the server
        FeatureModuleContext context = getContext();
        try {
            context.registerFeature(this, "toggleserver", null, EXTN_NAME);
        } catch (Exception e) {
            log("Failed to register feature: " + e.getMessage());
        }

        serverManager = new McpServerManager(this);
    }

    @Override
    public void handleEvent(String appEvent) {
        log("Event: " + appEvent);

        switch (appEvent) {
            case "md:file:opened":
                // Data file is now open — start the MCP server
                serverManager.start();
                break;

            case "md:file:closing":
                // Data file is about to close — stop the MCP server
                serverManager.stop();
                break;

            default:
                // Ignore other events
                break;
        }
    }

    @Override
    public void invoke(String uri) {
        String command = uri;
        int colonIdx = uri.indexOf(':');
        if (colonIdx >= 0) {
            command = uri.substring(0, colonIdx);
        }

        log("Invoked with command: " + command);

        if ("toggleserver".equals(command)) {
            if (serverManager.isRunning()) {
                serverManager.stop();
            } else {
                serverManager.start();
            }
        }
    }

    @Override
    public String getName() {
        return EXTN_NAME;
    }

    @Override
    public void cleanup() {
        log("cleanup() called");
        unload();
    }

    @Override
    public void unload() {
        log("unload() called — shutting down MCP server");
        if (serverManager != null) {
            serverManager.stop();
        }
    }

    // --- Helpers ---

    /**
     * Returns the Moneydance context, exposed for use by the server manager.
     */
    FeatureModuleContext getMDContext() {
        return getContext();
    }

    /**
     * Log to the Moneydance console.
     */
    static void log(String message) {
        McpLogger.log(message);
    }
}
