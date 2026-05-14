package com.moneydance.modules.features.mcpserver.json;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JsonParserTest {

    @Test
    void testJsonParserWithNotification() {
        String request = "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}";
        assertEquals("notifications/initialized", JsonParser.getString(request, "method"));
        assertNull(JsonParser.getValue(request, "id"));
    }

    @Test
    void testGetString() {
        String json = "{\"name\": \"Alice\", \"age\": 30}";
        assertEquals("Alice", JsonParser.getString(json, "name"));
        assertEquals("30", JsonParser.getString(json, "age"));
    }

    @Test
    void testGetStringWithEscapedChars() {
        String json = "{\"text\": \"Line 1\\nLine 2\\t\\\"Quote\\\"\"}";
        assertEquals("Line 1\nLine 2\t\"Quote\"", JsonParser.getString(json, "text"));
    }

    @Test
    void testGetStringWithEscapedBackslash() {
        String json = "{\"a\": \"ends with backslash \\\\\", \"b\": \"correct\"}";
        assertEquals("ends with backslash \\", JsonParser.getString(json, "a"));
        assertEquals("correct", JsonParser.getString(json, "b"));
    }

    @Test
    void testGetValue() {
        String json = "{\"params\": {\"id\": 123, \"tags\": [\"a\", \"b\"]}}";
        String params = JsonParser.getValue(json, "params");
        assertNotNull(params);
        assertTrue(params.startsWith("{"));
        assertTrue(params.contains("\"id\":123"));
        assertTrue(params.contains("\"tags\":[\"a\",\"b\"]"));
    }

    @Test
    void testNestedObjectsWithSameKey() {
        String json = "{\"outer\": {\"b\": \"inner\"}, \"b\": \"outer\"}";
        assertEquals("outer", JsonParser.getString(json, "b"));
    }
}
