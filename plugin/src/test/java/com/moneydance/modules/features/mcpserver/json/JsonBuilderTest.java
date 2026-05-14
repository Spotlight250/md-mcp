package com.moneydance.modules.features.mcpserver.json;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonBuilderTest {

    @Test
    void testEmptyObject() {
        JsonObjectBuilder builder = new JsonObjectBuilder();
        assertEquals("{}", builder.build());
    }

    @Test
    void testSimpleObject() {
        JsonObjectBuilder builder = new JsonObjectBuilder()
            .put("name", "Alice")
            .put("age", 30)
            .put("isActive", true);
        
        String expected = "{\"name\":\"Alice\",\"age\":30,\"isActive\":true}";
        assertEquals(expected, builder.build());
    }

    @Test
    void testStringEscaping() {
        JsonObjectBuilder builder = new JsonObjectBuilder()
            .put("text", "Line 1\nLine 2\t\"Quoted\" \\Slash");
            
        String expected = "{\"text\":\"Line 1\\nLine 2\\t\\\"Quoted\\\" \\\\Slash\"}";
        assertEquals(expected, builder.build());
    }

    @Test
    void testNullValues() {
        JsonObjectBuilder builder = new JsonObjectBuilder()
            .put("name", "Bob")
            .putNull("nickname");
            
        String expected = "{\"name\":\"Bob\",\"nickname\":null}";
        assertEquals(expected, builder.build());
    }

    @Test
    void testEmptyArray() {
        JsonArrayBuilder builder = new JsonArrayBuilder();
        assertEquals("[]", builder.build());
    }

    @Test
    void testArrayOfPrimitives() {
        JsonArrayBuilder builder = new JsonArrayBuilder()
            .add("apple")
            .add(42)
            .add(false)
            .addNull();
            
        String expected = "[\"apple\",42,false,null]";
        assertEquals(expected, builder.build());
    }

    @Test
    void testNestedObjectsAndArrays() {
        JsonObjectBuilder builder = new JsonObjectBuilder()
            .put("user", "Admin")
            .putArray("roles", new JsonArrayBuilder().add("read").add("write"))
            .putObject("meta", new JsonObjectBuilder().put("id", 123));
            
        String expected = "{\"user\":\"Admin\",\"roles\":[\"read\",\"write\"],\"meta\":{\"id\":123}}";
        assertEquals(expected, builder.build());
    }
}
