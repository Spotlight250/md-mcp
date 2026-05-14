package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.modules.features.mcpserver.json.JsonArrayBuilder;
import com.moneydance.modules.features.mcpserver.json.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GetSecurityPerformanceToolTest {

    @Test
    void testFormatTransaction() {
        String json = SecurityPerformanceFormatter.formatTransaction("20230101", "Buy Apple", "10.0", "150.0").build();
        assertEquals("20230101", JsonParser.getString(json, "date"));
        assertEquals("Buy Apple", JsonParser.getString(json, "description"));
        assertEquals("10.0", JsonParser.getString(json, "amount"));
    }

    @Test
    void testMetadata() {
        GetSecurityPerformanceTool tool = new GetSecurityPerformanceTool();
        assertEquals("get_security_performance", tool.getName());
    }
}
