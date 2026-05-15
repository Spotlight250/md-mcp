package com.moneydance.modules.features.mcpserver;

import java.io.*;
import java.nio.file.*;

/**
 * Handles the deployment of the bridge script.
 */
public class BridgeManager {

    private static final String BRIDGE_FILENAME = "mcp-proxy.mjs";

    /**
     * Deploys the bridge script to the user's .moneydance directory.
     * @return The absolute path to the deployed script.
     */
    public String deployBridge() throws IOException {
        File mcpDir = new File(System.getProperty("user.home"), ".moneydance/mcp");
        if (!mcpDir.exists()) {
            mcpDir.mkdirs();
        }

        File targetFile = new File(mcpDir, BRIDGE_FILENAME);
        try (InputStream in = getClass().getResourceAsStream(BRIDGE_FILENAME)) {
            if (in == null) {
                throw new IOException("Could not find bridge script in resources: " + BRIDGE_FILENAME);
            }
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        
        return targetFile.getAbsolutePath();
    }

    /**
     * Generates the JSON-RPC configuration block for Claude Desktop.
     */
    public String getClaudeConfigJson(String scriptPath) {
        String escapedPath = scriptPath.replace("\\", "/");
        return "{\n" +
               "  \"mcpServers\": {\n" +
               "    \"moneydance\": {\n" +
               "      \"command\": \"node\",\n" +
               "      \"args\": [\n" +
               "        \"" + escapedPath + "\"\n" +
               "      ]\n" +
               "    }\n" +
               "  }\n" +
               "}";
    }
}
