package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonObjectBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for all available MCP tools.
 */
public class ToolRegistry {
    private final Map<String, McpTool> tools = new HashMap<>();

    public void registerTool(McpTool tool) {
        tools.put(tool.getName(), tool);
    }

    public McpTool getTool(String name) {
        return tools.get(name);
    }

    /**
     * @return The "tools" array part of the tools/list response.
     */
    public String listToolsJson() {
        JsonArrayBuilder arrayBuilder = new JsonArrayBuilder();
        for (McpTool tool : tools.values()) {
            JsonObjectBuilder toolObj = new JsonObjectBuilder()
                .put("name", tool.getName())
                .put("description", tool.getDescription());
            
            // Note: inputSchema is already a JSON string, but JsonObjectBuilder 
            // put(String, String) will quote it as a string. 
            // We need a way to put a raw JSON value.
            // I'll add putRaw to JsonObjectBuilder.
            toolObj.putRaw("inputSchema", tool.getInputSchema());
            
            arrayBuilder.addObject(toolObj);
        }
        
        return new JsonObjectBuilder()
            .putArray("tools", arrayBuilder)
            .build();
    }
}
