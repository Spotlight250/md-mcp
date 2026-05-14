package com.moneydance.modules.features.mcpserver.tools;

import org.junit.jupiter.api.Test;
import com.moneydance.modules.features.mcpserver.json.JsonParser;
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
}
