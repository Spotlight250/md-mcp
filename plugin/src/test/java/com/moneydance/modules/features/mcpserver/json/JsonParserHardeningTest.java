package com.moneydance.modules.features.mcpserver.json;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class JsonParserHardeningTest {

    @Test
    public void testRecursionLimit() {
        // Create a deeply nested JSON string (100 levels)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) sb.append("{\"a\":");
        sb.append("1");
        for (int i = 0; i < 100; i++) sb.append("}");
        
        String deepJson = sb.toString();
        
        // This should fail with a RuntimeException (our depth limit is 50)
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            JsonParser.parse(deepJson);
        });
        assertEquals("Maximum JSON nesting depth exceeded", ex.getMessage());
    }

    @Test
    public void testParseStringEscaping() {
        // JSON: {"key":"value with \" and \\"}
        // Expected data: value with " and \
        String json = "{\"key\":\"value with \\\" and \\\\\"}";
        Object parsed = JsonParser.parse(json);
        assertTrue(parsed instanceof Map);
        assertEquals("value with \" and \\", ((Map<?, ?>) parsed).get("key"));
    }

    @Test
    public void testStringifyEscaping() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value with \" and \\");
        
        String json = JsonParser.stringify(data);
        assertEquals("{\"key\":\"value with \\\" and \\\\\"}", json);
    }

    @Test
    public void testEscapeSpecialChars() {
        assertEquals("line\\nbreak", JsonParser.escape("line\nbreak"));
        assertEquals("tab\\tchar", JsonParser.escape("tab\tchar"));
        assertEquals("back\\\\slash", JsonParser.escape("back\\slash"));
        assertEquals("quote\\\"char", JsonParser.escape("quote\"char"));
    }
}
