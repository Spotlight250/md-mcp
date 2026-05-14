package com.moneydance.modules.features.mcpserver.tools;

import com.moneydance.modules.features.mcpserver.json.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GetInvestmentsToolTest {

    @Test
    void testFormatInvestment() {
        String json = InvestmentFormatter.formatInvestment("uuid1", "My Account", "AAPL", "Apple Inc.", "10.5", 150.0, 1575, "USD").build();
        assertEquals("uuid1", JsonParser.getString(json, "account_id"));
        assertEquals("AAPL", JsonParser.getString(json, "ticker"));
        assertEquals("150.0", JsonParser.getValue(json, "current_price"));
        assertEquals("USD", JsonParser.getString(json, "currency"));
    }

    @Test
    void testMetadata() {
        GetInvestmentsTool tool = new GetInvestmentsTool();
        assertEquals("get_investments", tool.getName());
        assertTrue(tool.getDescription().contains("securities"));
    }
}
