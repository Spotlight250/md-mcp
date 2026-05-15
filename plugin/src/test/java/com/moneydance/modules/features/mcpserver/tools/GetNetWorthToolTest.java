package com.moneydance.modules.features.mcpserver.tools;

import org.junit.jupiter.api.Test;
import com.moneydance.modules.features.mcpserver.json.JsonParser;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class GetNetWorthToolTest {

    @Test
    void testMetadata() {
        GetNetWorthTool tool = new GetNetWorthTool();
        assertEquals("get_net_worth", tool.getName());
    }

    @Test
    void testParameterExtraction() {
        String params = "{\"as_of_date\":\"2024-01-01\", \"account_ids\":[\"acct1\", \"acct2\"]}";
        assertEquals("2024-01-01", JsonParser.getString(params, "as_of_date"));
        assertEquals("[\"acct1\",\"acct2\"]", JsonParser.getValue(params, "account_ids"));
    }

    @Test
    void testJsonArrayParsing() {
        // Test that the tool can handle various JSON array formats
        String params = "{\"account_ids\": [\"uuid-1\", \"uuid-2\"]}";
        String idsValue = JsonParser.getValue(params, "account_ids");
        Object parsed = JsonParser.parse(idsValue);
        
        assertTrue(parsed instanceof List);
        List<?> list = (List<?>) parsed;
        assertEquals(2, list.size());
        assertEquals("uuid-1", list.get(0));
        assertEquals("uuid-2", list.get(1));
    }
}
