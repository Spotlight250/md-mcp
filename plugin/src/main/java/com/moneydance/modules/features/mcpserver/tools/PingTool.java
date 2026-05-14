package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.apps.md.controller.FeatureModuleContext;
import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

/**
 * Implementation of the 'ping' tool.
 */
public class PingTool implements McpTool {
    private final String serverVersion;

    public PingTool(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return "Test connectivity to Moneydance. Returns server status, Moneydance version, and the currently open data file name.";
    }

    @Override
    public String getInputSchema() {
        return "{\"type\":\"object\",\"properties\":{},\"required\":[]}";
    }

    @Override
    public String execute(String paramsJson, FeatureModuleContext ctx) {
        String mdVersion = "unknown";
        String dataFile = "none";

        if (ctx != null) {
            try {
                mdVersion = String.valueOf(ctx.getBuild());
            } catch (Exception e) {}

            try {
                var currentAccount = ctx.getCurrentAccountBook();
                if (currentAccount != null) {
                    dataFile = currentAccount.getName();
                }
            } catch (Exception e) {}
        }

        String resultJson = new JsonObjectBuilder()
            .put("status", "pong")
            .put("moneydance_build", mdVersion)
            .put("data_file", dataFile)
            .put("server_version", serverVersion)
            .build();

        // Return the tool result in the MCP content format
        return new JsonObjectBuilder()
            .putArray("content", new JsonArrayBuilder()
                .addObject(new JsonObjectBuilder()
                    .put("type", "text")
                    .put("text", resultJson)))
            .put("isError", false)
            .build();
    }
}
