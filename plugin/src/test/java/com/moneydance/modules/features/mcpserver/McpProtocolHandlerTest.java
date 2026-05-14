package com.moneydance.modules.features.mcpserver;

import com.moneydance.modules.features.mcpserver.json.JsonParser;
import com.moneydance.modules.features.mcpserver.tools.ToolRegistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class McpProtocolHandlerTest {

    @Test
    void testHandlePing() {
        McpProtocolHandler handler = new McpProtocolHandler(null, new ToolRegistry());
        String request = "{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":1}";
        String response = handler.handleRequest(request);
        
        assertEquals("2.0", JsonParser.getString(response, "jsonrpc"));
        assertEquals("1", JsonParser.getValue(response, "id"));
        assertNotNull(JsonParser.getValue(response, "result"));
    }

    @Test
    void testHandleInitialize() {
        McpProtocolHandler handler = new McpProtocolHandler(null, new ToolRegistry());
        String request = "{\"jsonrpc\":\"2.0\",\"method\":\"initialize\",\"id\":\"abc\"}";
        String response = handler.handleRequest(request);
        
        assertEquals("\"abc\"", JsonParser.getValue(response, "id"));
        String result = JsonParser.getValue(response, "result");
        assertEquals("2024-11-05", JsonParser.getString(result, "protocolVersion"));
    }

    @Test
    void testHandleUnknownMethod() {
        McpProtocolHandler handler = new McpProtocolHandler(null, new ToolRegistry());
        String request = "{\"jsonrpc\":\"2.0\",\"method\":\"unknown\",\"id\":99}";
        String response = handler.handleRequest(request);
        
        String error = JsonParser.getValue(response, "error");
        assertNotNull(error);
        assertEquals("-32601", JsonParser.getValue(error, "code"));
    }

    @Test
    void testHandleNotification() {
        McpProtocolHandler handler = new McpProtocolHandler(null, new ToolRegistry());
        String request = "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}";
        String response = handler.handleRequest(request);
        assertNotNull(response);
        assertEquals(0, response.length());
    }
}
