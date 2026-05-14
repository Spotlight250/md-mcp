package com.moneydance.modules.features.mcpserver.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages registration and lookup of MCP Resources.
 */
public class ResourceRegistry {
    private final Map<String, McpResource> resources = new HashMap<>();

    public void registerResource(McpResource resource) {
        resources.put(resource.getUri(), resource);
    }

    public List<McpResource> getAllResources() {
        return new ArrayList<>(resources.values());
    }

    public McpResource getResource(String uri) {
        return resources.get(uri);
    }
}
